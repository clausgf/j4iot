package de.ostfalia.fbi.j4iot.views.device.chart;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.XAxis;
import com.github.appreciated.apexcharts.config.builder.GridBuilder;
import com.github.appreciated.apexcharts.config.builder.StrokeBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.grid.builder.RowBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.xaxis.TickPlacement;
import com.github.appreciated.apexcharts.config.xaxis.builder.LabelsBuilder;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ApexChartHelper extends ApexChartsBuilder implements ChartHelper<Double, Instant> {
    private DateTimeFormatter formater = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private Series<Double> series;


    @Override
    public void updateValues(Component component, List<ChartItem<Double, Instant>> graphItems) {
        ApexCharts graph = (ApexCharts) component;
        setSeriesValues(series, graphItems.get(0).getData());
        graph.updateSeries(series);
        graph.setXaxis(createXAxis(getCategories(graphItems.get(0).getData())));
    }

    @Override
    public Component build(List<ChartItem<Double, Instant>> graphItems) {
        series = new Series<>();
        setSeriesValues(series, graphItems.get(0).getData());

        return withChart(com.github.appreciated.apexcharts.config.builder.ChartBuilder.get()
                .withType(Type.LINE)
                .withZoom(ZoomBuilder.get()
                        .withEnabled(false)
                        .build())
                .build())
                .withStroke(StrokeBuilder.get()
                        .withCurve(Curve.STRAIGHT)
                        .build())
                .withGrid(GridBuilder.get()
                        .withRow(RowBuilder.get()
                                .withColors("#f3f3f3", "transparent")
                                .withOpacity(0.5).build()
                        ).build())
                .withXaxis(createXAxis(getCategories(graphItems.get(0).getData())))
                .withSeries(series).build();
    }

    private XAxis createXAxis(List<String> categories){
        return XAxisBuilder.get()
                .withCategories(categories)
                .withTickPlacement(TickPlacement.ON)
                .withLabels(LabelsBuilder.get().withRotate((double) -45).withRotateAlways(true).build())
                .build();
    }

    private List<String> getCategories(List<ChartItemData<Double, Instant>> graphItems){
        List<String> result = new ArrayList<>();
        for(ChartItemData<Double, Instant> graphItem : graphItems){
            result.add(convertInstant(graphItem.getxValue()));
        }
        return result;
    }

    private void setSeriesValues(Series<Double> series, List<ChartItemData<Double, Instant>> graphItems){
        Double[] arr = new Double[graphItems.size()];
        for(int i = 0; i< graphItems.size(); i++){
            arr[i] = graphItems.get(i).getyValue();
        }
        series.setData(arr);
    }

    private String convertInstant(Instant instant){
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formater);
    }

    @Override
    public void setTimeFormater(DateTimeFormatter formater) {
        if (formater == null){
            throw new IllegalArgumentException("Argument must not be null.");
        }
        this.formater = formater;
    }
}
