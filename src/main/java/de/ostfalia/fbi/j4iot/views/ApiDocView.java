package de.ostfalia.fbi.j4iot.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// https://www.baeldung.com/spring-rest-openapi-documentation

@PermitAll
@PageTitle("API Documentation")
@Route(value = "/apidoc", layout = MainLayout.class)
public class ApiDocView extends VerticalLayout {

    Logger log = LoggerFactory.getLogger(ApiDocView.class);
    // TODO find a better way to determine the url from the configuration
    // TODO secure all springdoc endpoints!
    private String springDocSwaggerUrl = "/../api-doc/swagger-ui/index.html";

    public ApiDocView() {
        setSpacing(false);

        Button b = new Button("Open API doc in new Window", event -> {
            log.info("Opening url {}", springDocSwaggerUrl);
            UI.getCurrent().getPage().open(springDocSwaggerUrl);
        });
        add(b);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

}
