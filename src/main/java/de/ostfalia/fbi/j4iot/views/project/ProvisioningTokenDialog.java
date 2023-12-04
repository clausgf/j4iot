package de.ostfalia.fbi.j4iot.views.project;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.textfield.TextField;
import de.ostfalia.fbi.j4iot.data.entity.ProvisioningToken;
import de.ostfalia.fbi.j4iot.views.GenericFormDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ProvisioningTokenDialog extends GenericFormDialog<ProvisioningToken> {

    private Logger log = LoggerFactory.getLogger(ProvisioningTokenDialog.class);

    TextField token = new TextField("Token");
    DateTimePicker expiresAt = new DateTimePicker("Expires");
    DateTimePicker lastUseAt = new DateTimePicker("Last use");


    public ProvisioningTokenDialog(Class<ProvisioningToken> modelClass, onSaveCreateListener<ProvisioningToken> onSaveCreate) {
        super(modelClass, "Edit Provisioning Token", onSaveCreate);
        binder.bindInstanceFields(this);

        Arrays.<HasValueAndElement>asList(token, lastUseAt).forEach(e -> e.setReadOnly(true));
        Arrays.<Component>asList(token).forEach(e -> form.setColspan(e, 2));
        form.add(token, expiresAt, lastUseAt);
    }
}
