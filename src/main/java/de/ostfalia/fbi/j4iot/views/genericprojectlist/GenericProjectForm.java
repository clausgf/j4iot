package de.ostfalia.fbi.j4iot.views.genericprojectlist;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.textfield.TextField;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.views.generic.GenericListViewForm;

// TODO base this form on a generic one
public class GenericProjectForm extends GenericListViewForm<Project> {
    TextField name = new TextField("Project name");
    TextField description = new TextField("Description");
    TextField tags = new TextField("Tags");
    DateTimePicker createdAt = new DateTimePicker("Created at");
    DateTimePicker updatedAt = new DateTimePicker("Update at");
    Checkbox autocreateDevices = new Checkbox("Autocreate devices");
    Checkbox provisioningAutoapproval = new Checkbox("Provisioning autoapproval");

    // TODO list of provisioning tokens, editor
    // TODO list of devices, editor

    public GenericProjectForm() {
        super(Project.class);
        createdAt.setReadOnly(true);
        updatedAt.setReadOnly(true);

        add(name, description, tags, createdAt, updatedAt, autocreateDevices, provisioningAutoapproval, createButtonsLayout());
    }

}
