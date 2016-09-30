package eu.hansolo.fx.darkandlightside.tableview;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import java.util.Currency;
import java.util.Locale;
import java.util.regex.Pattern;


public class CurrencyField extends TextFieldFX {
    public  static final Locale                     DEFAULT_LOCALE  = new Locale("de", "CH");
    public  static final Pattern                    NUMERIC_PATTERN = Pattern.compile("^([-]?\\d*\\.?\\d*)$");
    private              Locale                     locale;
    private              NumberStringConverter      converter;
    private              NumberFormat               numberFormat;
    private              ObjectProperty<BigDecimal> value;
    private              IntegerProperty            decimals;
    private              StringProperty             hint;
    private              ObjectProperty<Currency>   currency;
    private              Text                       currencyCodeText;


    // ******************** Constructors **************************************
    public CurrencyField() {
        this(BigDecimal.ZERO, "", "", DEFAULT_LOCALE, 2);
    }
    public CurrencyField(final BigDecimal VALUE) {
        this(VALUE, "", "", DEFAULT_LOCALE, 2);
    }
    public CurrencyField(final Locale LOCALE) {
        this(BigDecimal.ZERO, "", "", LOCALE, 2);
    }
    public CurrencyField(final Locale LOCALE, final int DECIMALS) {
        this(BigDecimal.ZERO, "", "", LOCALE, DECIMALS);
    }
    public CurrencyField(final BigDecimal VALUE, final String PROMPT_TEXT, final String HINT_TEXT, final Locale LOCALE, final int DECIMALS) {
        super();
        locale                = LOCALE;
        currency              = new ObjectPropertyBase<Currency>(Currency.getInstance(locale)) {
            @Override protected void invalidated() { currencyCodeText.setText(get().getCurrencyCode()); }
            @Override public Object getBean() { return CurrencyField.this; }
            @Override public String getName() { return "currency"; }
        };
        value                 = new ObjectPropertyBase<BigDecimal>(VALUE) {
            @Override public Object getBean() { return CurrencyField.this; }
            @Override public String getName() { return "value"; }
        };
        decimals              = new IntegerPropertyBase(clamp(0, 10, DECIMALS)) {
            @Override protected void invalidated() {
                numberFormat.setMinimumFractionDigits(get());
                numberFormat.setMaximumFractionDigits(get());
                setText(numberFormat.format(CurrencyField.this.getValue().doubleValue()));
            }
            @Override public Object getBean() { return CurrencyField.this; }
            @Override public String getName() { return "decimals"; }
        };
        hint                  = new SimpleStringProperty(CurrencyField.this, "hint", HINT_TEXT);
        converter             = new NumberStringConverter(locale);
        numberFormat          = NumberFormat.getInstance(LOCALE);         // Without Currency Symbol
        numberFormat.setMinimumFractionDigits(getDecimals());
        numberFormat.setMaximumFractionDigits(getDecimals());

        StringBuilder defaultNumber = new StringBuilder("0.");
        for (int i = 0 ; i < getDecimals() ; i++) { defaultNumber.append("0"); }

        setPrefColumnCount(12);
        setEditable(true);
        setText(VALUE.toString().toString().trim().matches(NUMERIC_PATTERN.pattern()) ? numberFormat.format(VALUE) : defaultNumber.toString());
        setPromptText(PROMPT_TEXT);

        initGraphics();
        registerListeners();
        initBindings();
    }


    // ******************** Initialization ************************************
    private void initGraphics() {
        getStylesheets().add(CurrencyField.class.getResource("styles.css").toExternalForm());
        getStyleClass().add("currency-field");

        currencyCodeText = new Text(currency.get().getCurrencyCode());
        currencyCodeText.getStyleClass().add("currency-code");
        setLeft(currencyCodeText);
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
            String    decimalSeparator    = Character.toString(decimalFormatSymbols.getDecimalSeparator());
            String    character           = e.getCharacter();
            TextField textField           = (TextField) e.getSource();
            String    text                = textField.getText().trim();
            int       decimalSeparatorPos = text.lastIndexOf(decimalSeparator);
            int       length              = text.length();
            int       noOfDecimals        = decimalSeparatorPos == -1 ? 0 : (length - decimalSeparatorPos - 1);

            // Check if input contains only numbers or dot
            if (character.matches("[0-9.-]")) {
                if (character.matches("-") && length > 1) {
                    // Minus is only allowed in the beginning -> consume
                    e.consume();
                } else if (text.startsWith("-") && character.matches("-")) {
                    // Minus is already present -> consume
                    e.consume();
                } else if(text.contains(decimalSeparator) && character.matches(String.join("", "[", decimalSeparator, "]"))) {
                    // There is a already a decimal separator -> consume
                    e.consume();
                } else if (noOfDecimals >= getDecimals() && textField.getCaretPosition() > decimalSeparatorPos) {
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
                if (valueProperty().isBound()) return;
                setValue(getText().trim().isEmpty() ? BigDecimal.ZERO : new BigDecimal(converter.fromString(getText().trim()).doubleValue()));
                setText(null == getValue() ? "" : numberFormat.format(converter.fromString(getText().isEmpty() ? "0" : getText().trim()).doubleValue()));
            }
        });
    }

    private void initBindings() {
        Bindings.bindBidirectional(textProperty(), valueProperty(), new BigDecimalStringConverter());
    }


    // ******************** Methods *******************************************
    public String getHintText() { return hint.get(); }
    public void setHintText(final String TEXT) { hint.set(TEXT); }
    public StringProperty hintTextProperty() { return hint; }

    public Currency getCurrency() { return currency.get(); }
    public void setCurrency(final Currency CURRENCY) { currency.set(CURRENCY); }
    public ObjectProperty<Currency> currencyProperty() { return currency; }

    public BigDecimal getValue() { return value.get(); }
    public void setValue(final BigDecimal VALUE) { value.set(VALUE); }
    public ObjectProperty<BigDecimal> valueProperty() { return value; }

    public int getDecimals() { return decimals.get(); }
    public void setDecimals(final int DECIMALS) { decimals.set(DECIMALS); }
    public IntegerProperty decimalsProperty() { return decimals; }

    public Locale getLocale() { return locale; }

    public NumberFormat getNumberFormat() { return numberFormat; }
    public String getFormattedString() { return null == getValue() ? "" : numberFormat.format(getValue()); }

    @Override public void replaceText(final int START, final int END, final String TEXT) {
        if (END > getLength()) return;
        super.replaceText(START, END, TEXT);
    }

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
            this(DEFAULT_LOCALE);
        }
        public BigDecimalStringConverter(final Locale LOCALE) {
            decimalFormat = new DecimalFormat("0.00", new DecimalFormatSymbols(LOCALE));
            decimalFormat.setCurrency(getCurrency());
            decimalFormat.setMinimumFractionDigits(getDecimals());
            decimalFormat.setMaximumFractionDigits(getDecimals());
            decimalFormat.setParseBigDecimal(true);

            numberFormat = NumberFormat.getInstance(LOCALE);
            numberFormat.setMinimumFractionDigits(getDecimals());
            numberFormat.setMaximumFractionDigits(getDecimals());

            String pattern = (((DecimalFormat) numberFormat).toPattern()).replace("\u00A4", "").trim();
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
