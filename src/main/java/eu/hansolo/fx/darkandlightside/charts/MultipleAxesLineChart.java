package eu.hansolo.fx.darkandlightside.charts;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipleAxesLineChart extends StackPane {

    private final LineChart                 BASE_CHART;
    private final ObservableList<LineChart> BACKGROUND_CHARTS = FXCollections.observableArrayList();
    private final Map<LineChart, Color>     CHART_COLOR_MAP   = new HashMap<>();
    private final double                    Y_AXIS_WIDTH      = 60;
    private final AnchorPane                DETAILS_WINDOW;
    private final double                    Y_AXIS_SEPARATION = 20;
    private double                          strokeWidth       = 0.3;


    public MultipleAxesLineChart(final LineChart BASE_CHART, final Color LINE_COLOR) {
        this(BASE_CHART, LINE_COLOR, null);
    }
    public MultipleAxesLineChart(final LineChart BASE_CHART, final Color LINE_COLOR, final Double STROKE_WIDTH) {
        if (STROKE_WIDTH != null) { this.strokeWidth = STROKE_WIDTH; }
        this.BASE_CHART = BASE_CHART;

        CHART_COLOR_MAP.put(BASE_CHART, LINE_COLOR);

        styleBaseChart(BASE_CHART);
        styleChartLine(BASE_CHART, LINE_COLOR);
        setFixedAxisWidth(BASE_CHART);

        setAlignment(Pos.CENTER_LEFT);

        registerListeners();

        DETAILS_WINDOW = new AnchorPane();
        bindMouseEvents(BASE_CHART, this.strokeWidth);

        rebuildChart();
    }

    private void registerListeners() {
        BACKGROUND_CHARTS.addListener((Observable observable) -> rebuildChart());
    }

    private void bindMouseEvents(LineChart baseChart, Double strokeWidth) {
        final DetailsPopup detailsPopup = new DetailsPopup();
        getChildren().add(DETAILS_WINDOW);
        DETAILS_WINDOW.getChildren().add(detailsPopup);
        DETAILS_WINDOW.prefHeightProperty().bind(heightProperty());
        DETAILS_WINDOW.prefWidthProperty().bind(widthProperty());
        DETAILS_WINDOW.setMouseTransparent(true);

        setOnMouseMoved(null);
        setMouseTransparent(false);

        final Axis xAxis = baseChart.getXAxis();
        final Axis yAxis = baseChart.getYAxis();

        final Line xLine = new Line();
        final Line yLine = new Line();
        yLine.setFill(Color.GRAY);
        xLine.setFill(Color.GRAY);
        yLine.setStrokeWidth(strokeWidth/2);
        xLine.setStrokeWidth(strokeWidth/2);
        xLine.setVisible(false);
        yLine.setVisible(false);

        final Node chartBackground = baseChart.lookup(".chart-plot-background");
        for (Node n: chartBackground.getParent().getChildrenUnmodifiable()) {
            if (n != chartBackground && n != xAxis && n != yAxis) {
                n.setMouseTransparent(true);
            }
        }
        chartBackground.setCursor(Cursor.CROSSHAIR);
        chartBackground.setOnMouseEntered((event) -> {
            chartBackground.getOnMouseMoved().handle(event);
            detailsPopup.setVisible(true);
            xLine.setVisible(true);
            yLine.setVisible(true);
            DETAILS_WINDOW.getChildren().addAll(xLine, yLine);
        });
        chartBackground.setOnMouseExited((event) -> {
            detailsPopup.setVisible(false);
            xLine.setVisible(false);
            yLine.setVisible(false);
            DETAILS_WINDOW.getChildren().removeAll(xLine, yLine);
        });
        chartBackground.setOnMouseMoved(event -> {
            double x = event.getX() + chartBackground.getLayoutX();
            double y = event.getY() + chartBackground.getLayoutY();

            xLine.setStartX(10);
            xLine.setEndX(DETAILS_WINDOW.getWidth() - 10);
            xLine.setStartY(y+5);
            xLine.setEndY(y+5);

            yLine.setStartX(x+5);
            yLine.setEndX(x+5);
            yLine.setStartY(10);
            yLine.setEndY(DETAILS_WINDOW.getHeight() - 10);

            detailsPopup.showChartDescrpition(event);

            if (y + detailsPopup.getHeight() + 10 < getHeight()) {
                AnchorPane.setTopAnchor(detailsPopup, y+10);
            } else {
                AnchorPane.setTopAnchor(detailsPopup, y-10-detailsPopup.getHeight());
            }

            if (x + detailsPopup.getWidth() + 10 < getWidth()) {
                AnchorPane.setLeftAnchor(detailsPopup, x+10);
            } else {
                AnchorPane.setLeftAnchor(detailsPopup, x-10-detailsPopup.getWidth());
            }
        });
    }

    private void styleBaseChart(final LineChart BASE_CHART) {
        BASE_CHART.setCreateSymbols(false);
        BASE_CHART.setLegendVisible(false);
        BASE_CHART.getXAxis().setAutoRanging(false);
        BASE_CHART.getXAxis().setAnimated(false);
        BASE_CHART.getYAxis().setAnimated(false);
    }

    private void setFixedAxisWidth(LineChart chart) {
        chart.getYAxis().setPrefWidth(Y_AXIS_WIDTH);
        chart.getYAxis().setMaxWidth(Y_AXIS_WIDTH);
    }

    private void rebuildChart() {
        getChildren().clear();

        getChildren().add(resizeBaseChart(BASE_CHART));
        for (LineChart lineChart : BACKGROUND_CHARTS) {
            getChildren().add(resizeBackgroundChart(lineChart));
        }
        getChildren().add(DETAILS_WINDOW);
    }

    private Node resizeBaseChart(final LineChart LINE_CHART) {
        HBox hBox = new HBox(LINE_CHART);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.prefHeightProperty().bind(heightProperty());
        hBox.prefWidthProperty().bind(widthProperty());

        LINE_CHART.minWidthProperty().bind(widthProperty().subtract((Y_AXIS_WIDTH + Y_AXIS_SEPARATION) * BACKGROUND_CHARTS.size()));
        LINE_CHART.prefWidthProperty().bind(widthProperty().subtract((Y_AXIS_WIDTH + Y_AXIS_SEPARATION) * BACKGROUND_CHARTS.size()));
        LINE_CHART.maxWidthProperty().bind(widthProperty().subtract((Y_AXIS_WIDTH + Y_AXIS_SEPARATION) * BACKGROUND_CHARTS.size()));

        return LINE_CHART;
    }

    private Node resizeBackgroundChart(final LineChart LINE_CHART) {
        HBox hBox = new HBox(LINE_CHART);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.prefHeightProperty().bind(heightProperty());
        hBox.prefWidthProperty().bind(widthProperty());
        hBox.setMouseTransparent(true);

        LINE_CHART.minWidthProperty().bind(widthProperty().subtract((Y_AXIS_WIDTH + Y_AXIS_SEPARATION) * BACKGROUND_CHARTS.size()));
        LINE_CHART.prefWidthProperty().bind(widthProperty().subtract((Y_AXIS_WIDTH + Y_AXIS_SEPARATION) * BACKGROUND_CHARTS.size()));
        LINE_CHART.maxWidthProperty().bind(widthProperty().subtract((Y_AXIS_WIDTH + Y_AXIS_SEPARATION) * BACKGROUND_CHARTS.size()));

        LINE_CHART.translateXProperty().bind(BASE_CHART.getYAxis().widthProperty());
        LINE_CHART.getYAxis().setTranslateX((Y_AXIS_WIDTH + Y_AXIS_SEPARATION) * BACKGROUND_CHARTS.indexOf(LINE_CHART));

        return hBox;
    }

    public void addSeries(final XYChart.Series SERIES, final Color LINE_COLOR) {
        NumberAxis yAxis = new NumberAxis();
        NumberAxis xAxis = new NumberAxis();

        // style x-axis
        xAxis.setAutoRanging(false);
        xAxis.setVisible(false);
        xAxis.setOpacity(0.0); // somehow the upper setVisible does not work
        xAxis.lowerBoundProperty().bind(((NumberAxis) BASE_CHART.getXAxis()).lowerBoundProperty());
        xAxis.upperBoundProperty().bind(((NumberAxis) BASE_CHART.getXAxis()).upperBoundProperty());
        xAxis.tickUnitProperty().bind(((NumberAxis) BASE_CHART.getXAxis()).tickUnitProperty());

        // style y-axis
        yAxis.setSide(Side.RIGHT);
        yAxis.setLabel(SERIES.getName());

        // create chart
        LineChart lineChart = new LineChart(xAxis, yAxis);
        lineChart.setAnimated(false);
        lineChart.setLegendVisible(false);
        lineChart.getData().add(SERIES);

        styleBackgroundChart(lineChart, LINE_COLOR);
        setFixedAxisWidth(lineChart);

        CHART_COLOR_MAP.put(lineChart, LINE_COLOR);
        BACKGROUND_CHARTS.add(lineChart);
    }

    private void styleBackgroundChart(final LineChart LINE_CHART, final Color LINE_COLOR) {
        styleChartLine(LINE_CHART, LINE_COLOR);

        Node contentBackground = LINE_CHART.lookup(".chart-content").lookup(".chart-plot-background");
        contentBackground.setStyle("-fx-background-color: transparent;");

        LINE_CHART.setVerticalZeroLineVisible(false);
        LINE_CHART.setHorizontalZeroLineVisible(false);
        LINE_CHART.setVerticalGridLinesVisible(false);
        LINE_CHART.setHorizontalGridLinesVisible(false);
        LINE_CHART.setCreateSymbols(false);
    }

    private String colorToHex(final Color COLOR) { return COLOR.toString().replace("0x", "#"); }

    private void styleChartLine(final LineChart CHART, final Color LINE_COLOR) {
        CHART.getYAxis().lookup(".axis-label").setStyle("-fx-text-fill: " + colorToHex(LINE_COLOR) + "; -fx-font-weight: bold;");
        Node seriesLine = CHART.lookup(".chart-series-line");
        seriesLine.setStyle("-fx-stroke: " + colorToHex(LINE_COLOR) + "; -fx-stroke-width: " + strokeWidth + ";");
    }

    public Node getLegend() {
        HBox hBox = new HBox();

        final CheckBox baseChartCheckBox = new CheckBox(BASE_CHART.getYAxis().getLabel());
        baseChartCheckBox.setSelected(true);
        baseChartCheckBox.setStyle("-fx-text-fill: " + colorToHex(CHART_COLOR_MAP.get(BASE_CHART)) + "; -fx-font-weight: bold;");
        baseChartCheckBox.setDisable(true);
        baseChartCheckBox.getStyleClass().add("readonly-checkbox");
        baseChartCheckBox.setOnAction(event -> baseChartCheckBox.setSelected(true));
        hBox.getChildren().add(baseChartCheckBox);

        for (final LineChart lineChart : BACKGROUND_CHARTS) {
            CheckBox checkBox = new CheckBox(lineChart.getYAxis().getLabel());
            checkBox.setStyle("-fx-text-fill: " + colorToHex(CHART_COLOR_MAP.get(lineChart)) + "; -fx-font-weight: bold");
            checkBox.setSelected(true);
            checkBox.setOnAction(event -> {
                if (BACKGROUND_CHARTS.contains(lineChart)) {
                    BACKGROUND_CHARTS.remove(lineChart);
                } else {
                    BACKGROUND_CHARTS.add(lineChart);
                }
            });
            hBox.getChildren().add(checkBox);
        }

        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(20);
        hBox.setStyle("-fx-padding: 0 10 20 10");

        return hBox;
    }

    private class DetailsPopup extends VBox {

        private DetailsPopup() {
            setStyle("-fx-border-width: 1px; -fx-padding: 5 5 5 5px; -fx-border-color: gray; -fx-background-color: whitesmoke;");
            setVisible(false);
        }

        public void showChartDescrpition(final MouseEvent EVENT) {
            getChildren().clear();

            Long xValueLong = Math.round((double) BASE_CHART.getXAxis().getValueForDisplay(EVENT.getX()));

            HBox baseChartPopupRow = buildPopupRow(EVENT, xValueLong, BASE_CHART);
            if (baseChartPopupRow != null) {
                getChildren().add(baseChartPopupRow);
            }

            for (LineChart lineChart : BACKGROUND_CHARTS) {
                HBox popupRow = buildPopupRow(EVENT, xValueLong, lineChart);
                if (popupRow == null) continue;

                getChildren().add(popupRow);
            }
        }

        private HBox buildPopupRow(final MouseEvent EVENT, final Long X_VALUE_LONG, final LineChart LINE_CHART) {
            Label seriesName = new Label(LINE_CHART.getYAxis().getLabel());
            seriesName.setTextFill(CHART_COLOR_MAP.get(LINE_CHART));

            Number yValueForChart = getYValueForX(LINE_CHART, X_VALUE_LONG.intValue());
            if (yValueForChart == null) {
                return null;
            }
            Number yValueLower = Math.round(normalizeYValue(LINE_CHART, EVENT.getY() - 10));
            Number yValueUpper = Math.round(normalizeYValue(LINE_CHART, EVENT.getY() + 10));
            Number yValueUnderMouse = Math.round((double) LINE_CHART.getYAxis().getValueForDisplay(EVENT.getY()));

            // make series name bold when mouse is near given chart's line
            if (isMouseNearLine(yValueForChart, yValueUnderMouse, Math.abs(yValueLower.doubleValue()-yValueUpper.doubleValue()))) {
                seriesName.setStyle("-fx-font-weight: bold");
            }

            HBox popupRow = new HBox(10, seriesName, new Label("["+yValueForChart+"]"));
            return popupRow;
        }

        private double normalizeYValue(final LineChart LINE_CHART, final double VALUE) {
            Double val = (Double) LINE_CHART.getYAxis().getValueForDisplay(VALUE);
            return val == null ? 0 : val;
        }

        private boolean isMouseNearLine(final Number REAL_Y_VALUE, final Number Y_VALUE_UNDER_MOUSE, final Double TOLERANCE) {
            return (Math.abs(Y_VALUE_UNDER_MOUSE.doubleValue() - REAL_Y_VALUE.doubleValue()) < TOLERANCE);
        }

        public Number getYValueForX(final LineChart CHART, final Number X_VALUE) {
            List<XYChart.Data> dataList = ((List<XYChart.Data>)((XYChart.Series)CHART.getData().get(0)).getData());
            for (XYChart.Data data : dataList) {
                if (data.getXValue().equals(X_VALUE)) { return (Number)data.getYValue(); }
            }
            return null;
        }
    }
}