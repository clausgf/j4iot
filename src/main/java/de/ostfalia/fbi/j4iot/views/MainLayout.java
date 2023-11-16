package de.ostfalia.fbi.j4iot.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import de.ostfalia.fbi.j4iot.security.SecurityService;
import de.ostfalia.fbi.j4iot.views.about.AboutView;
import de.ostfalia.fbi.j4iot.views.devicelist.DeviceListView;
import de.ostfalia.fbi.j4iot.views.projectlist.ProjectListView;
import de.ostfalia.fbi.j4iot.views.userslist.UsersListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private static final Logger log = LoggerFactory.getLogger(MainLayout.class);
    private final SecurityService securityService;
    private final IotService iotService;

    private H2 viewTitle;

    private Optional<Project> currentProject = Optional.empty();
    private Optional<Device> currentDevice = Optional.empty();

    public MainLayout(SecurityService securityService, IotService iotService) {
        this.securityService = securityService;
        this.iotService = iotService;
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        ComboBox<String> deviceSelection = new ComboBox<>();

        ComboBox<String> projectSelection = new ComboBox<>();
        projectSelection.setPlaceholder("Project");
        projectSelection.setTooltipText("Select the active project");
        projectSelection.setClearButtonVisible(true);
        projectSelection.setPrefixComponent(VaadinIcon.SEARCH.create());
        projectSelection.addFocusListener(comboBoxFocusEvent -> {
            projectSelection.setItems(iotService.findAllProjectNames(""));
        });
        projectSelection.addValueChangeListener(comboBoxStringComponentValueChangeEvent -> {
            //log.info("projectSelection old: project={} device={}", currentProject, currentDevice);
            Optional<Project> newProject = iotService.findProjectByName(comboBoxStringComponentValueChangeEvent.getValue());
            if (newProject.isEmpty())
            {
                currentProject = Optional.empty();
                projectSelection.clear();
                currentDevice = Optional.empty();
                deviceSelection.clear();
            } else { // newProject present
                if (!newProject.equals(currentProject)) {
                    log.info("New project differing from current one, clearing device!");
                    currentProject = newProject;
                    currentDevice = Optional.empty();
                    deviceSelection.clear();
                }
            }
            //log.info("projectSelection new: project={} device={}", currentProject, currentDevice);
        });

        deviceSelection.setPlaceholder("Device");
        deviceSelection.setTooltipText("Select the active device");
        deviceSelection.setClearButtonVisible(true);
        deviceSelection.setPrefixComponent(VaadinIcon.SEARCH.create());
        deviceSelection.addFocusListener(comboBoxFocusEvent -> {
            if (currentProject.isPresent()) {
                deviceSelection.setItems(iotService.findAllDeviceNamesByProject(currentProject.get(), ""));
            } else {
                deviceSelection.setItems();
            }
        });
        deviceSelection.addValueChangeListener(comboBoxStringComponentValueChangeEvent -> {
            //log.info("deviceSelection old: project={} device={}", currentProject, currentDevice);
            if (currentProject.isEmpty()) {
                currentDevice = Optional.empty();
            } else {
                currentDevice = iotService.findDeviceByProjectAndName(currentProject.get(), comboBoxStringComponentValueChangeEvent.getValue());
            }
            if (currentDevice.isEmpty()) {
                deviceSelection.clear();
            }
            //log.info("deviceSelection new: project={} device={}", currentProject, currentDevice);
        });

        // String u = securityService.getAuthenticatedUser().getUsername();
        // Button logout = new Button("Logout" + u, e -> securityService.logout());
        Button logout = new Button("Logout", e -> securityService.logout());

        var header = new HorizontalLayout(toggle, viewTitle, projectSelection, deviceSelection, logout);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(viewTitle);
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(true, header);
    }

    private void addDrawerContent() {
        H1 appName = new H1("j4iot");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Projects", ProjectListView.class, LineAwesomeIcon.PROJECT_DIAGRAM_SOLID.create()));
        nav.addItem(new SideNavItem("Devices", DeviceListView.class, LineAwesomeIcon.MICROCHIP_SOLID.create()));
        nav.addItem(new SideNavItem("Users", UsersListView.class, LineAwesomeIcon.USERS_SOLID.create()));

        nav.addItem(new SideNavItem("About", AboutView.class, LineAwesomeIcon.FILE.create()));

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
