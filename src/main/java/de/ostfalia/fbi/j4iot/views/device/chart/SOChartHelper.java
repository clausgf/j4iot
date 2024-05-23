package de.ostfalia.fbi.j4iot.views.device.chart;

import com.storedobject.chart.*;
import com.vaadin.flow.component.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SOChartHelper implements ChartHelper<Double, Instant> {
    List<LineChart> lines = new ArrayList<>();
    private RectangularCoordinate rc;

    @Override
    public void updateValues(Component component, List<ChartItem<Double, Instant>> graphItems) {
        SOChart chart = (SOChart) component;
        setData(chart, graphItems);
    }

    @Override
    public Component build(List<ChartItem<Double, Instant>> graphItems) {
        SOChart chart = new SOChart();

        chart.setSize("1300px", "700px");

        XAxis xAxis = new XAxis(DataType.DATE);
        YAxis yAxis = new YAxis(DataType.NUMBER);
        rc = new RectangularCoordinate(xAxis, yAxis);

        lines = new ArrayList<>();
        setData(chart, graphItems);

        return chart;
    }

    private void setData(SOChart chart, List<ChartItem<Double, Instant>> graphItems){
        for(ChartItem<Double, Instant> chartItem : graphItems){
            LineChart found = null;
            for(LineChart lineChart : lines){
                if (lineChart.getName().equals(chartItem.getName())){
                    found = lineChart;
                    break;
                }
            }
            if (found == null){
                found = createLine(chart, chartItem);
            }
            TimeData xValues = (TimeData) found.getData()[0];
            xValues.clear();
            Data yValues = (Data) found.getData()[1];
            yValues.clear();
            for(ChartItemData<Double, Instant> data : chartItem.getData()){
                xValues.add(data.getxValue().atZone(ZoneId.systemDefault()).toLocalDateTime());
                yValues.add(data.getyValue());
            }
            try {
                chart.updateData(found.getData());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private LineChart createLine(SOChart chart, ChartItem<Double, Instant> chartItem){
        TimeData xValues = new TimeData();
        xValues.setName("Zeit");
        Data yValues = new Data();
        yValues.setName("Wert");
        LineChart lineChart = new LineChart(xValues, yValues);
        lineChart.setName(chartItem.getName());
        lineChart.plotOn(rc);
        chart.add(lineChart);
        lines.add(lineChart);
        return lineChart;
    }

    @Override
    public void setTimeFormater(DateTimeFormatter formater) {

    }
}
