package eu.hansolo.fx.darkandlightside.tableview;

import javafx.beans.property.ObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import com.sun.javafx.scene.control.behavior.TextFieldBehavior;
import com.sun.javafx.scene.control.skin.TextFieldSkin;
import com.sun.javafx.scene.text.HitInfo;


public abstract class TextFieldFXSkin extends TextFieldSkin {
    private static final PseudoClass HAS_NO_SIDE_NODE = PseudoClass.getPseudoClass("no-side-nodes");
    private static final PseudoClass HAS_LEFT_NODE    = PseudoClass.getPseudoClass("left-node-visible");
    private static final PseudoClass HAS_RIGHT_NODE   = PseudoClass.getPseudoClass("right-node-visible");
    private        final TextField   control;
    private              Node        left;
    private              StackPane   leftPane;
    private              Node        right;
    private              StackPane   rightPane;


    // ******************** Constructors **************************************
    public TextFieldFXSkin(final TextField CONTROL) {
        super(CONTROL, new TextFieldBehavior(CONTROL));

        control = CONTROL;
        updateChildren();

        registerChangeListener(leftProperty(), "LEFT_NODE");
        registerChangeListener(rightProperty(), "RIGHT_NODE");
        registerChangeListener(CONTROL.focusedProperty(), "FOCUSED");
    }


    // ******************** Methods *******************************************
    public abstract ObjectProperty<Node> leftProperty();
    public abstract ObjectProperty<Node> rightProperty();

    @Override protected void handleControlPropertyChanged(final String PROPERTY) {
        super.handleControlPropertyChanged(PROPERTY);
        if ("LEFT_NODE" == PROPERTY || "RIGHT_NODE" == PROPERTY) updateChildren();
    }

    private void updateChildren() {
        Node newLeft = leftProperty().get();
        if (newLeft != null) {
            getChildren().remove(leftPane);
            leftPane = new StackPane(newLeft);
            leftPane.setAlignment(Pos.CENTER_LEFT);
            leftPane.getStyleClass().add("left-pane"); //$NON-NLS-1$
            getChildren().add(leftPane);
            left = newLeft;
        }

        Node newRight = rightProperty().get();
        if (newRight != null) {
            getChildren().remove(rightPane);
            rightPane = new StackPane(newRight);
            rightPane.setAlignment(Pos.CENTER_RIGHT);
            rightPane.getStyleClass().add("right-pane"); //$NON-NLS-1$
            getChildren().add(rightPane);
            right = newRight;
        }

        control.pseudoClassStateChanged(HAS_LEFT_NODE, left != null);
        control.pseudoClassStateChanged(HAS_RIGHT_NODE, right != null);
        control.pseudoClassStateChanged(HAS_NO_SIDE_NODE, left == null && right == null);
    }

    @Override protected void layoutChildren(final double X, final double Y, final double W, final double H) {
        final double FULL_HEIGHT        = H + snappedTopInset() + snappedBottomInset();
        final double LEFT_WIDTH         = leftPane == null ? 0.0 : snapSize(leftPane.prefWidth(FULL_HEIGHT));
        final double RIGHT_WIDTH        = rightPane == null ? 0.0 : snapSize(rightPane.prefWidth(FULL_HEIGHT));
        final double TEXT_FIELD_START_X = snapPosition(X) + snapSize(LEFT_WIDTH);
        final double TEXT_FIELD_WIDTH   = W - snapSize(LEFT_WIDTH) - snapSize(RIGHT_WIDTH);

        super.layoutChildren(TEXT_FIELD_START_X, 0, TEXT_FIELD_WIDTH, FULL_HEIGHT);

        if (leftPane != null) {
            final double LEFT_START_X = 0;
            leftPane.resizeRelocate(LEFT_START_X, 0, LEFT_WIDTH, FULL_HEIGHT);
        }

        if (rightPane != null) {
            final double RIGHT_START_X = rightPane == null ? 0.0 : W - RIGHT_WIDTH + snappedLeftInset();
            rightPane.resizeRelocate(RIGHT_START_X, 0, RIGHT_WIDTH, FULL_HEIGHT);
        }
    }

    @Override public HitInfo getIndex(final double X, final double Y) {
        final double LEFT_WIDTH = leftPane == null ? 0.0 : snapSize(leftPane.prefWidth(getSkinnable().getHeight()));
        return super.getIndex(X - LEFT_WIDTH, Y);
    }

    @Override protected double computePrefWidth(final double H, final double TOP_INSET, final double RIGHT_INSET, final double BOTTOM_INSET, final double LEFT_INSET) {
        final double PW          = super.computePrefWidth(H, TOP_INSET, RIGHT_INSET, BOTTOM_INSET, LEFT_INSET);
        final double LEFT_WIDTH  = leftPane == null ? 0.0 : snapSize(leftPane.prefWidth(H));
        final double RIGHT_WIDTH = rightPane == null ? 0.0 : snapSize(rightPane.prefWidth(H));
        return PW + LEFT_WIDTH + RIGHT_WIDTH;
    }

    @Override protected double computePrefHeight(final double W, final double TOP_INSET, final double RIGHT_INSET, final double BOTTOM_INSET, final double LEFT_INSET) {
        final double PH           = super.computePrefHeight(W, TOP_INSET, RIGHT_INSET, BOTTOM_INSET, LEFT_INSET);
        final double LEFT_HEIGHT  = leftPane == null ? 0.0 : snapSize(leftPane.prefHeight(-1));
        final double RIGHT_HEIGHT = rightPane == null ? 0.0 : snapSize(rightPane.prefHeight(-1));
        return Math.max(PH, Math.max(LEFT_HEIGHT, RIGHT_HEIGHT));
    }
}
