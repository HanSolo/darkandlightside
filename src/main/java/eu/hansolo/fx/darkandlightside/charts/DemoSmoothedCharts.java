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

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;


/**
 * User: hansolo
 * Date: 25.07.16
 * Time: 12:00
 */
public class DemoSmoothedCharts extends Application {
    private DateTimeFormatter    DF = DateTimeFormatter.ofPattern("dd.MM.YY");
    private AreaChart            chart;
    private CurveFittedAreaChart smoothedChart;
    private ToggleButton         toggleButton;

    @Override public void init() {
        XYChart.Series<String, Number> series         = new XYChart.Series<>();
        XYChart.Series<String, Number> smoothedSeries = new XYChart.Series<>();
        smoothedSeries.setName("Steps");
        Random rnd = new Random();
        for (int i = 0 ; i < 20 ; i++) {
            String category = DF.format(LocalDate.now().minusDays(i));
            int    value    = rnd.nextInt(3000) + 1000;
            series.getData().add(i, new Data(category, value));
            smoothedSeries.getData().add(i, new Data(category, value));
        }

        // Standard chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setAutoRanging(true);
        xAxis.setAnimated(false);
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(true);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(15000);
        yAxis.setTickUnit(1000);
        yAxis.setMinorTickVisible(false);
        yAxis.setAnimated(true);

        chart = new AreaChart(xAxis, yAxis);
        chart.setTitle("Steps");
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setHorizontalGridLinesVisible(true);
        chart.getData().add(series);
        chart.setPrefSize(800, 400);

        // Smoothed Chart
        CategoryAxis smoothedXAxis = new CategoryAxis();
        smoothedXAxis.setAutoRanging(true);
        smoothedXAxis.setAnimated(false);
        smoothedXAxis.setTickLabelsVisible(true);
        smoothedXAxis.setTickMarkVisible(true);

        NumberAxis smoothedYAxis = new NumberAxis();
        smoothedYAxis.setAutoRanging(true);
        smoothedYAxis.setLowerBound(0);
        smoothedYAxis.setUpperBound(15000);
        smoothedYAxis.setTickUnit(1000);
        smoothedYAxis.setMinorTickVisible(false);
        smoothedYAxis.setAnimated(true);

        smoothedChart = new CurveFittedAreaChart(smoothedXAxis, smoothedYAxis);
        smoothedChart.setTitle("Steps");
        smoothedChart.setAnimated(false);
        smoothedChart.setLegendVisible(false);
        smoothedChart.setHorizontalGridLinesVisible(true);
        smoothedChart.getData().add(smoothedSeries);
        smoothedChart.setPrefSize(800, 400);

        // ToggleButton
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), chart);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        FadeTransition fadeIn  = new FadeTransition(Duration.millis(500), chart);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        toggleButton = new ToggleButton("Standard");
        toggleButton.setOnMousePressed(e -> {
            if (toggleButton.isSelected()) {
                fadeIn.play();
                toggleButton.setText("Standard");
            } else {
                fadeOut.play();
                toggleButton.setText("Smoothed");
            }
        });
    }

    @Override public void start(Stage stage) {
        StackPane chartPane = new StackPane(smoothedChart, chart);

        VBox pane = new VBox(chartPane, toggleButton);
        pane.setSpacing(10);
        pane.setAlignment(Pos.CENTER);
        pane.setPadding(new Insets(10));

        Scene scene = new Scene(pane);

        stage.setTitle("Demo smoothed charts");
        stage.setScene(scene);
        stage.show();
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
