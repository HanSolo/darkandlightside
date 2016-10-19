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

package eu.hansolo.fx.darkandlightside.badperformance;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;



/**
 * User: hansolo
 * Date: 06.09.16
 * Time: 13:49
 */
public class DemoEffects extends Application {
    private Group shadowGroupSeparated;
    private Group shadowGroupOverlapped;

    @Override public void init() {
        DropShadow dropShadow = new DropShadow(BlurType.TWO_PASS_BOX, Color.MAGENTA, 50, 0, 0, 0);

        shadowGroupSeparated = new Group();
        for (int i = 0 ; i < 10 ; i++) {
            Circle circle = new Circle(i * 130, 50, 50, Color.ORANGE);
            circle.setStroke(Color.ORANGE.darker());
            shadowGroupSeparated.getChildren().add(circle);
        }
        shadowGroupSeparated.setEffect(dropShadow);

        shadowGroupOverlapped = new Group();
        shadowGroupOverlapped.setVisible(false);
        for (int i = 0 ; i < 10 ; i++) {
            Circle circle = new Circle(i * 70, 50, 50, Color.ORANGE);
            circle.setStroke(Color.ORANGE.darker());
            shadowGroupOverlapped.getChildren().add(circle);
        }
        shadowGroupOverlapped.setEffect(dropShadow);
    }

    @Override public void start(Stage stage) {
        VBox pane = new VBox(shadowGroupSeparated, shadowGroupOverlapped);
        pane.setSpacing(40);
        pane.setPadding(new Insets(40));
        pane.setBackground(new Background(new BackgroundFill(Color.web("#525453"), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(pane);
        scene.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                shadowGroupOverlapped.setVisible(true);
            }
        });

        stage.setTitle("Demo Effects");
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
