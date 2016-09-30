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

import eu.hansolo.fx.darkandlightside.tools.Fonts;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.List;


/**
 * Created by hansolo on 19.11.14.
 */
public class CurveFittedAreaChart extends AreaChart<String, Number> {
    private DoubleProperty       selectedYValue;
    private Line                 selector;
    private Region               chartPlotBackground;
    private double               lowerBound;
    private double               upperBound;
    private double               range;
    private Text                 valueText;
    private Timeline             timeline;


    public CurveFittedAreaChart(CategoryAxis xAxis, NumberAxis yAxis) {
        super(xAxis, yAxis);

        selectedYValue = new SimpleDoubleProperty(this, "selectedYValue", 0);

        // Add selector line to chart
        selector = new Line();
        selector.setStroke(Color.CYAN);
        selector.setStroke(Color.web("#2468ea"));
        selector.setOpacity(0);
        selector.setOnMouseDragged(event -> dragSelector(event));
        selector.setOnMouseReleased(event -> selectDataAfterDrag(event));

        chartPlotBackground = getChartBackground();
        chartPlotBackground.widthProperty().addListener(o -> resizeSelector(chartPlotBackground));
        chartPlotBackground.heightProperty().addListener(o -> resizeSelector(chartPlotBackground));
        chartPlotBackground.layoutYProperty().addListener(o -> resizeSelector(chartPlotBackground));

        setOnMouseClicked(event -> selectDataOnClick(event));
        setOnMouseReleased(event -> setSelector(event));

        valueText = new Text("");
        valueText.setTextAlignment(TextAlignment.CENTER);
        valueText.setFill(Color.WHITE);
        valueText.setOpacity(0);
        valueText.setFont(Fonts.latoLight(12));

        getChartChildren().addAll(selector, valueText);

        lowerBound = ((NumberAxis) getYAxis()).getLowerBound();
        upperBound = ((NumberAxis) getYAxis()).getUpperBound();
        range      = upperBound - lowerBound;

        KeyValue kvSelectorStart  = new KeyValue(selector.opacityProperty(), 1, Interpolator.EASE_BOTH);
        KeyValue kvSelectorWait   = new KeyValue(selector.opacityProperty(), 1, Interpolator.EASE_BOTH);
        KeyValue kvSelectorStop   = new KeyValue(selector.opacityProperty(), 0, Interpolator.EASE_BOTH);

        KeyValue kvValueTextStart = new KeyValue(valueText.opacityProperty(), 1, Interpolator.EASE_BOTH);
        KeyValue kvValueTextWait  = new KeyValue(valueText.opacityProperty(), 1, Interpolator.EASE_BOTH);
        KeyValue kvValueTextStop  = new KeyValue(valueText.opacityProperty(), 0, Interpolator.EASE_BOTH);

        KeyFrame kfStart          = new KeyFrame(Duration.ZERO, kvSelectorStart, kvValueTextStart);
        KeyFrame kfWait           = new KeyFrame(Duration.millis(2500), kvSelectorWait, kvValueTextWait);
        KeyFrame kfStop           = new KeyFrame(Duration.millis(3000), kvSelectorStop, kvValueTextStop);
        timeline = new Timeline(kfStart, kfWait, kfStop);

        valueText.textProperty().bind(selectedYValue.asString("%.0f"));
    }

    @Override protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        for (int seriesIndex = 0; seriesIndex < getDataSize(); seriesIndex++) {
            final XYChart.Series<String, Number> series = getData().get(seriesIndex);
            final Path seriesLine = (Path) ((Group) series.getNode()).getChildren().get(1);
            final Path fillPath   = (Path) ((Group) series.getNode()).getChildren().get(0);
            smooth(seriesLine.getElements(), fillPath.getElements());
        }
    }

    private int getDataSize() {
        final ObservableList<Series<String, Number>> data = getData();
        return (data != null) ? data.size() : 0;
    }

    private static void smooth(ObservableList<PathElement> strokeElements, ObservableList<PathElement> fillElements) {
        if (fillElements.isEmpty()) return;
        // as we do not have direct access to the data, first recreate the list of all the data points we have
        final Point2D[] dataPoints = new Point2D[strokeElements.size()];
        for (int i = 0; i < strokeElements.size(); i++) {
            final PathElement element = strokeElements.get(i);
            if (element instanceof MoveTo) {
                final MoveTo move = (MoveTo) element;
                dataPoints[i] = new Point2D(move.getX(), move.getY());
            } else if (element instanceof LineTo) {
                final LineTo line = (LineTo) element;
                final double x = line.getX(), y = line.getY();
                dataPoints[i] = new Point2D(x, y);
            }
        }
        // next we need to know the zero Y value
        final double zeroY = ((MoveTo) fillElements.get(0)).getY();
        // now clear and rebuild elements
        strokeElements.clear();
        fillElements.clear();
        Pair<Point2D[], Point2D[]> result = calcCurveControlPoints(dataPoints);
        Point2D[] firstControlPoints  = result.getKey();
        Point2D[] secondControlPoints = result.getValue();
        // start both paths
        strokeElements.add(new MoveTo(dataPoints[0].getX(), dataPoints[0].getY()));
        fillElements.add(new MoveTo(dataPoints[0].getX(), zeroY));
        fillElements.add(new LineTo(dataPoints[0].getX(), dataPoints[0].getY()));
        // add curves
        for (int i = 1; i < dataPoints.length; i++) {
            final int ci = i - 1;
            strokeElements.add(new CubicCurveTo(
                firstControlPoints[ci].getX(), firstControlPoints[ci].getY(),
                secondControlPoints[ci].getX(), secondControlPoints[ci].getY(),
                dataPoints[i].getX(), dataPoints[i].getY()));
            fillElements.add(new CubicCurveTo(
                firstControlPoints[ci].getX(), firstControlPoints[ci].getY(),
                secondControlPoints[ci].getX(), secondControlPoints[ci].getY(),
                dataPoints[i].getX(), dataPoints[i].getY()));
        }
        // end the paths
        fillElements.add(new LineTo(dataPoints[dataPoints.length - 1].getX(), zeroY));
        fillElements.add(new ClosePath());
    }

    /**
     * Calculate open-ended Bezier Spline Control Points.
     *
     * @param dataPoints Input data Bezier spline points.
     * @return The spline points
     */
    public static Pair<Point2D[], Point2D[]> calcCurveControlPoints(Point2D[] dataPoints) {
        Point2D[] firstControlPoints;
        Point2D[] secondControlPoints;
        int n = dataPoints.length - 1;
        if (n == 1) { // Special case: Bezier curve should be a straight line.
            firstControlPoints = new Point2D[1];
            // 3P1 = 2P0 + P3
            firstControlPoints[0] = new Point2D((2 * dataPoints[0].getX() + dataPoints[1].getX()) / 3, (2 * dataPoints[0].getY() + dataPoints[1].getY()) / 3);

            secondControlPoints = new Point2D[1];
            // P2 = 2P1 – P0
            secondControlPoints[0] = new Point2D(2 * firstControlPoints[0].getX() - dataPoints[0].getX(), 2 * firstControlPoints[0].getY() - dataPoints[0].getY());
            return new Pair<>(firstControlPoints, secondControlPoints);
        }

        // Calculate first Bezier control points
        // Right hand side vector
        double[] rhs = new double[n];

        // Set right hand side X values
        for (int i = 1; i < n - 1; ++i) {
            rhs[i] = 4 * dataPoints[i].getX() + 2 * dataPoints[i + 1].getX();
        }
        rhs[0]     = dataPoints[0].getX() + 2 * dataPoints[1].getX();
        rhs[n - 1] = (8 * dataPoints[n - 1].getX() + dataPoints[n].getX()) / 2.0;
        // Get first control points X-values
        double[] x = getFirstControlPoints(rhs);

        // Set right hand side Y values
        for (int i = 1; i < n - 1; ++i) {
            rhs[i] = 4 * dataPoints[i].getY() + 2 * dataPoints[i + 1].getY();
        }
        rhs[0]     = dataPoints[0].getY() + 2 * dataPoints[1].getY();
        rhs[n - 1] = (8 * dataPoints[n - 1].getY() + dataPoints[n].getY()) / 2.0;
        // Get first control points Y-values
        double[] y = getFirstControlPoints(rhs);

        // Fill output arrays.
        firstControlPoints  = new Point2D[n];
        secondControlPoints = new Point2D[n];
        for (int i = 0; i < n; ++i) {
            // First control point
            firstControlPoints[i] = new Point2D(x[i], y[i]);
            // Second control point
            if (i < n - 1) {
                secondControlPoints[i] = new Point2D(2 * dataPoints[i + 1].getX() - x[i + 1], 2 * dataPoints[i + 1].getY() - y[i + 1]);
            } else {
                secondControlPoints[i] = new Point2D((dataPoints[n].getX() + x[n - 1]) / 2, (dataPoints[n].getY() + y[n - 1]) / 2);
            }
        }
        return new Pair<>(firstControlPoints, secondControlPoints);
    }

    /**
     * Solves a tridiagonal system for one of coordinates (x or y) of first
     * Bezier control points.
     *
     * @param rhs Right hand side vector.
     * @return Solution vector.
     */
    private static double[] getFirstControlPoints(double[] rhs) {
        int      n   = rhs.length;
        double[] x   = new double[n]; // Solution vector.
        double[] tmp = new double[n]; // Temp workspace.
        double   b   = 2.0;
        x[0] = rhs[0] / b;
        for (int i = 1; i < n; i++) {// Decomposition and forward substitution.
            tmp[i] = 1 / b;
            b = (i < n - 1 ? 4.0 : 3.5) - tmp[i];
            x[i] = (rhs[i] - x[i - 1]) / b;
        }
        for (int i = 1; i < n; i++) {
            x[n - i - 1] -= tmp[n - i] * x[n - i]; // Backsubstitution.
        }
        return x;
    }

    public Region getChartBackground() {
        for (Node node : lookupAll(".chart-plot-background")) {
            if (node instanceof Region) return (Region) node;
        }
        //for (Node node : lookupAll(".chart-series-area-fill series0 default-color0")) {
        //    if (node instanceof Path) return (Path) node;
        //}
        return null;
    }

    private void resizeSelector(final Region CHART_BACKGROUND) {
        selector.setLayoutX(CHART_BACKGROUND.getLayoutX());
        selector.setLayoutY(CHART_BACKGROUND.getLayoutY());
        selector.setStartY(CHART_BACKGROUND.getLayoutBounds().getMinY() + 5);
        selector.setEndY(CHART_BACKGROUND.getLayoutBounds().getMaxY());
    }

    private void setSelector(final MouseEvent EVENT) {
        final double CHART_X     = chartPlotBackground.getLayoutX();
        final double CHART_WIDTH = chartPlotBackground.getLayoutBounds().getWidth();
        final double EVENT_X     = EVENT.getX() - CHART_X;
        selector.setStartX(clamp(0, CHART_WIDTH, EVENT_X));
        selector.setEndX(clamp(0, CHART_WIDTH, EVENT_X));
        valueText.setX(clamp(0, CHART_WIDTH, (selector.getBoundsInParent().getMinX()) - valueText.getLayoutBounds().getWidth() * 0.5));
        valueText.setY(selector.getStartY() + 5);
        timeline.stop();
        valueText.setOpacity(1);
        selector.setOpacity(1);
        timeline.play();
    }
    private void selectDataOnClick(final MouseEvent EVENT) {
        final double CHART_X     = chartPlotBackground.getBoundsInParent().getMinX();
        final double CHART_WIDTH = chartPlotBackground.getBoundsInParent().getWidth();
        final double EVENT_X     = EVENT.getX() - CHART_X;

        if (Double.compare(EVENT_X, CHART_X) < 0 ||
            Double.compare(EVENT_X, CHART_WIDTH) > 0) {
            return;
        }

        lowerBound = ((NumberAxis) getYAxis()).getLowerBound();
        upperBound = ((NumberAxis) getYAxis()).getUpperBound();
        range      = upperBound - lowerBound;

        Series<String,Number> series = getData().get(0);
        if (series.getData().isEmpty()) return;

        List<Data<String, Number>> data = series.getData();
        double lx = data.get(0).getNode().getLayoutX();
        double ux = data.get(data.size() - 1).getNode().getLayoutX();

        if (selector.getStartX() < lx || selector.getStartX() > ux) return;

        final Path seriesLine = (Path) ((Group) series.getNode()).getChildren().get(1);
        double x0 = 0;
        double y0 = 0;
        double x1 = 0;
        double y1 = 0;
        double x2 = 0;
        double y2 = 0;
        double x3 = 0;
        double y3 = 0;
        double x = selector.getStartX(); // x coordinate of selected point
        for (PathElement element : seriesLine.getElements()) {
            if (element instanceof MoveTo) {
                final MoveTo moveTo = (MoveTo) element;
                x0 = moveTo.getX();
                y0 = moveTo.getY();
            } else if (element instanceof CubicCurveTo) {
                final CubicCurveTo cubicCurveTo = (CubicCurveTo) element;
                x1 = cubicCurveTo.getControlX1();
                y1 = cubicCurveTo.getControlY1();
                x2 = cubicCurveTo.getControlX2();
                y2 = cubicCurveTo.getControlY2();
                x3 = cubicCurveTo.getX();
                y3 = cubicCurveTo.getY();

                if (x > x0 && x < x3) break;

                x0 = cubicCurveTo.getX();
                y0 = cubicCurveTo.getY();
            }
        }

        //double cx = 3.0 * (x1 - x0);
        //double bx = 3.0 * (x2 - x1) - cx;
        //double ax = x3 - x0 - cx - bx;
        double cy = 3.0 * (y1 - y0);
        double by = 3.0 * (y2 - y1) - cy;
        double ay = y3 - y0 - cy - by;


        double a0 = x0;
        double a1 = 3.0 * (x1 - x0);
        double a2 = 3.0 * (x2 - 2.0 * x1 + x0);
        double a3 = x3 - 3.0 * x2 + 3.0 * x1 - x0;
        double t = invB3P(a0, a1, a2, a3, x);
        double yt = ay * t * t * t + by * t * t + cy * t + y0;

        double factor = range / getYAxis().getLayoutBounds().getHeight();

        double selectedValue = ((getYAxis().getLayoutBounds().getHeight() - yt) * factor + lowerBound);

        selectedYValue.set(selectedValue);
    }

    private void dragSelector(final MouseEvent EVENT) {
        final double EVENT_X     = EVENT.getX();
        final double CHART_X     = chartPlotBackground.getLayoutX();
        final double CHART_WIDTH = chartPlotBackground.getLayoutBounds().getWidth();
        selector.setStartX(clamp(0, CHART_WIDTH, EVENT_X));
        selector.setEndX(clamp(0, CHART_WIDTH, EVENT_X));
        valueText.setX(clamp(0, CHART_WIDTH, (selector.getBoundsInParent().getMinX()) - valueText.getLayoutBounds().getWidth() * 0.5));
        valueText.setY(selector.getStartY() + 5);
        timeline.stop();
        valueText.setOpacity(1);
        selector.setOpacity(1);
        timeline.play();
    }
    private void selectDataAfterDrag(final MouseEvent EVENT) {
        final double EVENT_X     = EVENT.getX();
        final double CHART_X     = chartPlotBackground.getBoundsInParent().getMinX();
        final double CHART_WIDTH = chartPlotBackground.getBoundsInParent().getWidth();

        if (Double.compare(EVENT_X, CHART_X) < 0 ||
            Double.compare(EVENT_X, CHART_WIDTH) > 0) {
            return;
        }

        lowerBound = ((NumberAxis) getYAxis()).getLowerBound();
        upperBound = ((NumberAxis) getYAxis()).getUpperBound();
        range      = upperBound - lowerBound;

        Series<String,Number> series = getData().get(0);
        if (series.getData().isEmpty()) return;

        List<Data<String, Number>> data = series.getData();
        double lx = data.get(0).getNode().getLayoutX();
        double ux = data.get(data.size() - 1).getNode().getLayoutX();

        if (selector.getStartX() < lx || selector.getStartX() > ux) return;

        final Path seriesLine = (Path) ((Group) series.getNode()).getChildren().get(1);
        double x0 = 0;
        double y0 = 0;
        double x1 = 0;
        double y1 = 0;
        double x2 = 0;
        double y2 = 0;
        double x3 = 0;
        double y3 = 0;
        double x = selector.getStartX(); // x coordinate of selected point
        for (PathElement element : seriesLine.getElements()) {
            if (element instanceof MoveTo) {
                final MoveTo moveTo = (MoveTo) element;
                x0 = moveTo.getX();
                y0 = moveTo.getY();
            } else if (element instanceof CubicCurveTo) {
                final CubicCurveTo cubicCurveTo = (CubicCurveTo) element;
                x1 = cubicCurveTo.getControlX1();
                y1 = cubicCurveTo.getControlY1();
                x2 = cubicCurveTo.getControlX2();
                y2 = cubicCurveTo.getControlY2();
                x3 = cubicCurveTo.getX();
                y3 = cubicCurveTo.getY();

                if (x > x0 && x < x3) break;

                x0 = cubicCurveTo.getX();
                y0 = cubicCurveTo.getY();
            }
        }

        //double cx = 3.0 * (x1 - x0);
        //double bx = 3.0 * (x2 - x1) - cx;
        //double ax = x3 - x0 - cx - bx;
        double cy = 3.0 * (y1 - y0);
        double by = 3.0 * (y2 - y1) - cy;
        double ay = y3 - y0 - cy - by;


        double a0 = x0;
        double a1 = 3.0 * (x1 - x0);
        double a2 = 3.0 * (x2 - 2.0 * x1 + x0);
        double a3 = x3 - 3.0 * x2 + 3.0 * x1 - x0;
        double t = invB3P(a0, a1, a2, a3, x);
        double yt = ay * t * t * t + by * t * t + cy * t + y0;

        double factor = range / getYAxis().getLayoutBounds().getHeight();

        double selectedValue = ((getYAxis().getLayoutBounds().getHeight() - yt) * factor + lowerBound);
        selectedYValue.set(selectedValue);
    }

    public double getSelectedYValue() { return selectedYValue.get(); }
    public ReadOnlyDoubleProperty selectedYValueProperty() { return selectedYValue; }

    private double invB3P(double a0, double a1, double a2, double a3, double x) {
        double c;
        double h, p, q, D, R, S, F, t;
        double w1 = 2.0 * Math.PI / 3.0;
        double w2 = 4.0 * Math.PI / 3.0;

        c = 1.0 + a3;

        if (Double.compare(c, 1.0) == 0) {
            a3 = 1e-6;
        }
        h = a2 / 3.0 / a3;
        p  = (3.0 * a1 * a3 - a2 * a2) / 3.0 / a3 / a3;
        q  = (2.0 * a2 * a2 * a2 - 9.0 * a1 * a2 * a3 - 27.0 * a3 * a3 * (x - a0)) / 27.0 / a3 / a3 / a3;

        c  = (1.0 + p);        /* Check for p being too near to zero     */
        if (Double.compare(c, 1.0) == 0) {
            c = 1.0 + q;      /* Check for q being too near to zero     */
            if (Double.compare(c, 1.0)  == 0) {
                return( (float)(-h) );
            }

            t = -Math.exp(Math.log(Math.abs(q)) / 3.0);
            if (q < 0.0) {
                t = -t;
            }
            t -= h;
            return t;
        }

        R  = Math.sqrt(Math.abs(p) / 3.0);
        S  = Math.abs(q) / 2.0 / R / R / R;

        R  = -2.0 * R;
        if (q < 0.0) {
            R = -R;
        }

        if (p < 0.0) {
            D = p * p * p / 27.0 + q * q / 4.0;
            if (D <= 0.0) {
                F = Math.acos(S)/3.0;
                t = R * Math.cos(F + w2) - h;
                if ((t < -0.00005) || (t > 1.00005)) {
                    t = R * Math.cos(F + w1) - h;
                    if ((t < -0.00005) || (t > 1.00005)) {
                        t = R * Math.cos(F) - h;
                        t = clamp(-0.00005, 1.00005, t);
                    }
                }
            }
            else {
                t = R * Math.cosh(Math.log(S + Math.sqrt((S + 1.0) * (S - 1.0))) / 3.0) - h;  /* arcosh */
            }
        }
        else {
            t = R * Math.sinh(Math.log(S + Math.sqrt(S * S + 1.0)) / 3.0) - h;               /* arsinh */
        }

        return t;
    }

    private double clamp(final double MIN, final double MAX, final double VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }
}