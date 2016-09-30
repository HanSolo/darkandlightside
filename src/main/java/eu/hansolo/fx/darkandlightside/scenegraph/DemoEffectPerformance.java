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

package eu.hansolo.fx.darkandlightside.scenegraph;


import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.CacheHint;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.util.Random;


/**
 * User: hansolo
 * Date: 30.06.16
 * Time: 13:30
 */
public class DemoEffectPerformance extends Application {
    private static final Random         RND         = new Random();
    private static final Color[]        COLORS      = { Color.RED, Color.YELLOW, Color.CYAN, Color.BLUE, Color.LIME, Color.MAGENTA };
    private static final int            NO_OF_NODES = 10000;
    private static final Circle[]       CIRCLES     = new Circle[NO_OF_NODES];
    private static final DropShadow     EFFECT      = new DropShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.65), 3, 0, 0, 0);
    private              Pane           pane;
    private              AnimationTimer timer;


    @Override public void init() {
        for (int i = 0 ; i < NO_OF_NODES ; i++) {
            double x      = RND.nextInt(50) * 16 + 8;
            double y      = RND.nextInt(50) * 16 + 8;
            Color  color  = COLORS[RND.nextInt(5)];
            Circle circle = new Circle(x, y, 5, color);

            /***** WRONG APPROACH *****/
            //circle.setEffect(EFFECT);

            CIRCLES[i] = circle;
        }
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                move();
            }
        };

        pane = new Pane(CIRCLES);

        /***** CORRECT APPROACH *****/
        pane.setEffect(EFFECT);
    }

    @Override public void start(Stage stage) {
        Scene scene = new Scene(pane, 800, 800);

        stage.setTitle("Demo Effect Performance");
        stage.setScene(scene);
        stage.show();

        timer.start();
    }

    @Override public void stop() {
        System.exit(0);
    }

    private void move() {
        for (Circle circle : CIRCLES) {
            circle.setCenterX(RND.nextInt(50) * 16 + 8);
            circle.setCenterY(RND.nextInt(50) * 16 + 8);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
