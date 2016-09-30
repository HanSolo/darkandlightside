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
import javafx.css.PseudoClass;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;


/**
 * Created by hansolo on 07.07.16.
 */
public abstract class TableCellFX<S, T> extends TableCell<S, T> {
    private static final PseudoClass SELECTED_CELL_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected-cell");
    private static final PseudoClass MULTI_SELECT_PSEUDO_CLASS  = PseudoClass.getPseudoClass("multi-select");
    BooleanProperty          selectedCell;
    BooleanProperty          multiSelect;
    TablePosition            tablePos;
    boolean                  selecting;
    InvalidationListener     listener;
    TableRow                 rowStart;
    TableColumn              colStart;
    TableRow                 rowStop;
    TableColumn              colStop;


    //******************** Constructors ***************************************
    TableCellFX() {
        super();
        selectedCell = new BooleanPropertyBase(false) {
            @Override protected void invalidated() { pseudoClassStateChanged(SELECTED_CELL_PSEUDO_CLASS, get()); }
            @Override public Object getBean() { return TableCellFX.this; }
            @Override public String getName() { return "selectedCell"; }
        };
        multiSelect  = new BooleanPropertyBase(false) {
            @Override protected void invalidated() { pseudoClassStateChanged(MULTI_SELECT_PSEUDO_CLASS, get()); }
            @Override public Object getBean() { return TableCellFX.this; }
            @Override public String getName() { return "multiSelect"; }
        };
        selecting    = false;
        listener = o -> {
            TablePosition pos    = getTableView().getFocusModel().getFocusedCell();
            TableRow      row    = getTableRow();
            TableColumn   column = pos.getTableColumn();
            if (null == row || null == getTableColumn() || null == column) return;
            selectedCell.set(column.equals(getTableColumn()) && row.getIndex() == pos.getRow());
        };

        registerListeners();
    }


    //******************** Initialization *************************************
    private void registerListeners() {
        tableViewProperty().addListener(o -> {
            TableView tableView = getTableView();
            if (null == tableView) return;
            tableView.getFocusModel().focusedCellProperty().addListener(new WeakInvalidationListener(listener));
            if (isEditable()) tableView.editingCellProperty().addListener(c -> tablePos = getTableView().getEditingCell());
        });

        setOnMousePressed(mouseEvent -> {
            TableViewFX tableView = (TableViewFX) getTableView();
            if (null == tableView) return;
            if (tableView.isShiftDown() && !selecting) {
                TablePosition pos    = getTableView().getFocusModel().getFocusedCell();
                rowStart = getTableRow();
                colStart = pos.getTableColumn();
                if (null == rowStart || null == getTableColumn() || null == colStart) return;
                selecting = true;
            }
        });

        hoverProperty().addListener(o -> {
            TableViewFX tableView = (TableViewFX) getTableView();
            if (null == tableView) return;
            if (tableView.isShiftDown()) {
                //TablePosition pos = getTableView().getFocusModel().getFocusedCell();
                TableRow      row = getTableRow();
                //TableColumn   col = pos.getTableColumn();
                //if (null == row || null == getTableColumn() || null == col) return;
                if (null == row) return;
                fireEvent(new HoverEvent(HoverEvent.CURRENT_ROW, row));
                System.out.println("Hovering row: " + row.getIndex());
            }
        });

        setOnMouseReleased(mouseEvent -> {
            TableViewFX tableView = (TableViewFX) getTableView();
            if (null == tableView) return;
            if (tableView.isShiftDown() && selecting) { selecting = false; }
        });
    }


    //******************** Methods ********************************************
    public boolean isSelectedCell() { return selectedCell.get(); }
    public void setSelectedCell(final boolean IS_SELECTED) { selectedCell.set(IS_SELECTED); }
    public BooleanProperty selectedCellProperty() { return selectedCell; }

    public boolean isMultiSelect() { return multiSelect.get(); }
    public void setMultiSelect(final boolean SELECT) { multiSelect.set(SELECT); }
    public BooleanProperty multiSelectProperty() { return multiSelect; }
}
