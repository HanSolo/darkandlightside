package eu.hansolo.fx.darkandlightside.tableview;

import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;


public class SelectableTableCell<S, T> extends TableCellFX<S, T> {
    private ObjectProperty<StringConverter<T>> converter;


    //******************** Constructors ***************************************
    public SelectableTableCell() {
        this(null);
    }
    public SelectableTableCell(final StringConverter<T> CONVERTER) {
        getStyleClass().add("selectable-cell");
        converter    = new ObjectPropertyBase<StringConverter<T>>((StringConverter<T>) new DefaultStringConverter()) {
            @Override public Object getBean() { return SelectableTableCell.this; }
            @Override public String getName() { return "converter"; }
        };

        if (CONVERTER != null) setConverter(CONVERTER);
    }


    //******************** Methods ********************************************
    public StringConverter<T> getConverter() { return converterProperty().get(); }
    public void setConverter(final StringConverter CONVERTER) { converterProperty().set(CONVERTER); }
    public ObjectProperty<StringConverter<T>> converterProperty() { return converter; }

    @Override public void updateItem(final T ITEM, final boolean EMPTY) {
        super.updateItem(ITEM, EMPTY);
        if (EMPTY) {
            setText(null);
            setGraphic(null);
        } else {
            setText(getString());
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
    }

    private String getString() {
        return getConverter() == null ? getItem() == null ? "" : getItem().toString() : getConverter().toString(getItem());
    }
}
