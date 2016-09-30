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

package eu.hansolo.fx.darkandlightside.canvas;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;

import java.util.Random;


/**
 * User: hansolo
 * Date: 04.07.16
 * Time: 18:20
 */
public class DemoCanvasVsNodes extends Application {
    private static final Random RND       = new Random();
    private static       int    noOfNodes = 0;
    private Canvas         canvas;
    private AnimationTimer timer;

    private Pane           circlesPane;
    private Circle[]       circles;


    @Override public void init() {
        // ******************** Nodes *****************************************
        circlesPane = new Pane();
        circlesPane.setPrefSize(500, 500);
        circles = new Circle[250000];
        int counter = 0;
        for (int y = 0 ; y < 500 ; y++) {
            for (int x = 0 ; x < 500 ; x++) {
                circles[counter] = new Circle(x, y, 1);
                counter++;
            }
        }
        circlesPane.getChildren().addAll(circles);

        counter = 0;
        for (int y = 0 ; y < 500 ; y++) {
            for (int x = 0 ; x < 500 ; x++) {
                circles[counter].setFill(Color.rgb(RND.nextInt(255), RND.nextInt(255), RND.nextInt(255)));
                counter++;
            }
        }

        // ******************** Canvas *****************************************
        /*
        canvas              = new Canvas(500, 500);
        GraphicsContext ctx = canvas.getGraphicsContext2D();

        for (int y = 0 ; y < 500 ; y++) {
            for (int x = 0 ; x < 500 ; x++) {
                ctx.setFill(Color.rgb(RND.nextInt(255), RND.nextInt(255), RND.nextInt(255)));
                ctx.fillOval(x, y, 1, 1);
            }
        }
        */

        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                int counter = 0;

                // Nodes
                for (int y = 0; y < 500; y++) {
                    for (int x = 0; x < 500; x++) {
                        circles[counter].setFill(Color.rgb(RND.nextInt(255), RND.nextInt(255), RND.nextInt(255)));
                        counter++;
                    }
                }

                /* Canvas
                ctx.clearRect(0, 0, 500, 500);
                for (int y = 0; y < 500; y++) {
                    for (int x = 0; x < 500; x++) {
                        ctx.setFill(Color.rgb(RND.nextInt(255), RND.nextInt(255), RND.nextInt(255)));
                        ctx.fillOval(x, y, 1, 1);
                    }
                }
                */
            }
        };
    }

    @Override public void start(Stage stage) {
        //StackPane pane = new StackPane(canvas);
        StackPane pane = new StackPane(circlesPane);
        pane.setPadding(new Insets(20));

        Scene scene = new Scene(pane);

        stage.setTitle("Demo Canvas vs Nodes");
        stage.setScene(scene);
        stage.show();

        // Calculate number of nodes
        calcNoOfNodes(pane);
        System.out.println(noOfNodes + " Nodes in SceneGraph");

        timer.start();
    }

    @Override public void stop() {
        System.exit(0);
    }


    // ******************** Misc **********************************************
    private static void calcNoOfNodes(Node node) {
        if (node instanceof Parent) {
            if (((Parent) node).getChildrenUnmodifiable().size() != 0) {
                ObservableList<Node> tempChildren = ((Parent) node).getChildrenUnmodifiable();
                noOfNodes += tempChildren.size();
                for (Node n : tempChildren) { calcNoOfNodes(n); }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
