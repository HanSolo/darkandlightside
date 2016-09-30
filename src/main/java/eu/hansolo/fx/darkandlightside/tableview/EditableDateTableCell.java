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
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class EditableDateTableCell<S> extends TableCellFX<S, LocalDate> {
    private static final boolean                           RIGHT = true;
    private static final boolean                           LEFT  = false;
    private              ObjectProperty<DateTimeFormatter> formatter;
    private              boolean                           escapePressed;
    private              DatePicker                        datePicker;
    private              int                               caretPos;


    //******************** Constructors ***************************************
    public EditableDateTableCell() {
        this(null);
    }
    public EditableDateTableCell(final DateTimeFormatter FORMATTER) {
        getStyleClass().add("editable-date-cell");
        formatter    = new ObjectPropertyBase<DateTimeFormatter>(DateTimeFormatter.ofPattern("dd.MM.YYYY")) {
            @Override public Object getBean() { return EditableDateTableCell.this; }
            @Override public String getName() { return "formatter"; }
        };

        if (FORMATTER != null) setFormatter(FORMATTER);
    }


    //******************** Methods ********************************************
    public DateTimeFormatter getFormatter() { return formatterProperty().get(); }
    public void setFormatter(final DateTimeFormatter FORMATTER) { formatterProperty().set(FORMATTER); }
    public ObjectProperty<DateTimeFormatter> formatterProperty() { return formatter; }

    @Override public void startEdit() {
        if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) return;

        if (!isEmpty()) {
            super.startEdit();
            if (datePicker == null) createDatePicker();
            setGraphic(datePicker);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            datePicker.getEditor().setText(getText());
            datePicker.requestFocus();
            datePicker.getEditor().selectAll();
        }
    }

    @Override public void commitEdit(final LocalDate VALUE) {
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
            commitEdit(datePicker.getValue());
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
    }

    @Override public void updateItem(final LocalDate ITEM, final boolean EMPTY) {
        super.updateItem(ITEM, EMPTY);
        if (EMPTY) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (datePicker != null) {
                    datePicker.getEditor().setText(getString());
                    datePicker.setValue(ITEM);
                }
                setGraphic(datePicker);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setText(getString());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }
    }

    private void createDatePicker() {
        datePicker = new DatePicker(getItem());

        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override public String toString(final LocalDate DATE) { return DATE == null ? "" : getFormatter().format(DATE); }
            @Override public LocalDate fromString(final String DATE_STRING) { return (DATE_STRING != null && !DATE_STRING.isEmpty()) ? LocalDate.parse(DATE_STRING, getFormatter()) : null; }
        });

        datePicker.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);

        datePicker.getEditor().setOnKeyPressed(keyEvent -> {
            TableView tableView = EditableDateTableCell.this.getTableView();
            KeyCode keyCode     = keyEvent.getCode();
            boolean isShiftDown = keyEvent.isShiftDown();
            if (keyCode == KeyCode.ENTER ||
                keyCode == KeyCode.TAB ||
                keyCode == KeyCode.UP ||
                keyCode == KeyCode.DOWN) {
                commitEdit(datePicker.getValue());
                tableView.requestFocus();//why does it lose focus??
                switch (keyCode) {
                    case TAB  :
                        commitEdit(datePicker.getValue());
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
                        if (datePicker.getEditor().getSelectedText().isEmpty() && caretPos == datePicker.getEditor().getText().length()) {
                            commitEdit(datePicker.getValue());
                            tableView.getSelectionModel().select(getTableRow().getIndex(), getNextColumn(RIGHT));
                        }
                        break;
                    case LEFT :
                        if (datePicker.getEditor().getSelectedText().isEmpty() && caretPos == 0) {
                            commitEdit(datePicker.getValue());
                            tableView.getSelectionModel().select(getTableRow().getIndex(), getNextColumn(LEFT));
                        }
                        break;
                }
            } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
                escapePressed = true;
                cancelEdit();
            } else {
                escapePressed = false;
            }
        });

        datePicker.setOnKeyPressed(keyEvent -> {
            KeyCode keyCode     = keyEvent.getCode();
            boolean isShiftDown = keyEvent.isShiftDown();
            if (keyCode == KeyCode.ENTER ||
                keyCode == KeyCode.TAB ||
                keyCode == KeyCode.UP ||
                keyCode == KeyCode.DOWN) {
                commitEdit(datePicker.getValue());
                TableView tableView = EditableDateTableCell.this.getTableView();
                tableView.requestFocus();//why does it lose focus??
                switch (keyCode) {
                    case ENTER: commitEdit(getItem()); break;
                }
            } else if (keyCode == KeyCode.ESCAPE) {
                escapePressed = true;
                cancelEdit();
            } else {
                escapePressed = false;
            }
        });

        datePicker.setOnKeyReleased(keyEvent -> {
            KeyCode keyCode = keyEvent.getCode();
            if (keyCode.isDigitKey()) {
                if (CellField.isLessOrEqualOneSym()) {
                    CellField.addSymbol(keyEvent.getText());
                } else {
                    CellField.setText(getFormatter().format(datePicker.getValue()));
                }
                datePicker.getEditor().setText(CellField.getText());
                datePicker.getEditor().deselect();
                datePicker.getEditor().end();
                //datePicker.getEditor().positionCaret(datePicker.getEditor().getLength() + 2);//works sometimes
            }
        });

        datePicker.focusedProperty().addListener(o -> {
            if (datePicker.isFocused()) {
                selectedCell.set(true);
            } else {
                commitEdit(datePicker.getValue());
                selectedCell.set(false);
            }
        });

        datePicker.getEditor().caretPositionProperty().addListener(o -> caretPos = datePicker.getEditor().getCaretPosition());
    }

    private String getString() {
        return getFormatter() == null ? getItem() == null ? "" : getItem().toString() : getFormatter().format(getItem());
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
