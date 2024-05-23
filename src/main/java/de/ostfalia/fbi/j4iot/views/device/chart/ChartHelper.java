package de.ostfalia.fbi.j4iot.views.device.chart;

import com.vaadin.flow.component.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

public interface ChartHelper<Y, X> {

    void setTimeFormater(DateTimeFormatter formater);

    void updateValues(Component component, List<ChartItem<Y, X>> graphItems);

    Component build(List<ChartItem<Double, Instant>> graphItems);
}
