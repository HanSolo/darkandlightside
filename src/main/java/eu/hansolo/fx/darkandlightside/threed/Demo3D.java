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

package eu.hansolo.fx.darkandlightside.threed;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.col.ColModelImporter;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.util.Duration;

import java.net.URL;


/**
 * User: hansolo
 * Date: 04.09.16
 * Time: 11:09
 */
public class Demo3D extends Application {
    private ImageView          background;
    private Group              spaceShip1;
    private ParallelTransition transition;


    @Override public void init() {
        background = new ImageView(new Image(this.getClass().getResourceAsStream("starfield_background.jpg")));

        spaceShip1 = get3DFile("tie-fighter.dae");
        spaceShip1.getTransforms().add(new Rotate(10, Rotate.X_AXIS));
        spaceShip1.getTransforms().add(new Rotate(-20, Rotate.Y_AXIS));

        TranslateTransition translate = new TranslateTransition(Duration.millis(3000), spaceShip1);
        translate.setFromX(-300);
        translate.setFromY(-100);
        translate.setFromZ(0);
        translate.setToX(900);
        translate.setToY(100);
        translate.setToZ(900);
        translate.setInterpolator(Interpolator.LINEAR);

        ScaleTransition scale = new ScaleTransition(Duration.millis(3000), spaceShip1);
        scale.setFromX(0);
        scale.setFromY(0);
        scale.setFromZ(0);
        scale.setToX(60);
        scale.setToY(60);
        scale.setToZ(60);
        scale.setInterpolator(Interpolator.LINEAR);

        transition = new ParallelTransition(spaceShip1, translate, scale);
        transition.setInterpolator(Interpolator.LINEAR);
        transition.setCycleCount(Animation.INDEFINITE);
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(background, spaceShip1);
        pane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        pane.setPadding(new Insets(20));

        Scene scene = new Scene(pane, 600, 600);
        scene.setCamera(new PerspectiveCamera());

        stage.setTitle("JavaFX 3D Demo");
        stage.setScene(scene);
        stage.show();

        transition.play();
    }

    @Override public void stop() {
        System.exit(0);
    }

    private Group get3DFile(final String DAE_FILE_NAME) {
        final ColModelImporter COLLADA_IMPORTER = new ColModelImporter();
        try {
            final URL COLLADA_FILE_URL = this.getClass().getResource(DAE_FILE_NAME);
            COLLADA_IMPORTER.read(COLLADA_FILE_URL);
        } catch (ImportException e) {
            return new Group();
        }
        final Node[] NODES = COLLADA_IMPORTER.getImport();

        return new Group(NODES);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
