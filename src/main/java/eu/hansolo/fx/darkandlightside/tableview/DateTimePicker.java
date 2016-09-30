package eu.hansolo.fx.darkandlightside.tableview;

import com.sun.javafx.css.converters.ColorConverter;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DateTimePicker extends DatePicker {
    public  static final String           DEFAULT_FORMAT = "dd.MM.yyyy HH:mm";
    private static final PseudoClass      HAS_SELECTION  = PseudoClass.getPseudoClass("has-selection");
    private DateTimeFormatter             formatter;
    private BooleanProperty               hasSelection;
    private ObjectProperty<LocalDateTime> dateTime;
    private ObjectProperty<String>        format;


    // ******************** Constructors **************************************
    public DateTimePicker() {
        this(null, "");
    }
    public DateTimePicker(final LocalDateTime DATE_TIME) {
        this(DATE_TIME, "");
    }
    public DateTimePicker(final String PROMPT_TEXT) {
        this(null, PROMPT_TEXT);
    }
    public DateTimePicker(final LocalDateTime DATE_TIME, final String PROMPT_TEXT) {
        super(null == DATE_TIME ? null : DATE_TIME.toLocalDate());

        getStylesheets().add(DateTimePicker.class.getResource("styles.css").toExternalForm());
        setPromptText(PROMPT_TEXT);

        formatter = DateTimeFormatter.ofPattern(DEFAULT_FORMAT);
        dateTime  = new SimpleObjectProperty<>(DateTimePicker.this, "dateTime", DATE_TIME);
        format    = new ObjectPropertyBase<String>(DEFAULT_FORMAT) {
            @Override protected void invalidated() {
                formatter = DateTimeFormatter.ofPattern(get());
            }
            @Override public Object getBean() { return DateTimePicker.this; }
            @Override public String getName() { return "format"; }
        };

        setFormat(DEFAULT_FORMAT);
        setConverter(new InternalConverter());

        registerListeners();
    }


    // ******************** Initialization ************************************
    private void registerListeners() {
        valueProperty().addListener(o -> handleTextAndFocus(isFocused()));
        valueProperty().addListener((o, ov, nv) -> {
            if (null == nv) {
                dateTime.set(null);
            } else {
                dateTime.set(null == dateTime.get() ? LocalDateTime.of(nv, LocalTime.now()) : LocalDateTime.of(nv, dateTime.get().toLocalTime()));
            }
        });
        dateTime.addListener((o, ov, nv) -> setValue(null == nv ? null : nv.toLocalDate()));
        getEditor().textProperty().addListener(o -> handleTextAndFocus(isFocused()));
        focusedProperty().addListener(o -> handleTextAndFocus(isFocused()));

        getEditor().setOnKeyTyped(keyEvent -> {
            final KeyCode KEY_CODE = keyEvent.getCode();
            if (keyEvent.isShiftDown() && KEY_CODE == KeyCode.ENTER) {
                setPressed(true);
                System.out.println("check");
            }
        });
    }


    // ******************** Methods *******************************************
    @Override protected void layoutChildren() {
        super.layoutChildren();
    }

    public boolean hasSelection() { return null != hasSelection; }
    public void setHasSelection(final boolean HAS_SELECTION) { hasSelectionProperty().set(HAS_SELECTION); }
    public BooleanProperty hasSelectionProperty() {
        if (null == hasSelection) {
            hasSelection = new BooleanPropertyBase(false) {
                @Override protected void invalidated() { pseudoClassStateChanged(HAS_SELECTION, get()); }
                @Override public Object getBean() { return DateTimePicker.this; }
                @Override public String getName() { return "hasSelection"; }
            };
        }
        return hasSelection;
    }

    public LocalDateTime getDateTimeValue() { return dateTime.get(); }
    public void setDateTime(final LocalDateTime DATE_TIME) { dateTime.set(DATE_TIME); }
    public ObjectProperty<LocalDateTime> dateTimeProperty() { return dateTime; }

    public String getFormat() { return format.get(); }
    public void setFormat(final String FORMAT) { format.set(FORMAT); }
    public ObjectProperty<String> formatProperty() { return format; }

    private void simulateEnterPressed() {
        getEditor().fireEvent(new KeyEvent(getEditor(), getEditor(), KeyEvent.KEY_PRESSED, null, null, KeyCode.ENTER, false, false, false, false));
    }


    // ******************** Misc **********************************************
    private void handleTextAndFocus(final boolean IS_FOCUSED) {
        final boolean IS_EMPTY = isEditable() ? getEditor().getText().isEmpty() : null == getValue();
        setHasSelection(!IS_EMPTY);
    }


    // ******************** Internal Classes **********************************
    class InternalConverter extends StringConverter<LocalDate> {
        @Override public String toString(LocalDate object) {
            final LocalDateTime DATE_TIME = getDateTimeValue();
            return null == DATE_TIME ? "" : DATE_TIME.format(formatter);
        }
        @Override public LocalDate fromString(final String VALUE) {
            if (VALUE == null) {
                dateTime.set(null);
                return null;
            }

            dateTime.set(LocalDateTime.parse(VALUE, formatter));
            return dateTime.get().toLocalDate();
        }
    }
}
