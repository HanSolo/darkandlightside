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

import eu.hansolo.enzo.led.Led;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


/**
 * User: hansolo
 * Date: 19.08.16
 * Time: 15:16
 */
public class DemoWebViewLiveEdit extends Application {
    private ScriptEngine scriptEngine;
    private StackPane    ledPane;
    private Led          led;
    private WebView      webView;


    // ******************** Initialization ************************************
    @Override public void init() {
        led = new Led();

        ledPane = new StackPane();
        ledPane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        ledPane.setPrefSize(380, 380);
        ledPane.setPadding(new Insets(10, 10, 10, 10));
        ledPane.getChildren().add(led);

        initNashorn();
    }

    private void initNashorn() {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        scriptEngine = scriptEngineManager.getEngineByName("nashorn");

        // Inject led object into Nashorn scripting engine
        scriptEngine.put("led", led);
    }

    private void initWebView() {
        webView = new WebView();
        webView.setPrefSize(640, 400);

        WebEngine webEngine = webView.getEngine();
        webEngine.load(DemoWebViewLiveEdit.class.getResource("editor.html").toExternalForm());
        webEngine.getLoadWorker().stateProperty().addListener((ov, o, n) -> {
            if (Worker.State.SUCCEEDED == n) {
                webEngine.setOnStatusChanged(webEvent -> {
                    try {
                        scriptEngine.eval(webEvent.getData());
                    } catch (Exception e) { /* code is wrong */ }
                });
            }
        });
    }


    // ******************** Application start *********************************
    @Override public void start(Stage stage) {
        initWebView();

        HBox pane = new HBox();
        pane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        pane.setSpacing(10);
        pane.getChildren().addAll(ledPane, webView);

        Scene scene = new Scene(pane);

        stage.setTitle("WebView Nashorn Live-Edit");
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