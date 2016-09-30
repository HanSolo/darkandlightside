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
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.util.ArrayList;
import java.util.List;


public class EditableTableCell<S, T> extends TableCellFX<S, T> {
    private static final boolean                            RIGHT = true;
    private static final boolean                            LEFT  = false;
    private              ObjectProperty<StringConverter<T>> converter;
    private              boolean                            escapePressed;
    private              TextField                          textField;
    private              int                                caretPos;


    //******************** Constructors ***************************************
    public EditableTableCell() {
        this(null);
    }
    public EditableTableCell(final StringConverter<T> CONVERTER) {
        getStyleClass().add("editable-cell");
        converter    = new ObjectPropertyBase<StringConverter<T>>((StringConverter<T>) new DefaultStringConverter()) {
            @Override public Object getBean() { return EditableTableCell.this; }
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
            if (textField == null) createTextField();
            setText(null);
            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            textField.requestFocus();
            textField.selectAll();
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
    }

    @Override public void cancelEdit() {
        if (escapePressed) {
            super.cancelEdit();
            setText(String.valueOf(getItem()));
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        } else {
            commitEdit(getConverter().fromString(textField.getText()));
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
    }

    @Override public void updateItem(final T ITEM, final boolean EMPTY) {
        super.updateItem(ITEM, EMPTY);
        if (EMPTY) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setText(getString());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
            tablePos = getTableView().getEditingCell();
        }
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.setOnKeyPressed(keyEvent -> {
            TableView tableView = EditableTableCell.this.getTableView();
            KeyCode keyCode     = keyEvent.getCode();
            boolean isShiftDown = keyEvent.isShiftDown();
            if (keyCode == KeyCode.ENTER ||
                keyCode == KeyCode.TAB ||
                keyCode == KeyCode.UP ||
                keyCode == KeyCode.DOWN) {
                commitEdit(getConverter().fromString(textField.getText()));
                tableView.requestFocus();//why does it lose focus??
                switch (keyCode) {
                    case ENTER: commitEdit(getConverter().fromString(textField.getText())); break;
                    case TAB  :
                        commitEdit(getConverter().fromString(textField.getText()));
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
                        if (textField.getSelectedText().isEmpty() && caretPos == textField.getText().length()) {
                            commitEdit(getConverter().fromString(textField.getText()));
                            tableView.getSelectionModel().select(getTableRow().getIndex(), getNextColumn(RIGHT));
                        }
                        break;
                    case LEFT :
                        if (textField.getSelectedText().isEmpty() && caretPos == 0) {
                            commitEdit(getConverter().fromString(textField.getText()));
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

        textField.setOnKeyReleased(keyEvent -> {
            KeyCode keyCode = keyEvent.getCode();
            if (keyCode.isDigitKey()) {
                if (CellField.isLessOrEqualOneSym()) {
                    CellField.addSymbol(keyEvent.getText());
                } else {
                    CellField.setText(textField.getText());
                }
                textField.setText(CellField.getText());
                textField.deselect();
                textField.end();
                //textField.positionCaret(textField.getLength() + 2);//works sometimes
            }
        });

        textField.focusedProperty().addListener(o -> {
            if (textField.isFocused()) {
                selectedCell.set(true);
            } else {
                commitEdit(getConverter().fromString(getString()));
                selectedCell.set(false);
            }
        });

        textField.caretPositionProperty().addListener(o -> caretPos = textField.getCaretPosition());
    }

    private String getString() {
        return getConverter() == null ? getItem() == null ? "" : getItem().toString() : getConverter().toString(getItem());
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
