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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;


/**
 * Created by
 * User: hansolo
 * Date: 16.01.13
 * Time: 15:47
 */
public class DemoCalculator extends Application {
    public enum Operation {
        NONE     { public double eval(double x, double y) {return 0;    } },
        PLUS     { public double eval(double x, double y) {return x + y;} },
        MINUS    { public double eval(double x, double y) {return x - y;} },
        MULTIPLY { public double eval(double x, double y) {return x * y;} },
        DIVIDE   { public double eval(double x, double y) {return x / y;} };

        public abstract double eval(double x, double y);
    }
    private enum ButtonStyle {
        WHITE("button-white"),
        BROWN("button-brown"),
        GRAY("button-gray"),
        RED("button-red"),
        ORANGE("button-orange");

        public final String STYLE_CLASS;

        private ButtonStyle(final String STYLE_CLASS) {
            this.STYLE_CLASS = STYLE_CLASS;
        }
    }

    private EventHandler<ActionEvent> actionHandler;
    private EventHandler<KeyEvent>    keyHandler;
    private StringProperty            operator1;
    private StringProperty            operator2;
    private StringProperty            lcdValue;
    private StringBuilder             currentValue;
    private int                       currentOperator;
    private Operation                 operation;
    private Label                     lcd;
    private Button                    one;
    private Button                    two;
    private Button                    three;
    private Button                    four;
    private Button                    five;
    private Button                    six;
    private Button                    seven;
    private Button                    eight;
    private Button                    nine;
    private Button                    zero;
    private Button                    dot;
    private Button                    plus;
    private Button                    minus;
    private Button                    multiply;
    private Button                    divide;
    private Button                    plusminus;
    private Button                    equal;
    private Button                    ac;
    private Button                    c;
    private Button                    del;


    // ******************** Initialization ************************************
    @Override public void init() {
        actionHandler   = EVENT -> handleEvent(EVENT);
        keyHandler      = EVENT -> handleEvent(EVENT);

        operator1       = new SimpleStringProperty(this, "operator1", "0");
        operator2       = new SimpleStringProperty(this, "operator2", "0");
        lcdValue        = new SimpleStringProperty(this, "lcdValue", "0");
        currentOperator = 1;
        operation       = Operation.NONE;
        currentValue    = new StringBuilder();

        lcd = new Label();
        lcd.setMaxWidth(Double.MAX_VALUE);
        lcd.setMaxHeight(Double.MAX_VALUE);
        lcd.getStyleClass().add("lcd");
        lcd.textProperty().bind(lcdValue);

        one       = createButton("1", ButtonStyle.WHITE);
        two       = createButton("2", ButtonStyle.WHITE);
        three     = createButton("3", ButtonStyle.WHITE);
        four      = createButton("4", ButtonStyle.WHITE);
        five      = createButton("5", ButtonStyle.WHITE);
        six       = createButton("6", ButtonStyle.WHITE);
        seven     = createButton("7", ButtonStyle.WHITE);
        eight     = createButton("8", ButtonStyle.WHITE);
        nine      = createButton("9", ButtonStyle.WHITE);
        zero      = createButton("0", ButtonStyle.WHITE);
        dot       = createButton(".", ButtonStyle.WHITE);
        plus      = createButton("+", ButtonStyle.BROWN);
        minus     = createButton("-", ButtonStyle.BROWN);
        multiply  = createButton("×", ButtonStyle.BROWN);
        divide    = createButton("÷", ButtonStyle.BROWN);
        plusminus = createButton("±", ButtonStyle.GRAY);
        equal     = createButton("=", ButtonStyle.ORANGE);
        ac        = createButton("AC", ButtonStyle.RED);
        c         = createButton("C", ButtonStyle.RED);
        del       = createButton("Del", ButtonStyle.RED);
    }


    // ******************** Private Methods ***********************************
    private void updateLcdValue() {
        if (currentOperator == 1) {
            operator1.set(currentValue.toString());
        } else if (currentOperator == 2) {
            operator2.set(currentValue.toString());
        }
        lcdValue.set(currentValue.toString());
        equal.requestFocus();
    }

    private void updateOperator() {
        if (currentOperator == 1) {
            currentOperator = 2;
        } else if (currentOperator == 2) {
            String result = Double.toString(operation.eval(Double.parseDouble(operator1.get()), Double.parseDouble(operator2.get())));
            lcdValue.set(result);
            operator1.set(result);
            currentOperator = 2;
        }
        currentValue.setLength(0);
    }

    private void doOperation() {
        if (operation != Operation.NONE && !operator1.get().isEmpty() && !operator2.get().isEmpty()) {
            String result = Double.toString(operation.eval(Double.parseDouble(operator1.get()), Double.parseDouble(operator2.get())));
            lcdValue.set(result);
            operator1.set(result);
            currentOperator = 1;
            operation = Operation.NONE;
            currentValue.setLength(0);
            currentValue.append(result);
        }
    }

    private void reset() {
        operation = Operation.NONE;
        currentOperator = 1;
        currentValue.setLength(0);
        operator1.set("0");
        operator2.set("0");
        lcdValue.set("0");
    }

    private void resetLast() {
        if (currentOperator == 1) {
            reset();
        } else if (currentOperator == 2) {
            operator2.set("0");
            currentValue.setLength(0);
        }
    }

    private Button createButton(final String TEXT, final ButtonStyle BUTTON_STYLE) {
        Button button = new Button(TEXT);
        button.getStyleClass().addAll("calculator", BUTTON_STYLE.STYLE_CLASS);
        button.setOnAction(actionHandler);
        button.setOnKeyPressed(keyHandler);
        return button;
    }

    private Pane createLayout() {
        // Create GridPane
        GridPane pane = new GridPane();
        pane.getStyleClass().add("calculator");
        pane.setPadding(new Insets(10, 10, 10, 10));
        pane.setHgap(6);
        pane.setVgap(6);

        // Place LCD on top
        pane.add(lcd, 0, 0);
        GridPane.setColumnSpan(lcd, 5);
        GridPane.getHgrow(lcd);
        GridPane.setHgrow(lcd, Priority.ALWAYS);
        GridPane.setMargin(lcd, new Insets(0, 0, 15, 0));

        // First row (7, 8, 9, /, AC)
        pane.add(seven, 0, 1);
        pane.add(eight, 1, 1);
        pane.add(nine, 2, 1);
        GridPane.setMargin(nine, new Insets(0, 10, 0, 0));
        pane.add(divide, 3, 1);
        pane.add(ac, 4, 1);

        // Second row (4, 5, 6, x, C)
        pane.add(four, 0, 2);
        pane.add(five, 1, 2);
        pane.add(six, 2, 2);
        GridPane.setMargin(six, new Insets(0, 10, 0, 0));
        pane.add(multiply, 3, 2);
        pane.add(c, 4, 2);

        // Third row (1, 2, 3, -, DEL)
        pane.add(one, 0, 3);
        pane.add(two, 1, 3);
        pane.add(three, 2, 3);
        GridPane.setMargin(three, new Insets(0, 10, 0, 0));
        pane.add(minus, 3, 3);
        pane.add(del, 4, 3);

        // Fourth row (0, ., +-, +, =)
        pane.add(zero, 0, 4);
        pane.add(dot, 1, 4);
        pane.add(plusminus, 2, 4);
        GridPane.setMargin(plusminus, new Insets(0, 10, 0, 0));
        pane.add(plus, 3, 4);
        pane.add(equal, 4, 4);

        return pane;
    }


    // ******************** Event handling ************************************
    private void handleEvent(final Event EVENT) {
        final Object  SOURCE = EVENT.getSource();
        final String  TEXT   = EVENT instanceof KeyEvent ? ((KeyEvent) EVENT).getText() : "";
        if (SOURCE.equals(one) || TEXT.equals("1")) {
            currentValue.append("1");
            updateLcdValue();
        } else if (SOURCE.equals(two) || TEXT.equals("2")) {
            currentValue.append("2");
            updateLcdValue();
        } else if (SOURCE.equals(three) || TEXT.equals("3")) {
            currentValue.append("3");
            updateLcdValue();
        } else if (SOURCE.equals(four) || TEXT.equals("4")) {
            currentValue.append("4");
            updateLcdValue();
        } else if (SOURCE.equals(five) || TEXT.equals("5")) {
            currentValue.append("5");
            updateLcdValue();
        } else if (SOURCE.equals(six) || TEXT.equals("6")) {
            currentValue.append("6");
            updateLcdValue();
        } else if (SOURCE.equals(seven) || TEXT.equals("7")) {
            currentValue.append("7");
            updateLcdValue();
        } else if (SOURCE.equals(eight) || TEXT.equals("8")) {
            currentValue.append("8");
            updateLcdValue();
        } else if (SOURCE.equals(nine) || TEXT.equals("9")) {
            currentValue.append("9");
            updateLcdValue();
        } else if (SOURCE.equals(zero) || TEXT.equals("0")) {
            currentValue.append("0");
            updateLcdValue();
        } else if (SOURCE.equals(dot) || TEXT.equals(".")) {
            if (!lcdValue.toString().contains(".")) {
                currentValue.append(".");
            }
            updateLcdValue();
        } else if (SOURCE.equals(plusminus) || TEXT.equals("±")) {
            if (currentValue.length() > 0) {
                if (currentValue.charAt(0) == 45) {
                    currentValue.delete(0, 1);
                } else {
                    currentValue.insert(0, "-");
                }
            }
            updateLcdValue();
        } else if (SOURCE.equals(plus) || TEXT.equals("+")) {
            doOperation();
            operation = Operation.PLUS;
            updateOperator();
        } else if (SOURCE.equals(minus) || TEXT.equals("-")) {
            doOperation();
            operation = Operation.MINUS;
            updateOperator();
        } else if (SOURCE.equals(multiply) || TEXT.equals("x") || TEXT.equals("*")) {
            doOperation();
            operation = Operation.MULTIPLY;
            updateOperator();
        } else if (SOURCE.equals(divide) || TEXT.equals("/") || TEXT.equals(":")) {
            doOperation();
            operation = Operation.DIVIDE;
            updateOperator();
        } else if (SOURCE.equals(ac)) {
            reset();
        } else if (SOURCE.equals(c)) {
            resetLast();
        } else if (SOURCE.equals(del)) {
            int length = currentValue.length();
            if (length >= 1) {
                if (length == 1) {
                    currentValue.setLength(0);
                    currentValue.append("0");
                } else {
                    currentValue.replace(length - 1, length, "");
                }
                updateLcdValue();
            }
        } else if (SOURCE.equals(equal)) {
            doOperation();
        }
    }


    // ******************** Public Methods ************************************
    @Override public void start(Stage stage) {
        Scene scene = new Scene(createLayout());
        //scene.getStylesheets().add(DemoCalculator.class.getResource("calculator.css").toExternalForm());
        //scene.getStylesheets().add(DemoCalculator.class.getResource("calculator-alternative.css").toExternalForm());
        //scene.getStylesheets().add(DemoCalculator.class.getResource("calculator-braun.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Demo Powerful CSS");
        stage.setResizable(false);
        stage.show();
    }

    @Override public void stop() {

    }

    public static void main(String[] args) {
        launch(args);
    }
}