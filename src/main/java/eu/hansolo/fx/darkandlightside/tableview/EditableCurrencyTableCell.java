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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EditableCurrencyTableCell<S, T> extends TableCellFX<S, T> {
    public  static final Pattern     PATTERN_CURRENCY           = Pattern.compile("([a-zA-Z]{0,3}\\s?)(-?[0-9]{0,12})([,.]?[0-9]{0,12})([,.]?[0-9]{0,12})?(\\s?[a-zA-Z]{0,3})");
    private static final Matcher     MATCHER_CURRENCY           = PATTERN_CURRENCY.matcher("");
    private static final boolean     RIGHT                      = true;
    private static final boolean     LEFT                       = false;
    private              ObjectProperty<StringConverter<T>> converter;
    private              boolean                            escapePressed;
    private              CurrencyField                      currencyField;
    private              boolean                            currencyVisible;
    private              int                                caretPos;


    //******************** Constructors ***************************************
    public EditableCurrencyTableCell() {
        this(null);
    }
    public EditableCurrencyTableCell(final boolean CURRENCY_VISIBLE) {
        this(null, CURRENCY_VISIBLE);
    }
    public EditableCurrencyTableCell(final StringConverter<T> CONVERTER) {
        this(CONVERTER, false);
    }
    public EditableCurrencyTableCell(final StringConverter<T> CONVERTER, final boolean CURRENCY_VISIBLE) {
        getStyleClass().add("editable-cell");
        createCurrencyField();
        currencyVisible = CURRENCY_VISIBLE;
        converter       = new ObjectPropertyBase<StringConverter<T>>((StringConverter<T>) new DefaultStringConverter()) {
            @Override public Object getBean() { return EditableCurrencyTableCell.this; }
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
            if (currencyField == null) createCurrencyField();
            setText(null);
            setGraphic(currencyField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            currencyField.requestFocus();
            currencyField.selectAll();
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
            commitEdit(getConverter().fromString(currencyField.getText()));
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
                if (currencyField != null) { currencyField.setText(getString()); }
                setGraphic(currencyField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setText(String.format(currencyField.getLocale(), currencyField.getCurrency().getCurrencyCode() + " %.2f", currencyField.getValue()));
                //setText(getString());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
            tablePos = getTableView().getEditingCell();
        }
    }

    private void createCurrencyField() {
        currencyField = new CurrencyField(null == getText() ? BigDecimal.ZERO : new BigDecimal(getText()));
        currencyField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        currencyField.setOnKeyPressed(keyEvent -> {
            TableView tableView = EditableCurrencyTableCell.this.getTableView();
            KeyCode keyCode     = keyEvent.getCode();
            boolean isShiftDown = keyEvent.isShiftDown();
            if (keyCode == KeyCode.ENTER ||
                keyCode == KeyCode.TAB ||
                keyCode == KeyCode.UP ||
                keyCode == KeyCode.DOWN) {
                commitEdit(getConverter().fromString(currencyField.getText()));
                tableView.requestFocus();//why does it lose focus??
                switch (keyCode) {
                    case ENTER: commitEdit(getConverter().fromString(currencyField.getText())); break;
                    case TAB  :
                        commitEdit(getConverter().fromString(currencyField.getText()));
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
                        if (currencyField.getSelectedText().isEmpty() && caretPos == currencyField.getText().length()) {
                            commitEdit(getConverter().fromString(currencyField.getText()));
                            tableView.getSelectionModel().select(getTableRow().getIndex(), getNextColumn(RIGHT));
                        }
                        break;
                    case LEFT :
                        if (currencyField.getSelectedText().isEmpty() && caretPos == 0) {
                            commitEdit(getConverter().fromString(currencyField.getText()));
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

        currencyField.setOnKeyReleased(keyEvent -> {
            KeyCode keyCode = keyEvent.getCode();
            if (keyCode.isDigitKey() || keyCode.equals(KeyCode.PERIOD)) {
                if (CellField.isLessOrEqualOneSym()) {
                    CellField.addSymbol(keyEvent.getText());
                } else {
                    CellField.setText(currencyField.getText());
                }
                currencyField.setText(CellField.getText());
                currencyField.deselect();
                currencyField.end();
                //currencyField.positionCaret(currencyField.getLength() + 2);//works sometimes
            }
        });

        currencyField.focusedProperty().addListener(o -> {
            if (currencyField.isFocused()) {
                selectedCell.set(true);
            } else {
                //commitEdit(getConverter().fromString(getString()));
                commitEdit(getConverter().fromString(currencyField.getText()));
                selectedCell.set(false);
            }
        });

        currencyField.caretPositionProperty().addListener(o -> caretPos = currencyField.getCaretPosition());
    }

    private String getString() {
        if (null == getItem()) {
            return "";
        } else {
            MATCHER_CURRENCY.reset(getItem().toString());
            String nr = MATCHER_CURRENCY.matches() ? String.join("", MATCHER_CURRENCY.group(1), MATCHER_CURRENCY.group(2), MATCHER_CURRENCY.group(3), MATCHER_CURRENCY.group(4)) : "";
            return nr.isEmpty() ? "" : String.format(currencyField.getLocale(), "%.2f", Math.floor(Double.parseDouble(nr) * 100.0) / 100.0);
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
