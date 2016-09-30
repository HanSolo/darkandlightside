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
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;


/**
 * User: hansolo
 * Date: 01.07.16
 * Time: 12:16
 */
public class DemoEffectChaining extends Application {
    private Circle circle;

    @Override public void init() {
        circle = new Circle(100);
        circle.getStyleClass().add("circle");

        InnerShadow innerShadow = new InnerShadow(BlurType.TWO_PASS_BOX, Color.WHITE, 30, 0.0, 0, 5);
        DropShadow  dropShadow  = new DropShadow(BlurType.TWO_PASS_BOX, Color.BLACK, 10, 0.0, 0, 10);
        dropShadow.setInput(innerShadow);

        circle.setEffect(dropShadow);
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(circle);
        pane.setPadding(new Insets(20));

        Scene scene = new Scene(pane);
        scene.getStylesheets().add(getClass().getResource("effect-chaining.css").toExternalForm());

        stage.setTitle("Demo Effect Chaining");
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
