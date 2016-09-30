package eu.hansolo.fx.darkandlightside.tableview;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Pattern;


public class NumberField extends TextFieldFX {
    public  static final Locale  DEFAULT_LOCALE     = new Locale("de", "CH");
    public  static final Pattern PERCENTAGE_PATTERN = Pattern.compile("^([0-9]{1,2}|100(\\.[0-9]*)|100){1}(\\.[0-9]*)?$");
    private              Pattern                    numericPattern;
    private              Locale                     locale;
    private              NumberFormat               numberFormat;
    private              NumberStringConverter      converter;
    private              ObjectProperty<BigDecimal> value;
    private              IntegerProperty            decimals;
    private              StringProperty             hint;
    private              BooleanProperty            percentageMode;
    private              StringProperty             unit;
    private              Text                       unitText;
    private              BooleanProperty            invalidPercentage;
    private              String                     invalidPercentageText;


    // ******************** Constructors **************************************
    public NumberField() {
        this(BigDecimal.ZERO, "", "", DEFAULT_LOCALE, 2);
    }
    public NumberField(final BigDecimal VALUE) {
        this(VALUE, "", "", DEFAULT_LOCALE, 2);
    }
    public NumberField(final Locale LOCALE) {
        this(BigDecimal.ZERO, "", "", LOCALE, 2);
    }
    public NumberField(final Locale LOCALE, final int DECIMALS) {
        this(BigDecimal.ZERO, "", "", LOCALE, DECIMALS);
    }
    public NumberField(final BigDecimal VALUE, final String PROMPT_TEXT, final String HINT_TEXT, final Locale LOCALE, final int DECIMALS) {
        super();
        locale                = LOCALE;
        unit                  = new StringPropertyBase("") {
            @Override protected void invalidated() { unitText.setText(get()); }
            @Override public Object getBean() { return NumberField.this; }
            @Override public String getName() { return "unit"; }
        };
        value                 = new ObjectPropertyBase<BigDecimal>(VALUE) {
            @Override public Object getBean() { return NumberField.this; }
            @Override public String getName() { return "value"; }
        };
        decimals              = new IntegerPropertyBase(clamp(0, 10, DECIMALS)) {
            @Override protected void invalidated() {
                numberFormat.setMinimumFractionDigits(get());
                numberFormat.setMaximumFractionDigits(get());
                setText(numberFormat.format(NumberField.this.getValue().doubleValue()));
            }
            @Override public Object getBean() { return NumberField.this; }
            @Override public String getName() { return "decimals"; }
        };
        hint                  = new SimpleStringProperty(NumberField.this, "hint", HINT_TEXT);
        percentageMode        = new BooleanPropertyBase(false) {
            @Override protected void invalidated() { setUnit(get() ? "%" : ""); }
            @Override public Object getBean() { return NumberField.this; }
            @Override public String getName() { return "percentageMode"; }
        };
        invalidPercentage     = new BooleanPropertyBase(false) {
            @Override public Object getBean() { return NumberField.this; }
            @Override public String getName() { return "invalidPercentage"; }
        };
        invalidPercentageText = getLocale().getLanguage().equals("de") ? "Nummer zwischen 0-100" : "Number between 0-100";
        converter             = new NumberStringConverter(locale);

        numberFormat = NumberFormat.getInstance(LOCALE);
        numberFormat.setMinimumFractionDigits(getDecimals());
        numberFormat.setMaximumFractionDigits(getDecimals());
        char separator  = ((DecimalFormat)numberFormat).getDecimalFormatSymbols().getDecimalSeparator();
        numericPattern = Pattern.compile(separator == '.' ? "^([-]?\\d*\\.?\\d*)$" : "^([-]?\\d*,?\\d*)$");

        StringBuilder defaultNumber = new StringBuilder("0.");
        for (int i = 0 ; i < getDecimals() ; i++) { defaultNumber.append("0"); }

        setPrefColumnCount(12);
        setEditable(true);
        setText(VALUE.toString().toString().trim().matches(numericPattern.pattern()) ? numberFormat.format(VALUE) : defaultNumber.toString());
        setPromptText(PROMPT_TEXT);

        initGraphics();
        registerListeners();
        initBindings();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        getStylesheets().add(NumberField.class.getResource("styles.css").toExternalForm());
        getStyleClass().add("number-field");

        unitText = new Text(getUnit());
        unitText.getStyleClass().add("unit");
        setLeft(unitText);
    }

    private void registerListeners() {
        // Submit on ENTER
        setOnKeyPressed(e -> {
            final KeyCode CODE = e.getCode();
            if (KeyCode.ENTER == CODE) {
                if (getText().trim().isEmpty()) {
                    setValue(BigDecimal.ZERO);
                } else {
                    setValue(new BigDecimal(converter.fromString(getText().trim()).doubleValue()));
                }
                setText(null == getValue() ? "" : numberFormat.format(converter.fromString(getText().trim()).doubleValue()));
                selectRange(getLength(), 0);
                e.consume();
            } else if (e.isControlDown() && KeyCode.A == CODE) {
                selectRange(getLength(), 0);
                e.consume();
            } else if (e.isMetaDown() && KeyCode.A == CODE) {
                selectRange(getLength(), 0);
                e.consume();
            }
        });

        // Select range when double click with mouse
        setOnMouseClicked(event -> { if (event.getClickCount() == 2) selectRange(getLength(), 0); });

        // Validate each key typed
        addEventFilter(KeyEvent.KEY_TYPED, e -> {
            DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) numberFormat).getDecimalFormatSymbols();
            String               separator            = Character.toString(decimalFormatSymbols.getDecimalSeparator());
            String               character            = e.getCharacter();
            TextField            textField            = (TextField) e.getSource();
            String               text                 = textField.getText().trim();
            int                  decimalSeparatorPos  = text.lastIndexOf(separator);
            int                  length               = text.length();
            int                  noOfDecimals         = decimalSeparatorPos == -1 ? 0 : (length - decimalSeparatorPos - 1);
            String               pattern              = ".".equals(separator) ? "[0-9.-]" : "[0-9,-]";

            // Check if input contains only numbers, minus or dot
            if (character.matches(pattern)) {
                if (character.matches("-") && length > 1) {
                    // Minus is only allowed in the beginning -> consume
                    e.consume();
                } else if (text.startsWith("-") && character.matches("-")) {
                    // Minus is already present -> consume
                    e.consume();
                } else if(text.contains(separator) && character.matches(String.join("", "[", separator, "]"))) {
                    // There is a already a decimal separator -> consume
                    e.consume();
                } else if (!isPercentageMode() && noOfDecimals >= getDecimals() && textField.getCaretPosition() > decimalSeparatorPos) {
                    // More decimals than allowed -> consume
                    e.consume();
                }
            } else {
                e.consume();
            }
        });

        // Submit on focus lost
        focusedProperty().addListener(o -> {
            if (isFocused()) {
                // Focus gain
                Platform.runLater(() -> selectRange(getLength(), 0));
            } else {
                // Focus lost
                if(valueProperty().isBound()) return;
                if (getText().trim().isEmpty()) {
                    setValue(BigDecimal.ZERO);
                    setInvalidPercentage(false);
                } else {
                    if (isPercentageMode()) {
                        if (getText().matches(PERCENTAGE_PATTERN.pattern()) && Double.compare(converter.fromString(getText()).doubleValue(), 100) <= 0) {
                            setValue(new BigDecimal(converter.fromString(getText().trim()).doubleValue()));
                            setInvalidPercentage(false);
                        } else {
                            setInvalidPercentage(true);
                            Platform.runLater(() -> {
                                requestFocus();
                                selectAll();
                            });
                        }
                    } else {
                        setValue(new BigDecimal(converter.fromString(getText().trim()).doubleValue()));
                        setInvalidPercentage(false);
                    }
                }
                setText(null == getValue() ? "" : numberFormat.format(converter.fromString(getText().isEmpty() ? "0" : getText().trim()).doubleValue()));
            }
        });
        percentageModeProperty().addListener(o -> setHintText(isPercentageMode() ? invalidPercentageText : getHintText()));
    }

    private void initBindings() {
        Bindings.bindBidirectional(textProperty(), valueProperty(), new BigDecimalStringConverter());
    }


    // ******************** Methods *******************************************
    public String getHintText() { return hint.get(); }
    public void setHintText(final String TEXT) { hint.set(TEXT); }
    public StringProperty hintTextProperty() { return hint; }

    public String getUnit() { return unit.get(); }
    public void setUnit(final String UNIT) { unit.set(UNIT); }
    public StringProperty unitProperty() { return unit; }

    public BigDecimal getValue() { return value.get(); }
    public void setValue(final BigDecimal VALUE) { value.set(VALUE); }
    public ObjectProperty<BigDecimal> valueProperty() { return value; }

    public int getDecimals() { return decimals.get(); }
    public void setDecimals(final int DECIMALS) { decimals.set(DECIMALS); }
    public IntegerProperty decimalsProperty() { return decimals; }

    public boolean isPercentageMode() { return percentageMode.get(); }
    public void setPercentageMode(final boolean MODE) { percentageMode.set(MODE); }
    public BooleanProperty percentageModeProperty() { return percentageMode; }

    public Locale getLocale() { return locale; }

    public NumberFormat getNumberFormat() { return numberFormat; }
    public String getFormattedString() { return null == getValue() ? "" : numberFormat.format(getValue()); }

    public Pattern getNumericPattern() { return numericPattern; }

    @Override public void replaceText(final int START, final int END, final String TEXT) {
        if (END > getLength()) return;
        super.replaceText(START, END, TEXT);
    }

    protected boolean isInvalidPercentage() { return invalidPercentage.get(); }
    protected void setInvalidPercentage(final boolean INVALID) { invalidPercentage.set(INVALID); }
    protected BooleanProperty invalidPercentageProperty() { return invalidPercentage; }

    private static final <T extends Number> T clamp(final T MIN, final T MAX, final T VALUE) {
        if (VALUE.doubleValue() < MIN.doubleValue()) return MIN;
        if (VALUE.doubleValue() > MAX.doubleValue()) return MAX;
        return VALUE;
    }


    // ******************** Internal Classes **********************************
    public class BigDecimalStringConverter extends StringConverter<BigDecimal> {
        private DecimalFormat decimalFormat;
        private NumberFormat  numberFormat;

        public BigDecimalStringConverter() {
            this(getLocale());
        }
        public BigDecimalStringConverter(final Locale LOCALE) {
            decimalFormat = new DecimalFormat("0.00", new DecimalFormatSymbols(LOCALE));
            decimalFormat.setMinimumFractionDigits(getDecimals());
            decimalFormat.setMaximumFractionDigits(getDecimals());
            decimalFormat.setParseBigDecimal(true);

            numberFormat = NumberFormat.getInstance(LOCALE);
            numberFormat.setMinimumFractionDigits(getDecimals());
            numberFormat.setMaximumFractionDigits(getDecimals());

            String pattern = (((DecimalFormat) numberFormat).toPattern()).trim();

            decimalFormat.applyPattern(pattern);

            registerListeners();
        }

        private void registerListeners() {
            decimalsProperty().addListener(o -> {
                decimalFormat.setMinimumFractionDigits(getDecimals());
                decimalFormat.setMaximumFractionDigits(getDecimals());
            });
        }

        @Override public String toString(final BigDecimal VALUE) {
            if( VALUE == null) return "0"; // default
            return numberFormat.format(VALUE);
        }

        @Override public BigDecimal fromString(final String TEXT) {
            if (TEXT == null || TEXT.isEmpty() || TEXT.equals("-")) return BigDecimal.ZERO; // default
            try {
                return (BigDecimal) decimalFormat.parse(TEXT);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
