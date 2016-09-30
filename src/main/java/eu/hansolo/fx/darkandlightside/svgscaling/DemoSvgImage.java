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

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


/**
 * Created by hansolo on 13.07.16.
 */
public class DemoSvgImage extends Application {
    private ImageView svgImageView;

    @Override public void init() {
        SvgImageLoaderFactory.install();
        svgImageView = new ImageView(new Image(DemoSvgImage.class.getResourceAsStream("MillenniumFalcon.svg")));
        svgImageView.setPreserveRatio(true);
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(svgImageView);
        pane.setPrefSize(450, 450);
        pane.setPadding(new Insets(20));

        Scene scene = new Scene(pane);

        stage.setTitle("Demo SVGImage");
        stage.setScene(scene);
        stage.show();

        svgImageView.setFitWidth(200);
    }

    private void setPrefSize(final Node NODE, final double TARGET_WIDTH, final double TARGET_HEIGHT) {
        NODE.setScaleX(TARGET_WIDTH / NODE.getLayoutBounds().getWidth());
        NODE.setScaleY(TARGET_HEIGHT / NODE.getLayoutBounds().getHeight());
    }

    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
