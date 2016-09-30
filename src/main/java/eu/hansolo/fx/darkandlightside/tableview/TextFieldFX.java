package eu.hansolo.fx.darkandlightside.tableview;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;


public class TextFieldFX extends TextField {
    private ObjectProperty<Node> right = new SimpleObjectProperty<>(this, "right");
    private ObjectProperty<Node> left  = new SimpleObjectProperty<>(this, "left");


    // ******************** Constructors **************************************
    public TextFieldFX() {
        getStyleClass().add("text-field-fx");
    }


    // ******************** Methods *******************************************
    public final Node getLeft() { return left.get(); }
    public final void setLeft(Node value) { left.set(value); }
    public final ObjectProperty<Node> leftProperty() { return left; }

    public final Node getRight() { return right.get(); }
    public final void setRight(Node value) { right.set(value); }
    public final ObjectProperty<Node> rightProperty() { return right; }

    @Override protected Skin<?> createDefaultSkin() {
        return new TextFieldFXSkin(this) {
            @Override public ObjectProperty<Node> leftProperty() { return TextFieldFX.this.leftProperty(); }
            @Override public ObjectProperty<Node> rightProperty() { return TextFieldFX.this.rightProperty(); }
        };
    }

    @Override public String getUserAgentStylesheet() { return TextFieldFX.class.getResource("styles.css").toExternalForm(); }
}
