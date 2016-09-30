/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.darkandlightside.charts;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;


/**
 * User: hansolo
 * Date: 04.07.16
 * Time: 15:45
 */
public class DemoChartsSeries extends Application {
    private BarChart<String, Number>   chart;
    private BarChartFX<String, Number> chartFX;
    private ObservableList<NameValue>  nameValueList;


    @Override public void init() {
        ChartData data = new ChartData(new Record("Anakin", 20000),
                                       new Record("Obi-Wan", 18000),
                                       new Record("Luke", 17000),
                                       new Record("Leia", 10000),
                                       new Record("Kylo Ren", 15000));

        nameValueList = FXCollections.observableArrayList();

        XYChart.Series series = new Series();
        series.setName("Jedi");
        series.getData().setAll(data.getData());

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();

        chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Midichlorians (Standard)");
        chart.getData().add(series);

        Series<String, Number> seriesFX = new Series<>();
        nameValueList.forEach(nameValue -> seriesFX.getData().add(new Data<>(nameValue.getName(), nameValue.getValue())));

        CategoryAxis xAxisFX = new CategoryAxis();
        NumberAxis   yAxisFX = new NumberAxis();

        chartFX = new BarChartFX<>(xAxisFX, yAxisFX);
        chartFX.setTitle("Midichlorians (Modified)");
        chartFX.setAnimated(false);
        chartFX.getData().add(seriesFX);

        nameValueList.addListener((ListChangeListener) change -> {
            final Legend                 LEGEND = new Legend();
            final Series<String, Number> SERIES = new Series<>();

            // Create chart SERIES and LEGEND
            nameValueList.forEach(process -> {
                SERIES.getData().add(new Data(process.getName(), process.getValue()));
                LEGEND.getItems().add(new LegendItem(process.getName(), new Region()));
            });

            // Set chart SERIES
            chartFX.getData().setAll(SERIES);

            // Set chart LEGEND
            chartFX.setNewLegend(LEGEND);

            // Adjust yAxis upper bound to max value
            Comparator<NameValue> cmp = Comparator.comparing(NameValue::getValue);
            ((NumberAxis) chartFX.getYAxis()).setUpperBound(Collections.max(nameValueList, cmp).getValue() * 1.1);
        });
    }

    @Override public void start(Stage stage) {
        HBox pane = new HBox(chart, chartFX);
        pane.setPadding(new Insets(20));
        pane.setSpacing(20);

        Scene scene = new Scene(pane);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        stage.setTitle("One Color per Series");
        stage.setScene(scene);
        stage.show();

        nameValueList.setAll(new NameValue("Anakin", 20000),
                             new NameValue("Obi-Wan", 18000),
                             new NameValue("Luke", 17000),
                             new NameValue("Leia", 10000),
                             new NameValue("Kylo Ren", 15000));
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }


    // ******************** Internal Classes **********************************
    public class Record {
        private StringProperty name;
        private DoubleProperty value;


        // ******************** Constructors **********************************
        public Record(final String NAME, final double VALUE) {
            name = new StringPropertyBase(NAME) {
                @Override public Object getBean() { return Record.this; }
                @Override public String getName() { return "name"; }
            };
            value = new DoublePropertyBase(VALUE) {
                @Override public Object getBean() { return Record.this; }
                @Override public String getName() { return "value"; }
            };
        }


        // ******************** Methods ***************************************
        public String getName() { return name.get(); }
        public void setName(final String NAME) { name.set(NAME); }
        public StringProperty nameProperty() { return name; }

        public double getValue() { return value.get(); }
        public void setValue(final double VALUE) { value.set(VALUE); }
        public DoubleProperty valueProperty() { return value; }
    }

    public class ChartData {
        private ObservableList<Record>                       records;
        private ObservableList<XYChart.Data<String, Number>> data;


        // ******************** Constructors **********************************
        public ChartData() {
            this(new Record[]{});
        }
        public ChartData(final Record... RECORDS) {
            records = FXCollections.observableArrayList();
            data    = FXCollections.observableArrayList();
            Arrays.stream(RECORDS).forEach(r -> addRecord(r));
        }


        // ******************** Methods ***************************************
        public ObservableList<Record> getRecords() { return records; }
        public ObservableList<XYChart.Data<String, Number>> getData() { return data; }

        public void addRecord(final Record RECORD) {
            if (records.contains(RECORD)) return;
            records.add(RECORD);
            Data<String, Number> d = new Data();
            d.XValueProperty().bind(RECORD.nameProperty());
            d.YValueProperty().bind(RECORD.valueProperty());
            data.add(d);
        }
        public void removeRecord(final Record RECORD) {
            int i = records.indexOf(RECORD);
            if (i > -1) {
                for (Data<String, Number> d : data) {
                    if (d.getXValue().equals(RECORD.getName()) && Double.compare(RECORD.getValue(), (Double) d.getYValue()) == 0) {
                        d.XValueProperty().unbind();
                        d.YValueProperty().unbind();
                        data.remove(d);
                        break;
                    }
                }
                records.remove(i);
            }
        }
        public void updateRecord(final Record RECORD, final double VALUE) {
            int i = records.indexOf(RECORD);
            if (i > -1) records.get(i).setValue(VALUE);
        }
    }

    private class NameValue {
        private StringProperty name;
        private DoubleProperty value;

        public NameValue(final String NAME, final double VALUE) {
            name  = new StringPropertyBase(NAME) {
                @Override public Object getBean() { return NameValue.this; }
                @Override public String getName() { return "name"; }
            };
            value = new DoublePropertyBase(VALUE) {
                @Override public Object getBean() { return NameValue.this; }
                @Override public String getName() { return "value"; }
            };
        }

        public String getName() { return name.get(); }
        public void setName(final String NAME) { name.set(NAME); }
        public StringProperty nameProperty() { return name; }

        public double getValue() { return value.get(); }
        public void setValue(final double VALUE) { value.set(VALUE); }
        public DoubleProperty valueProperty() { return value; }
    }
}
