package de.ostfalia.fbi.j4iot.views;

import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import de.ostfalia.fbi.j4iot.data.service.ProjectService;
import de.ostfalia.fbi.j4iot.data.service.UserService;
import de.ostfalia.fbi.j4iot.security.SecurityService;
import de.ostfalia.fbi.j4iot.views.about.AboutView;
import de.ostfalia.fbi.j4iot.views.device.DeviceDashboard;
import de.ostfalia.fbi.j4iot.views.device.DeviceList;
import de.ostfalia.fbi.j4iot.views.device.DeviceSettings;
import de.ostfalia.fbi.j4iot.views.project.ProjectList;
import de.ostfalia.fbi.j4iot.views.project.ProjectSettings;
import de.ostfalia.fbi.j4iot.views.user.UserMasterDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    // ************************************************************************

    private static final Logger log = LoggerFactory.getLogger(MainLayout.class);
    private final AccessAnnotationChecker accessChecker;
    private final SecurityService securityService;
    private final ProjectService projectService;
    private final DeviceService deviceService;
    private final UserService userService;

    private Project currentProject;
    private Device currentDevice;
    private boolean isValueChangeEnabled = true;

    private H2 viewTitle;
    private final Dialog passwordChangeDialog = createPasswordChangeDialog();
    ComboBox<String> projectSelection = new ComboBox<>();
    ComboBox<String> deviceSelection = new ComboBox<>();
    SideNavItem navItemProjectDevices = new SideNavItem("Devices in project", DefaultView.class, VaadinIcon.ROCKET.create());
    SideNavItem navItemProjectDashboard = new SideNavItem("Project dashboard", DefaultView.class, VaadinIcon.DASHBOARD.create());
    SideNavItem navItemProjectSettings = new SideNavItem("Project settings", DefaultView.class, VaadinIcon.SLIDERS.create());
    SideNavItem navItemDeviceDashboard = new SideNavItem("Device dashboard", DefaultView.class, VaadinIcon.DASHBOARD.create());
    SideNavItem navItemDeviceSettings = new SideNavItem("Device settings", DefaultView.class, VaadinIcon.SLIDERS.create());

    // ************************************************************************

    public MainLayout(AccessAnnotationChecker accessChecker, SecurityService securityService, ProjectService projectService, DeviceService deviceService, UserService userService) {
        this.accessChecker = accessChecker;
        this.securityService = securityService;
        this.projectService = projectService;
        this.deviceService = deviceService;
        this.userService = userService;

        this.currentProject = null;
        this.currentDevice = null;
        setPrimarySection(Section.DRAWER);
        addHeaderContent();
        addDrawerContent();
        setCurrentProject(null);
    }

    // ************************************************************************

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

    // ************************************************************************

    private void addDrawerContent() {
        H1 appName = new H1("j4iot");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);
        header.setClassName("drawer-header");

        SideNav overviewNav = new SideNav();
        overviewNav.setLabel("Overview");
        overviewNav.addItem(new SideNavItem("My Projects", ProjectList.class, VaadinIcon.CONNECT_O.create()));
        overviewNav.addItem(new SideNavItem("My Devices", DeviceList.class, VaadinIcon.ROCKET.create()));
        overviewNav.addItem(new SideNavItem("Visualization", VisualizationView.class, VaadinIcon.SPLINE_CHART.create()));
        overviewNav.addItem(new SideNavItem("API doc", ApiDocView.class, VaadinIcon.BOOK.create()));
        overviewNav.addItem(new SideNavItem("About", AboutView.class, VaadinIcon.INFO_CIRCLE.create()));

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
        adminNav.setCollapsible(true);
        if (accessChecker.hasAccess(UserMasterDetail.class)) {
            adminNav.addItem(new SideNavItem("Users", UserMasterDetail.class, VaadinIcon.USERS.create()));
        }

        Footer footer = new Footer();
        footer.setClassName("drawer-footer");
        footer.setWidthFull();
        VerticalLayout footerLayout = new VerticalLayout();
        footer.add(footerLayout);
        footerLayout.setWidthFull();
        configureProjectSelection(projectSelection, deviceSelection);
        configureDeviceSelection(deviceSelection);
        footerLayout.add(projectSelection, deviceSelection);

        addToDrawer(header, overviewNav, projectNav, deviceNav, adminNav, footer);
    }

    private void configureProjectSelection(ComboBox<String> projectSelection, ComboBox<String> deviceSelection) {
        projectSelection.setWidthFull();
        projectSelection.setPlaceholder("Project");
        projectSelection.setTooltipText("Select the active project");
        projectSelection.setPrefixComponent(VaadinIcon.SEARCH.create());
        projectSelection.addFocusListener(comboBoxFocusEvent -> {
            updateProjectNames();
        });
        projectSelection.addValueChangeListener(comboBoxStringComponentValueChangeEvent -> {
            if (isValueChangeEnabled) {
                //log.info("projectSelection old: project={} device={}", currentProject, currentDevice);
                isValueChangeEnabled = false;
                String projectName = comboBoxStringComponentValueChangeEvent.getValue();
                Optional<Project> project = projectService.findByAuthAndName(projectName);
                setCurrentProject(project.orElse(null));
                isValueChangeEnabled = true;
                //log.info("projectSelection new: project={} device={}", currentProject, currentDevice);
            }
        });
    }

    private void configureDeviceSelection(ComboBox<String> deviceSelection) {
        deviceSelection.setWidthFull();
        deviceSelection.setPlaceholder("Device");
        deviceSelection.setTooltipText("Select the active device");
        deviceSelection.setPrefixComponent(VaadinIcon.SEARCH.create());
        deviceSelection.addFocusListener(comboBoxFocusEvent -> {
            updateDeviceNames();
        });
        deviceSelection.addValueChangeListener(comboBoxStringComponentValueChangeEvent -> {
            if (isValueChangeEnabled) {
                //log.info("deviceSelection old: project={} device={}", currentProject, currentDevice);
                isValueChangeEnabled = false;
                String deviceName = comboBoxStringComponentValueChangeEvent.getValue();
                if (currentProject != null) {
                    Optional<Device> device = deviceService.findByUserAuthAndProjectIdAndName(currentProject.getId(), deviceName);
                    setCurrentDevice(device.orElse(null));
                } else {
                    setCurrentDevice(null);
                }
                isValueChangeEnabled = true;
                //log.info("deviceSelection new: project={} device={}", currentProject, currentDevice);
            }
        });
    }

    // ************************************************************************

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
                userService.updatePassword(username, oldPassword.getValue(), newPassword.getValue());
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

    // ************************************************************************

    private void setNavItems() {
        navItemProjectDevices.setVisible(currentProject != null);
        navItemProjectDashboard.setVisible(currentProject != null);
        navItemProjectSettings.setVisible(currentProject != null);

        if (currentProject != null) {
            navItemProjectDevices.setPath(DeviceList.class, DeviceList.getRouteParameters(currentProject));
            navItemProjectDashboard.setPath(DefaultView.class);
            navItemProjectSettings.setPath(ProjectSettings.class, ProjectSettings.getRouteParameters(currentProject));
        } else {
            navItemProjectDevices.setPath(DefaultView.class);
            navItemProjectDashboard.setPath(DefaultView.class);
            navItemProjectSettings.setPath(DefaultView.class);
        }

        navItemDeviceDashboard.setVisible(currentDevice != null);
        navItemDeviceSettings.setVisible(currentDevice != null);

        if (currentDevice != null) {
            navItemDeviceDashboard.setPath(DeviceDashboard.class, DeviceDashboard.getRouteParameters(currentDevice));
            navItemDeviceSettings.setPath(DeviceSettings.class, DeviceSettings.getRouteParameters(currentProject, currentDevice));
        } else {
            navItemDeviceDashboard.setPath(DefaultView.class);
            navItemDeviceSettings.setPath(DefaultView.class);
        }

        String projectName = (currentProject != null) ? currentProject.getName() : "";
        if (!projectName.equals(projectSelection.getValue())) {
            updateProjectNames();
            isValueChangeEnabled = false;
            projectSelection.setValue(projectName);
            isValueChangeEnabled = true;
        }

        deviceSelection.setVisible(currentProject != null);
        String deviceName = (currentDevice != null) ? currentDevice.getName() : "";
        if (!deviceName.equals(deviceSelection.getValue())) {
            updateDeviceNames();
            isValueChangeEnabled = false;
            deviceSelection.setValue(deviceName);
            isValueChangeEnabled = true;
        }
    }

    // ************************************************************************

    public void updateProjectNames() {
        boolean oldIsValueChangeEnabled = isValueChangeEnabled;
        isValueChangeEnabled = false;
        projectSelection.setItems(projectService.findAllNamesByAuth());
        isValueChangeEnabled = oldIsValueChangeEnabled;
    }

    public void updateDeviceNames() {
        boolean oldIsValueChangeEnabled = isValueChangeEnabled;
        isValueChangeEnabled = false;
        if (currentProject != null) {
            deviceSelection.setItems(deviceService.findAllNamesByUserAuthAndProjectId(currentProject.getId()));
        } else {
            deviceSelection.setItems();
        }
        isValueChangeEnabled = oldIsValueChangeEnabled;
    }

    // ************************************************************************

    public void setCurrentProject(Project newProject) {
        if (newProject != null) {
            if (!newProject.equals(currentProject)) {
                //log.info("New project differing from current one, clearing device!");
                currentProject = newProject;
                currentDevice = null;
                setNavItems();
            }
        } else { // project empty: reset project and device selection
            currentProject = null;
            currentDevice = null;
            setNavItems();
        }
    }

    public void setCurrentDevice(Device newDevice) {
        if (newDevice != null) {
            if (!newDevice.equals(currentDevice)) {
                //log.info("New device differing from current one!");
                currentProject = newDevice.getProject();
                currentDevice = newDevice;
                setNavItems();
            }
        } else { // device empty
            currentDevice = null;
            setNavItems();
        }
    }

    // ************************************************************************

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        updatePageTitle();;
    }

    public void updatePageTitle() {
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle titleAnnotation = getContent().getClass().getAnnotation(PageTitle.class);
        String title = titleAnnotation == null ? "" : titleAnnotation.value();
        if (titleAnnotation == null) {
            Component c = getContent();
            if (c instanceof HasDynamicTitle componentWithDynamicTitle) {
                title = componentWithDynamicTitle.getPageTitle();
            }
        }
        return title;
    }

    // ************************************************************************

}
