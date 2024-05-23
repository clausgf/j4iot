package de.ostfalia.fbi.j4iot.views.device.chart;

import java.util.List;

public class ChartItem<Y, X> {
    private String name;
    private List<ChartItemData<Y, X>> data;

    public ChartItem(String name, List<ChartItemData<Y, X>> data){
        this.name = name;
        this.data = data;
    }

    public String getName(){
        return name;
    }

    public List<ChartItemData<Y, X>> getData(){
        return data;
    }
}
