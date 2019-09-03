package WordAnalyze;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class WordAnalyzer {
    private PushbackReader reader;
    public String token;
    public String num;
    public int count = 0;
    public Symbols symbol;
    public Errors error;
    public int lineOffset = 1;
    public int wordOffset = 0;
    private char ch;
    private Map<String, Symbols> reserves = new HashMap<String, Symbols>() {
        {
            put("const", Symbols.Const);
            put("int", Symbols.Int);
            put("float", Symbols.Float);
            put("void", Symbols.Void);
            put("if", Symbols.If);
            put("else", Symbols.Else);
            put("while", Symbols.While);
            put("main", Symbols.Main);
            put("return", Symbols.Return);
            put("scanf", Symbols.Scanf);
            put("printf", Symbols.Printf);
        }
    };

    private Map<String, Symbols> errors = new HashMap<String, Symbols>() {
        {
            put("const", Symbols.Const);
            put("int", Symbols.Int);
            put("void", Symbols.Void);
            put("if", Symbols.If);
            put("else", Symbols.Else);
            put("while", Symbols.While);
            put("main", Symbols.Main);
            put("return", Symbols.Return);
            put("scanf", Symbols.Scanf);
            put("printf", Symbols.Printf);
        }
    };

    public enum Symbols {
        Identifier, Const, Int, Float, Void,
        If, Else, While, Main, Return,
        Printf, Scanf, Plus, Multi, Minus, Div,
        UnsignedInt, UnsignedFloat, Greater, GreaterOrEqual,
        Less, LessOrEqual, NotEqual, Equal, StringLiteral,
        LeftBrace, RightBrace, LeftParenthesis, RightParenthesis,
        Comma, Semicolon, Assign, EOF, Unknown
    }

    public enum Errors {
        EOF, InvalidIdentifier, UnknownSeparator, NumberStartFromZero, NumberOutOfRange,
        FloatOutOfRange, InvalidStringLiteral, InvalidEscape, Internal, ExclamationError
    }

    public WordAnalyzer(String source) throws FileNotFoundException {
        reader = new PushbackReader(new FileReader(System.getProperty("user.dir") + source));
    }

    public String getType() {
        switch (symbol) {
            case Identifier:
                return "标识符";
            case Const:
            case Int:
            case Float:
            case Void:
            case If:
            case Else:
            case While:
            case Main:
            case Return:
            case Printf:
            case Scanf:
                return "关键字";
            case Plus:
            case Multi:
            case Minus:
            case Div:
            case Greater:
            case Less:
                return "字符运算符";
            case LeftBrace:
            case RightBrace:
            case LeftParenthesis:
            case RightParenthesis:
            case Comma:
            case Semicolon:
            case Assign:
                return "分界符";
            case UnsignedInt:
                return "无符号整数常量";
            case UnsignedFloat:
                return "无符号浮点数常量";
            case GreaterOrEqual:
            case LessOrEqual:
            case Equal:
            case NotEqual:
                return "双字符运算符";
            case StringLiteral:
                return "字符串常量";
            default:
                return "未知";
        }
    }

    private Symbols isReserved() {
        Symbols symbol = reserves.get(token);
        if (symbol == null) {
            symbol = Symbols.Identifier;
        }
        return symbol;
    }

    public String transIntNum(boolean subzero) {
        if (subzero) {
            return new BigInteger(token, 10).negate().toString(2);
        }
        return new BigInteger(token, 10).toString(2);
    }

    public String transFloatNum(boolean subzero) {
        float num = new Float(token);
        if (subzero) {
            num = -num;
        }
        if (Float.isInfinite(num)) {
            error = Errors.FloatOutOfRange;
        }
        return Integer.toBinaryString(Float.floatToIntBits(num));
    }

    private void read() {
        try {
            ch = (char) reader.read();
            ++wordOffset;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void unread() throws IOException {
        reader.unread(ch);
        --wordOffset;
    }

    private boolean isSpace() {
        return ch == ' ' || ch == '\r';
    }

    private boolean isNewLine() {
        if(ch == '\n') {
            ++lineOffset;
            wordOffset = 0;
            return true;
        }
        return false;
    }

    private boolean isTab() {
        return ch == '\t';
    }

    private boolean isLetter() {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || ch == '_';
    }

    private boolean isDigit() {
        return ch >= '0' && ch <= '9';
    }

    private boolean isZero() {
        return ch == '0';
    }

    private void clearToken() {
        token = "";
    }

    private void catToken() {
        token = token + ch;
    }

    public Symbols getsym() throws IOException {
        ++count;
        read();
        clearToken();
        while (isSpace() || isNewLine() || isTab()) {
            read();
        }
        if (isLetter()) {
            while (isLetter() || isDigit()) {
                catToken();
                read();
            }
            unread();
            symbol = isReserved();
        } else if (isDigit()) {
            boolean decimal = false;
            if (isZero()) {
                catToken();
                read();
                if (isDigit()) {
                    symbol = Symbols.Unknown;
                    error = Errors.NumberStartFromZero;
                }
            }
            while (isDigit() || ch == '.') {
                if (ch == '.') {
                    if (!decimal) {
                        decimal = true;
                    } else {
                        error = Errors.UnknownSeparator;
                        return Symbols.Unknown;
                    }
                }
                catToken();
                read();
            }
            unread();
            if (!decimal) {
                num = transIntNum(false);
                symbol = Symbols.UnsignedInt;
                if (num.length() > 32) {
                    error = Errors.NumberOutOfRange;
                }
            } else {
                num = transFloatNum(false);
                symbol = Symbols.UnsignedFloat;
            }
        } else if (ch == '"') {
            read();
            while (ch != '"') {
                catToken();
                read();
            }
            symbol = Symbols.StringLiteral;
        } else if (ch == '=') {
            catToken();
            read();
            if (ch == '=') {
                symbol = Symbols.Equal;
                catToken();
            } else {
                unread();
                symbol = Symbols.Assign;
            }
        } else if (ch == '!') {
            catToken();
            read();
            if (ch == '=') {
                symbol = Symbols.NotEqual;
                catToken();
            } else {
                unread();
                symbol = Symbols.Unknown;
                error = Errors.UnknownSeparator;
            }
        } else if (ch == '<') {
            catToken();
            read();
            if (ch == '=') {
                symbol = Symbols.LessOrEqual;
                catToken();
            } else {
                unread();
                symbol = Symbols.Less;
            }
        } else if (ch == '>') {
            catToken();
            read();
            if (ch == '=') {
                symbol = Symbols.GreaterOrEqual;
                catToken();
            } else {
                unread();
                symbol = Symbols.GreaterOrEqual;
            }
        } else if (ch == '+') {
            catToken();
            symbol = Symbols.Plus;
        } else if (ch == '-') {
            catToken();
            symbol = Symbols.Minus;
        } else if (ch == '*') {
            catToken();
            symbol = Symbols.Multi;
        } else if (ch == '(') {
            catToken();
            symbol = Symbols.LeftParenthesis;
        } else if (ch == ')') {
            catToken();
            symbol = Symbols.RightParenthesis;
        } else if (ch == '{') {
            catToken();
            symbol = Symbols.LeftBrace;
        } else if (ch == '}') {
            catToken();
            symbol = Symbols.RightBrace;
        } else if (ch == ',') {
            catToken();
            symbol = Symbols.Comma;
        } else if (ch == ';') {
            catToken();
            symbol = Symbols.Semicolon;
        } else if (ch == '/') {
            catToken();
            symbol = Symbols.Div;
        } else if (ch == 65535) {
            symbol = Symbols.EOF;
        } else {
            symbol = Symbols.Unknown;
        }
        return symbol;
    }
}
