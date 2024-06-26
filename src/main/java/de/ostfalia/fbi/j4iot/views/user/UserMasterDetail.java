package de.ostfalia.fbi.j4iot.views.user;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.ostfalia.fbi.j4iot.data.entity.User;
import de.ostfalia.fbi.j4iot.data.service.UserService;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import java.util.Optional;

@PageTitle("Users")
@Route(value="/users/:userId?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
//@PermitAll
@Uses(Icon.class)
public class UserMasterDetail extends Div implements BeforeEnterObserver {

    private final String USER_ID = "userId";
    private final String USER_EDIT_ROUTE_TEMPLATE = "/users/%s/edit";

    private final Grid<User> grid = new Grid<>(User.class, false);

    private TextField name = new TextField("Username");
    private DateTimePicker createdAt = new DateTimePicker("Created at");
    private DateTimePicker updatedAt = new DateTimePicker("Updated at");
    private PasswordField passwordField = new PasswordField("Password");
    private TextField firstName = new TextField("First name");
    private TextField lastName = new TextField("Last name");
    private TextField email = new TextField("Email");
    private DateTimePicker expiresAt = new DateTimePicker("Expires at");
    private DateTimePicker lastLoginAt = new DateTimePicker("Last login at");

    private final Button cancelButton = new Button("Cancel");
    private final Button saveCreateButton = new Button("Save");

    private final BeanValidationBinder<User> binder;

    private User user;

    private final UserService userService;


    public UserMasterDetail(UserService userService) {
        this.userService = userService;
        addClassNames("master-detail-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSplitterPosition(70);

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("firstName").setAutoWidth(true);
        grid.addColumn("lastName").setAutoWidth(true);
        grid.addColumn("lastLoginAt").setAutoWidth(true);
        //grid.addColumn("role").setAutoWidth(true);

        grid.setItems(userService.findAll());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(USER_EDIT_ROUTE_TEMPLATE, event.getValue().getName()));
            } else {
                clearForm();
                UI.getCurrent().navigate(UserMasterDetail.class);
            }
        });

        // Bind fields. This is where you'd define e.g. validation rules
        binder = new BeanValidationBinder<>(User.class);
        binder.bindInstanceFields(this);

        cancelButton.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> userId = event.getRouteParameters().get(USER_ID).map(String::toString);
        populateForm(null);
        if (userId.isPresent()) {
            Optional<User> userFromBackend = userService.findById(userId.get());
            if (userFromBackend.isPresent()) {
                populateForm(userFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested user was not found, ID = %s", userId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available, refresh grid
                refreshGrid();
                event.forwardTo(UserMasterDetail.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayout = new Div();
        editorLayout.setClassName("editor-layout");

        H3 header = new H3("User");
        header.setClassName("editor-header");
        editorLayout.add(header);

        Div editorDiv = new Div();
        editorDiv.setClassName("editor-main");
        editorLayout.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        createdAt.setReadOnly(true);
        updatedAt.setReadOnly(true);
        lastLoginAt.setReadOnly(true);
        name.setReadOnly(true);
        firstName.setReadOnly(true);
        lastName.setReadOnly(true);
        email.setReadOnly(true);
        expiresAt.setReadOnly(true);
        formLayout.add(name, passwordField, createdAt, updatedAt, firstName, lastName, email, expiresAt, lastLoginAt);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayout);

        splitLayout.addToSecondary(editorLayout);
    }

    private void createButtonLayout(Div editorLayout) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("editor-footer");
        saveCreateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        buttonLayout.add(saveCreateButton, cancelButton);

        editorLayout.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        //grid.getDataProvider().refreshAll();
        grid.setItems(userService.findAll());
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(User value) {
        this.user = value;
        binder.readBean(this.user);
        passwordField.setVisible(value == null);
        saveCreateButton.setText(value == null ? "Create" : "Save");
    }
}
