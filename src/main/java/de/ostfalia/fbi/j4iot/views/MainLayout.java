package de.ostfalia.fbi.j4iot.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import de.ostfalia.fbi.j4iot.data.service.UserService;
import de.ostfalia.fbi.j4iot.security.SecurityService;
import de.ostfalia.fbi.j4iot.views.about.AboutView;
import de.ostfalia.fbi.j4iot.views.device.DeviceDashboard;
import de.ostfalia.fbi.j4iot.views.device.DeviceList;
import de.ostfalia.fbi.j4iot.views.project.ProjectOverview;
import de.ostfalia.fbi.j4iot.views.project.ProjectList;
import de.ostfalia.fbi.j4iot.views.user.UserMasterDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private static final Logger log = LoggerFactory.getLogger(MainLayout.class);
    private final SecurityService securityService;
    private final IotService iotService;
    private final UserService userService;

    public final static String PROJECT_NAME_RP = "projectName";
    public final static String DEVICE_NAME_RP = "deviceName";

    private String projectName;
    private String deviceName;

    private H2 viewTitle;
    private final Dialog passwordChangeDialog = createPasswordChangeDialog();
    ComboBox<String> projectSelection = new ComboBox<>();
    ComboBox<String> deviceSelection = new ComboBox<>();
    SideNavItem navItemProjectDevices = new SideNavItem("Devices in project", ProjectOverview.class, LineAwesomeIcon.MICROCHIP_SOLID.create());
    SideNavItem navItemProjectDashboard = new SideNavItem("Project dashboard", ProjectOverview.class, LineAwesomeIcon.TACHOMETER_ALT_SOLID.create());
    SideNavItem navItemProjectSettings = new SideNavItem("Project settings", ProjectOverview.class, LineAwesomeIcon.COG_SOLID.create());
    SideNavItem navItemDeviceDashboard = new SideNavItem("Device dashboard", ProjectOverview.class, LineAwesomeIcon.TACHOMETER_ALT_SOLID.create());
    SideNavItem navItemDeviceSettings = new SideNavItem("Device settings", ProjectOverview.class, LineAwesomeIcon.COG_SOLID.create());


    public MainLayout(SecurityService securityService, IotService iotService, UserService userService) {
        this.securityService = securityService;
        this.iotService = iotService;
        this.userService = userService;
        this.projectName = null;
        this.deviceName = null;
        setNavItems();
        setPrimarySection(Section.DRAWER);
        addHeaderContent();
        addDrawerContent();
        setProjectName(null);
        setDeviceName(null);
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        MenuBar userMenuBar = new MenuBar();
        configureUserMenuBar(userMenuBar);

        var header = new HorizontalLayout(toggle, viewTitle, userMenuBar);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(viewTitle);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(true, header);
    }

    private void configureUserMenuBar(MenuBar userMenuBar) {
        userMenuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        Avatar avatar = new Avatar(securityService.getAuthenticatedUserFullName());
        //avatar.setImage(pictureUrl);
        MenuItem userMenuItem = userMenuBar.addItem(avatar);
        SubMenu userSubMenu = userMenuItem.getSubMenu();
        userSubMenu.addItem("Change password", e -> passwordChangeDialog.open());
        userSubMenu.addItem("Logout", e -> securityService.logout());
    }

    private void configureProjectSelection(ComboBox<String> projectSelection, ComboBox<String> deviceSelection) {
        projectSelection.setPlaceholder("Project");
        projectSelection.setTooltipText("Select the active project");
        projectSelection.setClearButtonVisible(true);
        projectSelection.setPrefixComponent(VaadinIcon.SEARCH.create());
        projectSelection.addFocusListener(comboBoxFocusEvent -> {
            projectSelection.setItems(iotService.findAllProjectNames(""));
        });
        projectSelection.addValueChangeListener(comboBoxStringComponentValueChangeEvent -> {
            //log.info("projectSelection old: project={} device={}", currentProject, currentDevice);
            setProjectName(comboBoxStringComponentValueChangeEvent.getValue());
            //log.info("projectSelection new: project={} device={}", currentProject, currentDevice);
        });
    }

    private void configureDeviceSelection(ComboBox<String> deviceSelection) {
        deviceSelection.setPlaceholder("Device");
        deviceSelection.setTooltipText("Select the active device");
        deviceSelection.setClearButtonVisible(true);
        deviceSelection.setPrefixComponent(VaadinIcon.SEARCH.create());
        deviceSelection.addFocusListener(comboBoxFocusEvent -> {
            if (projectName != null) {
                deviceSelection.setItems(iotService.searchAllDeviceNamesByProjectName(projectName, ""));
            } else {
                deviceSelection.setItems();
            }
        });
        deviceSelection.addValueChangeListener(comboBoxStringComponentValueChangeEvent -> {
            //log.info("deviceSelection old: project={} device={}", currentProject, currentDevice);
            setDeviceName(comboBoxStringComponentValueChangeEvent.getValue());
            //log.info("deviceSelection new: project={} device={}", currentProject, currentDevice);
        });
    }

    private void addDrawerContent() {
        H1 appName = new H1("j4iot");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);
        header.setClassName("drawer-header");

        SideNav overviewNav = new SideNav();
        overviewNav.setLabel("Overview");
        overviewNav.addItem(new SideNavItem("My Projects", ProjectList.class, LineAwesomeIcon.PROJECT_DIAGRAM_SOLID.create()));
        overviewNav.addItem(new SideNavItem("My Devices", DeviceList.class, LineAwesomeIcon.MICROCHIP_SOLID.create()));
        overviewNav.addItem(new SideNavItem("About", AboutView.class, LineAwesomeIcon.INFO_CIRCLE_SOLID.create()));

        SideNav projectNav = new SideNav();
        projectNav.setLabel("Project");
        projectNav.addItem(navItemProjectDevices);
        projectNav.addItem(navItemProjectDashboard);
        projectNav.addItem(navItemProjectSettings);

        SideNav deviceNav = new SideNav();
        deviceNav.setLabel("Device");
        deviceNav.addItem(navItemDeviceDashboard);
        deviceNav.addItem(navItemDeviceSettings);

        SideNav adminNav = new SideNav();
        adminNav.setLabel("Admin");
        //adminNav.setCollapsible(true);
        adminNav.addItem(new SideNavItem("Users", UserMasterDetail.class, LineAwesomeIcon.USERS_SOLID.create()));

        Footer footer = new Footer();
        footer.setClassName("drawer-footer");
        VerticalLayout footerLayout = new VerticalLayout();
        footer.add(footerLayout);
        configureProjectSelection(projectSelection, deviceSelection);
        configureDeviceSelection(deviceSelection);
        footerLayout.add(projectSelection, deviceSelection);

        addToDrawer(header, overviewNav, projectNav, deviceNav, adminNav, footer);
    }

    private Dialog createPasswordChangeDialog() {
        VerticalLayout dialogLayout = new VerticalLayout();
        PasswordField oldPassword = new PasswordField("Old password");
        PasswordField newPassword = new PasswordField("New password");
        dialogLayout.add(oldPassword, newPassword);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Change Password");
        dialog.add(dialogLayout);

        Button cancel = new Button("Cancel", e -> {
            oldPassword.clear();
            newPassword.clear();
            dialog.close();
        });
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(cancel);
        Button save = new Button("Save", event -> {
            try {
                String username = securityService.getAuthenticatedUsername();
                userService.changePassword(username, oldPassword.getValue(), newPassword.getValue());
                Notification.show("Password changed!");
            } catch (UsernameNotFoundException e) {
                Notification.show("Wrong username/password, data not modified!");
            }
            oldPassword.clear();
            newPassword.clear();
            dialog.close();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(save);
        return dialog;
    }

    private void setNavItems() {
        navItemProjectDevices.setVisible(projectName != null);
        navItemProjectDashboard.setVisible(projectName != null);
        navItemProjectSettings.setVisible(projectName != null);
        navItemDeviceDashboard.setVisible(deviceName != null);
        navItemDeviceSettings.setVisible(deviceName != null);

        RouteParameters projectRp;
        RouteParameters deviceRp;
        if (projectName != null && deviceName != null) {
            projectRp = new RouteParameters(new RouteParam(PROJECT_NAME_RP, projectName));
            deviceRp = new RouteParameters(new RouteParam(PROJECT_NAME_RP, projectName), new RouteParam(DEVICE_NAME_RP, deviceName));
        } else if (projectName != null && deviceName == null) {
            projectRp = new RouteParameters(new RouteParam(PROJECT_NAME_RP, projectName));
            deviceRp = new RouteParameters(new RouteParam(PROJECT_NAME_RP, projectName), new RouteParam(DEVICE_NAME_RP, ""));
        } else {
            projectRp = new RouteParameters();
            deviceRp = new RouteParameters(new RouteParam(PROJECT_NAME_RP, ""), new RouteParam(DEVICE_NAME_RP, ""));
        }

        navItemProjectDevices.setPath(DeviceList.class, projectRp);
        navItemProjectDashboard.setPath(ProjectOverview.class);
        navItemProjectSettings.setPath(ProjectOverview.class);

        navItemDeviceDashboard.setPath(DeviceDashboard.class, deviceRp);
        navItemDeviceSettings.setPath(ProjectOverview.class);
    }

    private void setProjectName(String newProjectName) {
        if (newProjectName == null || !iotService.projectExistsByName(newProjectName))
        { // project is null or does not exist: reset project and device selection
            projectName = null;
            projectSelection.clear();
            setDeviceName(null);
        } else { // project named newProject is present
            if (!newProjectName.equals(projectName)) {
                log.info("New project name differing from current one, clearing device!");
                projectName = newProjectName;
                setDeviceName(null);
            }
        }
    }

    private void setDeviceName(String newDeviceName) {
        boolean projectExists = (projectName != null) && iotService.projectExistsByName(projectName);
        boolean deviceExists = projectExists && (deviceName != null) && iotService.deviceExistsByProjectNameAndDeviceName(projectName, newDeviceName);
        if (!projectExists) {
            projectName = null;
            projectSelection.clear();
        }
        if (!deviceExists) {
            deviceName = null;
            deviceSelection.clear();
        }
        setNavItems();
        if (projectExists && !deviceExists) {
            RouteParameters rp = new RouteParameters(new RouteParam(PROJECT_NAME_RP, projectName));
            UI.getCurrent().navigate(DeviceList.class, rp);
        }
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
        log.info("afterNavigation");
    }

    private String getCurrentPageTitle() {
        PageTitle titleAnnotation = getContent().getClass().getAnnotation(PageTitle.class);
        String title = titleAnnotation == null ? "" : titleAnnotation.value();
        if (titleAnnotation == null) {
            Component c = getContent();
            if (HasDynamicTitle.class.isInstance(c)) {
                title = ((HasDynamicTitle) c).getPageTitle();
            }
        }
        return title;
    }

}
