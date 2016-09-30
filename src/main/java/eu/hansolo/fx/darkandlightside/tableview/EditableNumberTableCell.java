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

package eu.hansolo.fx.darkandlightside.tableview;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by hansolo on 23.06.16.
 */
public class EditableNumberTableCell<S, T> extends TableCellFX<S, T> {
    public  static final Pattern               PATTERN_CURRENCY = Pattern.compile("([a-zA-Z]{0,3}\\s?)(-?[0-9]{0,12})([,.]?[0-9]{0,12})([,.]?[0-9]{0,12})?(\\s?[a-zA-Z]{0,3})");
    private static final Matcher               MATCHER_CURRENCY = PATTERN_CURRENCY.matcher("");
    private static final boolean               RIGHT            = true;
    private static final boolean               LEFT             = false;
    private ObjectProperty<StringConverter<T>> converter;
    private boolean                            escapePressed;
    private NumberField                        numberField;
    private int                                caretPos;


    //******************** Constructors ***************************************
    public EditableNumberTableCell() {
        this(null);
    }
    public EditableNumberTableCell(final boolean CURRENCY_VISIBLE) {
        this(null, CURRENCY_VISIBLE);
    }
    public EditableNumberTableCell(final StringConverter<T> CONVERTER) {
        this(CONVERTER, false);
    }
    public EditableNumberTableCell(final StringConverter<T> CONVERTER, final boolean CURRENCY_VISIBLE) {
        getStyleClass().add("editable-cell");
        createNumberField();
        converter       = new ObjectPropertyBase<StringConverter<T>>((StringConverter<T>) new DefaultStringConverter()) {
            @Override public Object getBean() { return EditableNumberTableCell.this; }
            @Override public String getName() { return "converter"; }
        };

        if (CONVERTER != null) setConverter(CONVERTER);
    }


    //******************** Methods ********************************************
    public StringConverter<T> getConverter() { return converterProperty().get(); }
    public void setConverter(final StringConverter CONVERTER) { converterProperty().set(CONVERTER); }
    public ObjectProperty<StringConverter<T>> converterProperty() { return converter; }

    @Override public void startEdit() {
        if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) return;

        if (!isEmpty()) {
            super.startEdit();
            if (numberField == null) createNumberField();
            setText(null);
            setGraphic(numberField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            numberField.requestFocus();
            numberField.selectAll();
        }
    }

    @Override public void commitEdit(final T VALUE) {
        if (!isEditing()) return;
        final TableView<S> tableView = getTableView();
        if (null == tableView || null == getTableColumn() || null == VALUE || null == tablePos) return;
        CellEditEvent editEvent = new CellEditEvent(tableView, tablePos, TableColumn.editCommitEvent(), VALUE);
        Event.fireEvent(getTableColumn(), editEvent);

        super.cancelEdit();

        updateItem(VALUE, false);

        if (tableView != null) {
            tableView.edit(-1, null);
            //tableView.requestFocus();
        }

        // Reset CellField Text
        CellField.setText("");
    }

    @Override public void cancelEdit() {
        if (escapePressed) {
            super.cancelEdit();
            setText(String.valueOf(getItem()));
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        } else {
            commitEdit(getConverter().fromString(numberField.getText()));
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        // Reset CellField Text
        CellField.setText("");
    }

    @Override public void updateItem(final T ITEM, final boolean EMPTY) {
        super.updateItem(ITEM, EMPTY);
        if (EMPTY) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (numberField != null) { numberField.setText(getString()); }
                setGraphic(numberField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setText(String.format(numberField.getLocale(), new StringBuilder(" %.").append(numberField.getDecimals()).append("f").toString(), numberField.getValue()));
                //setText(getString());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
            tablePos = getTableView().getEditingCell();
        }
    }

    private void createNumberField() {
        numberField = new NumberField(null == getText() ? BigDecimal.ZERO : new BigDecimal(getText()));
        numberField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        numberField.setOnKeyPressed(keyEvent -> {
            TableView tableView   = EditableNumberTableCell.this.getTableView();
            KeyCode   keyCode     = keyEvent.getCode();
            boolean   isShiftDown = keyEvent.isShiftDown();
            if (keyCode == KeyCode.ENTER ||
                keyCode == KeyCode.TAB ||
                keyCode == KeyCode.UP ||
                keyCode == KeyCode.DOWN) {
                commitEdit(getConverter().fromString(numberField.getText()));
                tableView.requestFocus();//why does it lose focus??
                switch (keyCode) {
                    case ENTER: commitEdit(getConverter().fromString(numberField.getText())); break;
                    case TAB  :
                        commitEdit(getConverter().fromString(numberField.getText()));
                        TableColumn nextColumn = getNextColumn(!isShiftDown);
                        if (nextColumn != null) {
                            tableView.getFocusModel().focus(getIndex(), nextColumn);
                            tableView.getSelectionModel().select(getIndex(), nextColumn);
                            tableView.edit(getTableRow().getIndex(), nextColumn);
                        }
                        break;
                    case UP   : tableView.getSelectionModel().selectAboveCell(); break;
                    case DOWN : tableView.getSelectionModel().selectBelowCell(); break;
                }
            } else if (keyCode == KeyCode.LEFT || keyCode == KeyCode.RIGHT) {
                switch(keyCode) {
                    case RIGHT:
                        if (numberField.getSelectedText().isEmpty() && caretPos == numberField.getText().length()) {
                            commitEdit(getConverter().fromString(numberField.getText()));
                            tableView.getSelectionModel().select(getTableRow().getIndex(), getNextColumn(RIGHT));
                        }
                        break;
                    case LEFT :
                        if (numberField.getSelectedText().isEmpty() && caretPos == 0) {
                            commitEdit(getConverter().fromString(numberField.getText()));
                            tableView.getSelectionModel().select(getTableRow().getIndex(), getNextColumn(LEFT));
                        }
                        break;
                }
            } else if (keyCode == KeyCode.ESCAPE) {
                escapePressed = true;
                cancelEdit();
            } else {
                escapePressed = false;
            }
        });

        numberField.setOnKeyReleased(keyEvent -> {
            KeyCode keyCode = keyEvent.getCode();
            if (keyCode.isDigitKey() || keyCode.equals(KeyCode.PERIOD)) {
                if (CellField.isLessOrEqualOneSym()) {
                    CellField.addSymbol(keyEvent.getText());
                } else {
                    CellField.setText(numberField.getText());
                }
                numberField.setText(CellField.getText());
                numberField.deselect();
                numberField.end();
                //numberField.positionCaret(numberField.getLength() + 2);//works sometimes
            }
        });

        numberField.focusedProperty().addListener(o -> {
            if (numberField.isFocused()) {
                selectedCell.set(true);
            } else {
                commitEdit(getConverter().fromString(getString()));
                selectedCell.set(false);
            }
        });

        numberField.caretPositionProperty().addListener(o -> caretPos = numberField.getCaretPosition());
    }

    private String getString() {
        if (null == getItem()) {
            return "";
        } else {
            MATCHER_CURRENCY.reset(getItem().toString());
            String nr = MATCHER_CURRENCY.matches() ? String.join("", MATCHER_CURRENCY.group(1), MATCHER_CURRENCY.group(2), MATCHER_CURRENCY.group(3), MATCHER_CURRENCY.group(4)) : "";
            return nr.isEmpty() ? "" : String.format(numberField.getLocale(), "%.2f", Math.floor(Double.parseDouble(nr) * 100.0) / 100.0);
        }
    }

    private TableColumn<S, ?> getNextColumn(final boolean RIGHT) {
        List<TableColumn<S, ?>> columns = new ArrayList<>();
        for (TableColumn<S, ?> column : getTableView().getColumns()) { columns.addAll(getLeaves(column)); }

        //if (columns.size() < 2) return null; //There is no other column that supports editing.

        int currentIndex = columns.indexOf(getTableColumn());
        int nextIndex = currentIndex;
        if (RIGHT) {
            nextIndex++;
            if (nextIndex > columns.size() - 1) { nextIndex = 0; }
        } else {
            nextIndex--;
            if (nextIndex < 0) { nextIndex = columns.size() - 1; }
        }
        return columns.get(nextIndex);
    }
    private List<TableColumn<S, ?>> getLeaves(final TableColumn<S, ?> ROOT) {
        List<TableColumn<S, ?>> columns = new ArrayList<>();
        if (ROOT.getColumns().isEmpty()) {
            //We only want the leaves that are editable.
            //if (ROOT.isEditable()) { columns.add(ROOT); }
            columns.add(ROOT);
            return columns;
        } else {
            for (TableColumn<S, ?> column : ROOT.getColumns()) { columns.addAll(getLeaves(column)); }
            return columns;
        }
    }
}
