package eu.hansolo.fx.darkandlightside.tableview;


public class CellField {
    private static StringBuilder text = new StringBuilder("");

    public  static String getText() { return text.toString(); }
    public  static void setText(String text) { CellField.text = new StringBuilder(text); }

    //true, if length of more than one character
    public static boolean isLessOrEqualOneSym() { return CellField.text.length() <= 1; }

    //add character to the end of line
    public static void addSymbol(String symbol) { text.append(symbol); }
    public static void clearText() { setText(""); }
}
