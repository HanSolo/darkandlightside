package eu.hansolo.fx.darkandlightside.tableview;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;


public class ColumnHeader extends Label {
    private static final PseudoClass     SELECTED_COLUMN_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected-column-header");
    private              BooleanProperty selectedColumnHeader;


    //******************** Constructors ***************************************
    public ColumnHeader() {
        this("");
    }
    public ColumnHeader(final String TEXT) {
        super(TEXT);

        getStyleClass().add("selectable-column-header");
        selectedColumnHeader = new BooleanPropertyBase(false) {
            @Override protected void invalidated() { pseudoClassStateChanged(SELECTED_COLUMN_PSEUDO_CLASS, get()); }
            @Override public Object getBean() { return ColumnHeader.this; }
            @Override public String getName() { return "selectedColumnHeader"; }
        };
    }


    //******************** Methods ********************************************
    public boolean getSelectedColumnHeader() { return selectedColumnHeader.get(); }
    public void setSelectedColumnHeader(final boolean SELECTED) { selectedColumnHeader.set(SELECTED); }
    public BooleanProperty selectedColumnHeaderProperty() { return selectedColumnHeader; }
}
