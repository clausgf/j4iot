package de.ostfalia.fbi.j4iot.views.device;

import com.github.appreciated.apexcharts.helper.Series;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import de.ostfalia.fbi.j4iot.data.service.ProjectService;
import de.ostfalia.fbi.j4iot.data.service.TimeseriesService;
import de.ostfalia.fbi.j4iot.views.MainLayout;
import de.ostfalia.fbi.j4iot.views.device.chart.ChartHelper;
import de.ostfalia.fbi.j4iot.views.device.chart.ChartItem;
import de.ostfalia.fbi.j4iot.views.device.chart.ChartItemData;
import de.ostfalia.fbi.j4iot.views.device.chart.SOChartHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@AnonymousAllowed
@Route(value="/projects/:projectId/devices/:id?/graph", layout = MainLayout.class)
public class DeviceGraph  extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {
    private static final boolean IS_LOCALHOST = true;
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final DateTimeFormatter formater = DateTimeFormatter.ofPattern(DATE_FORMAT +" HH:mm").localizedBy(Locale.GERMANY);
    private static final String MAIN_DATA_CONTROL_CSS_CLASS = "main-controls";

    private final Logger log = LoggerFactory.getLogger(DeviceEditor.class);
    public final static String ID_ROUTING_PARAMETER = "id";
    public final static String PROJECT_ID_ROUTING_PARAMETER = "projectId";
    private final ProjectService projectService;
    private final DeviceService deviceService;
    private final TimeseriesService timeseriesService;
    private Project project;
    private Device device;

    private Button chartModeButton;
    private Button tableModeButton;
    private Button allDataModeButton;
    private Grid<ChartItemData<Double, Instant>> table;
    private Component chart;
    private Component allDataChart;
    private HorizontalLayout datePickerContainer;
    private List<String> fieldItems;
    private String selectedField;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Instant startDate;
    private Instant endDate;
    private long aggregateSec;

    ChartHelper<Double, Instant> helper;
    ChartHelper<Double, Instant> allDataHelper;

    public DeviceGraph(ProjectService projectService, DeviceService deviceService, TimeseriesService timeseriesService) {
        this.projectService = projectService;
        this.deviceService = deviceService;
        this.timeseriesService = timeseriesService;
        createHeaderContainer();
        createMainContainer();
    }

    public static RouteParameters getRouteParameters(Project project, Device device) {
        if ((project != null) && (device != null)) {
            return new RouteParameters(
                    new RouteParam(PROJECT_ID_ROUTING_PARAMETER, project.getId()),
                    new RouteParam(ID_ROUTING_PARAMETER, device.getId()));
        } else if ((project != null)) {
            return new RouteParameters(new RouteParam(PROJECT_ID_ROUTING_PARAMETER, project.getId()));
        } else {
            return RouteParameters.empty();
        }
    }

    public void beforeEnter(BeforeEnterEvent event) {
        RouteParameters routeParameters = event.getRouteParameters();
        Optional<Long> projectId = routeParameters.getLong(PROJECT_ID_ROUTING_PARAMETER);
        projectId.ifPresent(aLong -> project = projectService.findByAuthAndId(aLong).orElse(null));
        if (project == null) {
            String msg = "No access to project or not found id=" + projectId.orElse(null);
            log.error(msg);
            Notification.show(msg);
        }

        Optional<Long> deviceId = routeParameters.getLong(ID_ROUTING_PARAMETER);
        deviceId.ifPresent(aLong -> device = deviceService.findById(aLong).orElse(null));
        if (device == null) {
            String msg = "No access to device or not found id=" + deviceId.orElse(null);
            log.error(msg);
            Notification.show(msg);
        }

        setData(loadData());
        //activateChartMode();
    }

    @Override
    public String getPageTitle() {
        return DeviceUtil.getPageTitle("Device graph", device);
    }

    private void createHeaderContainer(){
        HorizontalLayout container = new HorizontalLayout();
        add(container);
        container.setWidth("70%");

        //Visualization Mode
        VerticalLayout paddingContainer = new VerticalLayout();
        container.add(paddingContainer);
        HorizontalLayout modeContainer = new HorizontalLayout();
        paddingContainer.add(modeContainer);
        chartModeButton = new Button("Diagramm");
        chartModeButton.setIcon(VaadinIcon.CHART.create());
        chartModeButton.addClickListener(x -> activateChartMode());
        modeContainer.add(chartModeButton);

        tableModeButton = new Button("Tabelle");
        tableModeButton.setIcon(VaadinIcon.TABLE.create());
        tableModeButton.addClickListener(x -> activateTableMode());
        modeContainer.add(tableModeButton);

        allDataModeButton = new Button("Alle Daten");
        allDataModeButton.setIcon((VaadinIcon.CHART.create()));
        allDataModeButton.addClickListener(x -> activateAllDataMode());
        modeContainer.add(allDataModeButton);

        //Chart Value Field
        VerticalLayout fieldContainer = new VerticalLayout();
        container.add(fieldContainer);
        fieldItems = loadFields();
        ComboBox<String> fieldComboBox = new ComboBox<>();
        fieldContainer.add(fieldComboBox);
        fieldComboBox.setItems(fieldItems);
        fieldComboBox.addValueChangeListener(x -> changeChartValueField(x.getValue()));
        fieldComboBox.setValue("battery_V");

        //Chart Value Time Period
        List<TimePeriodItem> timePeriodItems = new ArrayList<>();
        timePeriodItems.add(new TimePeriodItem("Eigen", 0, ChronoUnit.MINUTES));
        timePeriodItems.add(new TimePeriodItem("Letzten %d Minuten", 30, ChronoUnit.MINUTES));
        timePeriodItems.add(new TimePeriodItem("Letzte %d Stunde", 1, ChronoUnit.HOURS));
        timePeriodItems.add(new TimePeriodItem("Letzten %d Stunde", 12, ChronoUnit.HOURS));
        timePeriodItems.add(new TimePeriodItem("Letzten %d Stunde", 24, ChronoUnit.HOURS));
        timePeriodItems.add(new TimePeriodItem("Letzte Woche", 24*7, ChronoUnit.HOURS));

        VerticalLayout timePeriodContainer = new VerticalLayout();
        container.add(timePeriodContainer);
        ComboBox<TimePeriodItem> timePeriodComboBox = new ComboBox<>();
        timePeriodContainer.add(timePeriodComboBox);
        timePeriodComboBox.setItems(timePeriodItems);
        timePeriodComboBox.addValueChangeListener(x -> changeChartValueTimePeriod(x.getValue()));

        datePickerContainer = new HorizontalLayout();
        datePickerContainer.setVisible(false);
        timePeriodContainer.add(datePickerContainer);
        DatePicker.DatePickerI18n datePickerFormat = new DatePicker.DatePickerI18n();
        datePickerFormat.setDateFormat(DATE_FORMAT);
        startDatePicker = new DatePicker();
        startDatePicker.setI18n(datePickerFormat);
        startDatePicker.addValueChangeListener(x -> changeChartValueTimePeriod(localDateToInstant(x.getValue()), localDateToInstant(endDatePicker.getValue())));
        datePickerContainer.add(startDatePicker);
        endDatePicker = new DatePicker();
        startDatePicker.setI18n(datePickerFormat);
        endDatePicker.addValueChangeListener(x -> changeChartValueTimePeriod(localDateToInstant(startDatePicker.getValue()), localDateToInstant(x.getValue())));
        datePickerContainer.add(endDatePicker);
        timePeriodComboBox.setValue(timePeriodItems.get(0));
        startDatePicker.setValue(LocalDate.now().minusDays(1));
        endDatePicker.setValue(LocalDate.now());

        //Chart value time aggregation
        VerticalLayout aggregateContainer = new VerticalLayout();
        container.add(aggregateContainer);

        List<AggregateItem> aggregateItems = new ArrayList<>();
        aggregateItems.add(new AggregateItem("30min", Duration.ofMinutes(30).toSeconds()));
        aggregateItems.add(new AggregateItem("1h", Duration.ofHours(1).toSeconds()));
        aggregateItems.add(new AggregateItem("1 Tag", Duration.ofDays(1).toSeconds()));
        aggregateItems.add(new AggregateItem("1 Woche", Duration.ofDays(7).toSeconds()));

        ComboBox<AggregateItem> aggregateComboBox = new ComboBox<>();
        aggregateContainer.add(aggregateComboBox);
        aggregateComboBox.setItems(aggregateItems);
        aggregateComboBox.addValueChangeListener(x -> changeChartValueAggregateValue(x.getValue()));
        aggregateComboBox.setValue(aggregateItems.get(2));
    }

    private void createMainContainer(){
        HorizontalLayout mainContainer = new HorizontalLayout();
        add(mainContainer);

        allDataHelper = new SOChartHelper();
        allDataChart = allDataHelper.build(new ArrayList<>());
        allDataChart.setClassName(MAIN_DATA_CONTROL_CSS_CLASS);
        mainContainer.add(allDataChart);

        helper = new SOChartHelper();
        chart = helper.build(new ArrayList<>());
        chart.setClassName(MAIN_DATA_CONTROL_CSS_CLASS);
        mainContainer.setSizeFull();
        mainContainer.add(chart);

        table = new Grid<>();
        mainContainer.add(table);
        table.addColumn(x -> convertInstant(x.getxValue())).setHeader("Zeit");
        table.addColumn(ChartItemData::getyValue).setHeader("Wert");
        table.setClassName(MAIN_DATA_CONTROL_CSS_CLASS);
    }

    private void setData(List<ChartItem<Double, Instant>> records){
        if (table == null || chart == null || allDataChart == null) return;
        if (allDataChart.isVisible()){
            allDataHelper.updateValues(allDataChart, records);
        }
        if (records.size() > 0 && chart.isVisible()){
            for(ChartItem<Double, Instant> record : records){
                if (record.getName().equals(selectedField)){
                    table.setItems(record.getData());
                    List<ChartItem<Double, Instant>> single = new ArrayList<>();
                    single.add(new ChartItem<>("Data", record.getData()));
                    helper.updateValues(chart, single);
                }
            }
        }
    }

    private List<ChartItem<Double, Instant>> loadData(){
        if (device == null || startDate == null || endDate == null || aggregateSec <= 0 || selectedField == null){
            return new ArrayList<>();
        }

        if (IS_LOCALHOST){
            List<ChartItem<Double, Instant>> chartItems = new ArrayList<>();
            Random random = new Random();
            for(String fieldItem : fieldItems){
                List<ChartItemData<Double, Instant>> datas = new ArrayList<>();
                int start = -random.nextInt(1,10);
                for(int i = 0; i < 10; i++){
                    Double nextValue = Double.valueOf(random.nextDouble(0,10));
                    datas.add(new ChartItemData<>(nextValue, Instant.now().plus(start + i, ChronoUnit.DAYS)));
                }
                chartItems.add(new ChartItem<>(fieldItem, datas));
            }
            return chartItems;
        }

        List<ChartItem<Double, Instant>> chartItems = new ArrayList<>();
        for(String fieldItem : fieldItems){
            List<ChartItemData<Double, Instant>> datas = new ArrayList<>();
            List<FluxRecord> records = timeseriesService.loadData(device, fieldItem, startDate, endDate, aggregateSec);
            for (FluxRecord record : records){
                datas.add(new ChartItemData<>(Double.valueOf(record.getValue().toString()), record.getTime()));
            }
            chartItems.add(new ChartItem<>(fieldItem, datas));
        }
        return chartItems;
    }

    private List<String> loadFields(){
        List<String> result = new ArrayList<>();
        if (IS_LOCALHOST){
            result.add("battery_V");
            result.add("Test");
            return result;
        }

        List<FluxTable> tables = timeseriesService.loadMeasurementFields(project.getName(), "system");
        for (FluxTable table : tables){
            for(FluxRecord record : table.getRecords()){
                result.add(record.getValue().toString());
            }
        }
        return result;
    }

    private void activateChartMode(){
        changeMode(VisualizationMode.CHART);
    }

    private void activateTableMode(){
        changeMode(VisualizationMode.TABLE);
    }

    private void activateAllDataMode(){
        changeMode(VisualizationMode.ALL_DATA);
    }

    private void changeMode(VisualizationMode mode){
        if (mode == VisualizationMode.CHART){
            chartModeButton.setClassName("active-show-button");
            tableModeButton.setClassName("deactive-show-button");
            allDataModeButton.setClassName("deactive-show-button");
            chart.setVisible(true);
            table.setVisible(false);
            allDataChart.setVisible(false);
        }else if (mode == VisualizationMode.TABLE){
            chartModeButton.setClassName("deactive-show-button");
            tableModeButton.setClassName("active-show-button");
            allDataModeButton.setClassName("deactive-show-button");
            chart.setVisible(false);
            table.setVisible(true);
            allDataChart.setVisible(false);
        }else if(mode == VisualizationMode.ALL_DATA){
            chartModeButton.setClassName("deactive-show-button");
            tableModeButton.setClassName("deactive-show-button");
            allDataModeButton.setClassName("active-show-button");
            chart.setVisible(false);
            table.setVisible(false);
            allDataChart.setVisible(true);
        }
    }

    private void changeChartValueField(String value){
        selectedField = value;
        setData(loadData());
    }

    private void changeChartValueAggregateValue(AggregateItem item){
        if (item != null){
            aggregateSec = item.sec();
            setData(loadData());
        }
    }

    private void changeChartValueTimePeriod(TimePeriodItem timePeriod){
        if (timePeriod != null) {
            if (timePeriod.text().equals("Eigen")) {
                datePickerContainer.setVisible(true);
                changeChartValueTimePeriod(localDateToInstant(startDatePicker.getValue()), localDateToInstant(endDatePicker.getValue()));
            } else {
                datePickerContainer.setVisible(false);
                try {
                    changeChartValueTimePeriod(Instant.now().minus(timePeriod.value(), timePeriod.unit()), Instant.now());
                }
                catch(NullPointerException e){
                    int a = 0;
                    a++;
                }
            }
        }
    }

    private void changeChartValueTimePeriod(Instant startDate, Instant endDate){
        this.startDate = startDate;
        this.endDate = endDate;
        setData(loadData());
    }

    private Instant localDateToInstant(LocalDate date){
        return date != null ? date.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
    }

    private static String convertInstant(Instant instant){
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formater);
    }

    enum VisualizationMode{
        CHART,
        TABLE,
        ALL_DATA
    }

    record AggregateItem(String text, long sec){
        public String toString(){
            return text;
        }
    }

    record TimePeriodItem(String text, int value, ChronoUnit unit){
        public String toString(){
            return String.format(text, value);
        }
    }
}
