package de.ostfalia.fbi.j4iot.views;

import com.vaadin.flow.component.html.IFrame;
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
    @Value("${j4iot.visualization.url}") private String springDocSwaggerUrl = "https://iot-i.ostfalia.de/grafana/public-dashboards/3ec9aa9dec4d45eb8f67cfd9bb223ed0";
    
    public VisualizationView() {
        IFrame frame = new IFrame();
        frame.setSrc(springDocSwaggerUrl);
        frame.setHeight("100%");
        frame.setWidth("100%");
        add(frame);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

}
