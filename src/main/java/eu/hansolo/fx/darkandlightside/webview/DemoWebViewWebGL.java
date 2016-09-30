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

package eu.hansolo.fx.darkandlightside.webview;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import netscape.javascript.JSObject;

import java.net.URL;


/**
 * User: hansolo
 * Date: 04.07.16
 * Time: 17:51
 */
public class DemoWebViewWebGL extends Application {
    private WebView webView;

    @Override public void init() {}

    private void initOnFxApplicationThread() {
        webView = new WebView();

        WebEngine webEngine = webView.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener((ov, o, n) -> {
            if (Worker.State.SUCCEEDED == n) {}
        });
        webEngine.load("http://madebyevan.com/webgl-water/");
    }

    @Override public void start(Stage stage) {
        initOnFxApplicationThread();

        StackPane pane = new StackPane(webView);
        pane.setPadding(new Insets(20));

        Scene scene = new Scene(pane);

        stage.setTitle("Demo WebView");
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
