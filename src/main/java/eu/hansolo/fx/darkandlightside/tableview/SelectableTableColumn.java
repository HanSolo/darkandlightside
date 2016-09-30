package eu.hansolo.fx.darkandlightside.tableview;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.awt.event.MouseEvent;


public class SelectableTableColumn<S, T> extends TableColumn<S, T> {
    private ColumnHeader         header;
    private InvalidationListener listener;


    //******************** Constructors ***************************************
    public SelectableTableColumn() {
        this("");
    }
    public SelectableTableColumn(final String TEXT) {
        super();
        header   = new ColumnHeader(TEXT);
        listener = o -> {
            TablePosition pos    = getTableView().getFocusModel().getFocusedCell();
            TableColumn   column = pos.getTableColumn();
            if (null == column) return;
            header.setSelectedColumnHeader(column.equals(SelectableTableColumn.this));
        };

        setGraphic(header);
        registerListeners();
    }


    //******************** Initialization *************************************
    private void registerListeners() {
        textProperty().addListener(o -> header.setText(getText()));



        tableViewProperty().addListener(o -> {
            TableView tableView = getTableView();
            if (null == tableView) return;
            tableView.getFocusModel().focusedCellProperty().addListener(new WeakInvalidationListener(listener));
        });
    }


    //******************** Methods ********************************************
    public String getHeaderText() { return header.getText(); }
    public void setHeaderText(final String TEXT) { header.setText(TEXT); }
    public StringProperty headerTextProperty() { return header.textProperty(); }
}
