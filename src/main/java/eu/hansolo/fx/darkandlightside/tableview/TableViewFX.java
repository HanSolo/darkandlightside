package eu.hansolo.fx.darkandlightside.tableview;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;


public class TableViewFX<S> extends TableView<S> {

    private BooleanProperty shiftDown;
    private BooleanProperty mouseDown;


    public TableViewFX() {
        this(FXCollections.<S>observableArrayList());
    }
    public TableViewFX(final ObservableList<S> ITEMS) {
        super(ITEMS);
        getStylesheets().add(TableViewFX.class.getResource("styles.css").toExternalForm());
        shiftDown = new BooleanPropertyBase(false) {
            @Override public Object getBean() { return TableViewFX.this; }
            @Override public String getName() { return "shiftDown"; }
        };
        mouseDown = new BooleanPropertyBase(false) {
            @Override public Object getBean() { return TableViewFX.this; }
            @Override public String getName() { return "mouseDown"; }
        };

        registerListeners();
    }

    private void registerListeners() {
        setOnMouseClicked(mouseEvent -> {
            final int         ROW = getSelectionModel().getSelectedIndex();
            final TableColumn COL = getFocusModel().getFocusedCell().getTableColumn();
            edit(ROW, COL);
        });

        setOnKeyPressed(keyEvent -> {
            final KeyCode KEY_CODE = keyEvent.getCode();
            shiftDown.set(keyEvent.isShiftDown());

            if (KEY_CODE == KeyCode.ENTER || KEY_CODE == KeyCode.ESCAPE) {
                CellField.clearText();
            }
            if (KEY_CODE.isDigitKey()) {
                final int         ROW = getSelectionModel().getSelectedIndex();
                final TableColumn COL = getFocusModel().getFocusedCell().getTableColumn();
                edit(ROW, COL);
            }
        });
        setOnKeyReleased(keyEvent -> shiftDown.set(keyEvent.isShiftDown()));

        setOnMousePressed(mouseEvent -> mouseDown.set(true));
        setOnMouseReleased(mouseEvent -> mouseDown.set(false));

        addEventHandler(HoverEvent.CURRENT_ROW, hoverEvent -> System.out.println("Hovered Row: " + hoverEvent.TABLE_ROW.getIndex()));
        addEventHandler(HoverEvent.CURRENT_COL, hoverEvent -> System.out.println("Hovered Col: " + hoverEvent.TABLE_COL.getId()));

        final ObservableList<TablePosition> selectedCells = getSelectionModel().getSelectedCells();
        selectedCells.addListener((ListChangeListener<TablePosition>) c -> {
            for (TablePosition pos : selectedCells) { edit(pos.getRow(), pos.getTableColumn()); }
        });
    }

    public boolean isShiftDown() { return shiftDown.get(); }
    public ReadOnlyBooleanProperty shiftDownProperty() { return shiftDown; }

    public boolean isMouseDown() { return mouseDown.get(); }
    public ReadOnlyBooleanProperty mouseDownProperty() { return mouseDown; }
}
