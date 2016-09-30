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

package eu.hansolo.fx.darkandlightside.svgscaling;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


/**
 * Created by hansolo on 05.07.16.
 */
public class DemoSvgPathAlternative extends Application {
    private StackPane pane;
    private Region    svgRegion;

    @Override public void init() {
        svgRegion = new Region();
        svgRegion.getStyleClass().add("falcon");
        svgRegion.widthProperty().addListener(e -> relocate());
        svgRegion.heightProperty().addListener(e -> relocate());
    }

    @Override public void start(Stage stage) {
        pane = new StackPane(svgRegion);
        pane.setPadding(new Insets(20));

        Scene scene = new Scene(pane);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        stage.setTitle("Demo SVGPath Alternative");
        stage.setScene(scene);
        stage.show();

        // Correct approach
        svgRegion.setPrefSize(150, 205);

        // Correct approach
        //svgRegion.resize(150, 205);
    }

    private void relocate() {
        svgRegion.relocate((pane.getPrefWidth() - svgRegion.getLayoutBounds().getWidth()) * 0.5,
                           (pane.getPrefHeight() - svgRegion.getLayoutBounds().getHeight()) * 0.5);
    }

    private void resize() {
        double aspectRatio = 300/410;

    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
