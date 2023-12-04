package de.ostfalia.fbi.j4iot.views.project;

import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.textfield.TextField;
import de.ostfalia.fbi.j4iot.data.entity.Forwarding;
import de.ostfalia.fbi.j4iot.views.GenericFormDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ForwardingDialog extends GenericFormDialog<Forwarding> {

    private Logger log = LoggerFactory.getLogger(ForwardingDialog.class);

    TextField name = new TextField("Forwarding name");
    TextField forwardFromUrl = new TextField("Forward from URL");
    TextField forwardToUrl = new TextField("Forward to URL");
    Checkbox extendUrl = new Checkbox("Add remainder of \"from\"-URL to \"to\"-URL");
    Checkbox enableMethodGet = new Checkbox("Support HTTP GET method");
    DateTimePicker lastUseAt = new DateTimePicker("Last use");


    public ForwardingDialog(Class<Forwarding> modelClass, onSaveCreateListener<Forwarding> onSaveCreate) {
        super(modelClass, "Edit URL forwarding", onSaveCreate);
        binder.bindInstanceFields(this);

        name.addValueChangeListener(event -> {
            // UriComponents uriComponents = MvcUriComponentsBuilder
            //         .fromMethodName(RestDeviceApi.class, "getForwardWithDeviceAuth", "a", event.getValue(), "", null)
            //         .buildAndExpand();
            // URI uri = uriComponents.encode().toUri();
            // forwardFromUrl.setValue(uri.toString());#
            forwardFromUrl.setValue("http[s]://hostname:port/.../api/forward/{projectName}/"+event.getValue()+"/...");
        });
        Arrays.<HasValueAndElement>asList(forwardFromUrl).forEach(e -> e.setReadOnly(true));
        Arrays.asList(lastUseAt).forEach(e -> e.setReadOnly(true));
        Arrays.asList(name, forwardFromUrl, forwardToUrl).forEach(e -> form.setColspan(e, 2));
        Arrays.asList(extendUrl, enableMethodGet).forEach(e -> form.setColspan(e, 2));
        Arrays.asList(lastUseAt).forEach(e -> form.setColspan(e, 2));
        form.add(name, forwardFromUrl, forwardToUrl, extendUrl, enableMethodGet, lastUseAt);
    }
}
