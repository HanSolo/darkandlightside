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

package eu.hansolo.fx.darkandlightside.datepicker;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.time.LocalDate;


/**
 * User: hansolo
 * Date: 30.06.16
 * Time: 13:03
 */
public class DemoDatePicker extends Application {
    private VBox pane;

    @Override public void init() {
        EventHandler<MouseEvent> eventConsumer = e -> e.consume();

        Label      labelDisabled      = new Label("Disabled");
        Region     spacerDisabled     = new Region();
        DatePicker datePickerDisabled = new DatePicker(LocalDate.now());
        datePickerDisabled.setDisable(true);

        Label      labelEditableFalse      = new Label("Editable = false");
        Region     spacerEditableFalse     = new Region();
        DatePicker datePickerEditableFalse = new DatePicker(LocalDate.now());
        datePickerEditableFalse.setEditable(false);

        Label      labelReadOnly      = new Label("ReadOnly");
        Region     spacerReadOnly     = new Region();
        DatePicker datePickerReadOnly = new DatePicker(LocalDate.now());
        datePickerReadOnly.setEditable(false);
        datePickerReadOnly.skinProperty().addListener(o1 -> {
            if (null == datePickerReadOnly.getSkin() || null == datePickerReadOnly.getScene()) return;
            fixReadOnlyBug(datePickerReadOnly, eventConsumer);
            datePickerReadOnly.editableProperty().addListener(o2 -> fixReadOnlyBug(datePickerReadOnly, eventConsumer));
        });

        HBox disabledBox = new HBox(labelDisabled, spacerDisabled, datePickerDisabled);
        disabledBox.setSpacing(20);
        disabledBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(spacerDisabled, Priority.ALWAYS);

        HBox editableFalseBox = new HBox(labelEditableFalse, spacerEditableFalse, datePickerEditableFalse);
        editableFalseBox.setSpacing(20);
        editableFalseBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(spacerEditableFalse, Priority.ALWAYS);

        HBox readOnlyBox = new HBox(labelReadOnly, spacerReadOnly, datePickerReadOnly);
        readOnlyBox.setSpacing(20);
        readOnlyBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(spacerReadOnly, Priority.ALWAYS);

        pane = new VBox(disabledBox, editableFalseBox, readOnlyBox);
        pane.setPadding(new Insets(20));
        pane.setSpacing(20);
    }

    @Override public void start(Stage stage) {
        Scene scene = new Scene(pane, 380, 160);

        stage.setTitle("DatePicker Demo");
        stage.setScene(scene);
        stage.show();
    }

    @Override public void stop() {
        System.exit(0);
    }

    private void fixReadOnlyBug(final DatePicker DATE_PICKER, final EventHandler<MouseEvent> EVENT_CONSUMER) {
        Node arrowButton = DATE_PICKER.lookup("#arrow-button");
        if (DATE_PICKER.isEditable()) {
            arrowButton.removeEventFilter(MouseEvent.MOUSE_PRESSED, EVENT_CONSUMER);
            arrowButton.removeEventFilter(MouseEvent.MOUSE_RELEASED, EVENT_CONSUMER);
        } else {
            arrowButton.addEventFilter(MouseEvent.MOUSE_PRESSED, EVENT_CONSUMER);
            arrowButton.addEventFilter(MouseEvent.MOUSE_RELEASED, EVENT_CONSUMER);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
