package de.ostfalia.fbi.j4iot.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@PermitAll
@PageTitle("Visualization")
@Route(value = "/visualization", layout = MainLayout.class)
public class VisualizationView extends VerticalLayout {

    Logger log = LoggerFactory.getLogger(VisualizationView.class);
    @Value("${j4iot.visualization.url}") String visualizationUrl;
    
    public VisualizationView() {
        setSpacing(false);

        Button b = new Button("Open Visualization in new Window", event -> {
            log.info("Opening url {}", visualizationUrl);
            UI.getCurrent().getPage().open(visualizationUrl);
        });
        add(b);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

}
