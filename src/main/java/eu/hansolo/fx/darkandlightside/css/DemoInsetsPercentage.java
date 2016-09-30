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

package eu.hansolo.fx.darkandlightside.css;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;


/**
 * User: hansolo
 * Date: 01.07.16
 * Time: 12:09
 */
public class DemoInsetsPercentage extends Application {
    private Region donut;

    @Override public void init() {
        donut = new Region();
        donut.setPrefSize(200, 200);
        donut.getStyleClass().add("donut");
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(donut);
        pane.setPadding(new Insets(20));

        Scene scene = new Scene(pane);
        scene.getStylesheets().add(getClass().getResource("insets.css").toExternalForm());

        stage.setTitle("Demo Insets Percentage");
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
