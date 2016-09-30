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
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Created by hansolo on 07.06.16.
 */
public class BarChartFX<X, Y> extends BarChart<X, Y> {
    private Map<Node, BarData> nodeMap = new HashMap<>();


    // ******************** Constructors **********************************
    public BarChartFX() {
        this((Axis<X>) new CategoryAxis(), (Axis<Y>) new NumberAxis());
    }
    public BarChartFX(final Axis<X> X_AXIS, final Axis<Y> Y_AXIS) {
        super(X_AXIS, Y_AXIS);
        nodeMap = new HashMap<>(16);
    }
    public BarChartFX(final Axis<X> X_AXIS, final Axis<Y> Y_AXIS, final Legend LEGEND) {
        this(X_AXIS, Y_AXIS);
        setLegend(LEGEND);
    }
    public BarChartFX(final Axis<X> X_AXIS, final Axis<Y> Y_AXIS, final ObservableList<Series<X,Y>> DATA, final Legend LEGEND) {
        this(X_AXIS, Y_AXIS);
        setData(DATA);
        setLegend(LEGEND);
    }


    // ******************** Methods ***************************************
    @Override protected void seriesAdded(final Series<X, Y> SERIES, final int INDEX) {
        super.seriesAdded(SERIES, INDEX);
        for (int j = 0; j < SERIES.getData().size(); j++) {
            final Data<X, Y> ITEM     = SERIES.getData().get(j);
            final BarData    BAR_DATA = new BarData(ITEM.getXValue().toString(), ITEM.getYValue());
            nodeMap.put(ITEM.getNode(), BAR_DATA);
            getPlotChildren().add(BAR_DATA.getValueText());
        }
    }

    @Override protected void seriesRemoved(final Series<X, Y> SERIES) {
        for (Node bar : nodeMap.keySet()) {
            final BarData BAR_DATA = nodeMap.get(bar);
            getPlotChildren().remove(BAR_DATA.getValueText());
        }
        nodeMap.clear();
        super.seriesRemoved(SERIES);
    }

    public void setNewLegend(final Legend LEGEND) {
        int length = LEGEND.getItems().size();
        int count  = 0;
        if (length == nodeMap.size()) {
            for (Node bar : nodeMap.keySet()) {
                String barName = nodeMap.get(bar).getName();
                Optional<LegendItem> opt = LEGEND.getItems().stream().filter(item -> item.getText().trim().equals(barName.trim())).findFirst();
                if (opt.isPresent()) {
                    opt.get().getSymbol().getStyleClass().setAll("chart-legend-item-symbol", "chart-bar", "bar-legend-symbol");
                    opt.get().getSymbol().getStyleClass().addAll("series" + count, "default-color" + count);
                    count++;
                    if (count > 19) count = 0;
                }
            }
            super.setLegend(LEGEND);
        }
    }

    @Override protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        int count = 0;
        for (Node bar : nodeMap.keySet()) {
            Node text = nodeMap.get(bar).getValueText();
            bar.getStyleClass().setAll("chart-bar", "series" + count, "data" + count, "default-color" + count);
            Bounds bounds = bar.getBoundsInParent();
            text.setLayoutX(Math.round(bounds.getMinX() + bounds.getWidth() / 2 - text.prefWidth(-1) / 2));
            text.setLayoutY(Math.round(bounds.getMinY() - text.prefHeight(-1) * 0.5));
            count++;
            if (count > 19) count = 0;
        }
    }


    // ******************** Inner Classes *************************************
    private class BarData {
        private String name;
        private Y      value;
        private Text   valueText;


        // ******************** Constructors **********************************
        public BarData() { this("", null); }
        public BarData(final String NAME, final Y VALUE) {
            name  = NAME;
            value = VALUE;
            valueText = new Text(String.valueOf(VALUE));
        }


        // ******************** Methods ***************************************
        public String getName() { return name; }
        public void setName(final String NAME) { name = NAME; }

        public Y getValue() { return value; }
        public void setValue(final Y VALUE) {
            value = VALUE;
            valueText.setText(null == VALUE ? "" : String.valueOf(VALUE));
        }

        public Text getValueText() { return valueText; }
    }
}