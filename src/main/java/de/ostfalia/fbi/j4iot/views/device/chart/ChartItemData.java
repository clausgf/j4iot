package de.ostfalia.fbi.j4iot.views.device.chart;

public class ChartItemData<Y, X> {
    private final Y yValue;
    private final X xValue;

    public ChartItemData(Y yValue, X xValue){
        this.yValue = yValue;
        this.xValue = xValue;
    }

    public Y getyValue() {
        return yValue;
    }

    public X getxValue(){
        return xValue;
    }
}
