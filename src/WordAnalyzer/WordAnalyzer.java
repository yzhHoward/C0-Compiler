package WordAnalyzer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WordAnalyzer {
    private PushbackReader reader;
    public String token;
    public String num;
    public int count = 0;
    public WordSymbol symbol;
    public int lineOffset = 1;
    public int wordOffset = 1;
    private char ch;
    private ArrayList<Integer> wordOffsetOfLine = new ArrayList<>();
    private Map<String, WordSymbol> reserves = new HashMap<String, WordSymbol>() {
        {
            put("const", WordSymbol.Const);
            put("void", WordSymbol.Void);
            put("int", WordSymbol.Int);
            put("char", WordSymbol.Char);
            put("double", WordSymbol.Double);
            put("struct", WordSymbol.Struct);
            put("if", WordSymbol.If);
            put("else", WordSymbol.Else);
            put("switch", WordSymbol.Switch);
            put("case", WordSymbol.Case);
            put("default", WordSymbol.Default);
            put("while", WordSymbol.While);
            put("for", WordSymbol.For);
            put("do", WordSymbol.Do);
            put("return", WordSymbol.Return);
            put("break", WordSymbol.Break);
            put("continue", WordSymbol.Continue);
            put("scan", WordSymbol.Scan);
            put("print", WordSymbol.Print);
        }
    };

    public WordAnalyzer(String source) {
        try {
            reader = new PushbackReader(new FileReader(System.getProperty("user.dir") + source));
        } catch (FileNotFoundException e) {
            System.out.println("目标文件不存在！");
        }
    }

    public String getType() {
        switch (symbol) {
            case UnsignedInt:
                return "无符号整数常量";
            case Identifier:
                return "标识符";
            case Const:
            case Void:
            case Int:
            case Char:
            case If:
            case Else:
            case While:
            case Return:
            case Print:
            case Scan:
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

    private void unread() {
        try {
            reader.unread(ch);
            --wordOffset;
            if (ch == '\n') {
                --lineOffset;
                getWordOffset();
            }
        } catch (IOException e) {
            System.out.println("不能unread!");
        }
    }

    private void read() {
        try {
            boolean newLine = false;
            if (ch == '\n') {
                newLine = true;
            }
            ch = (char) reader.read();
            ++wordOffset;
            if (newLine) {
                addWordOffset();
                ++lineOffset;
                wordOffset = 1;
            }
        } catch (IOException e) {
            System.out.println("读出错！");
        }
    }

    private void addWordOffset() {
        if (lineOffset - 1 < wordOffsetOfLine.size()) {
            wordOffsetOfLine.set(lineOffset - 1, wordOffset);
        } else {
            wordOffsetOfLine.add(wordOffset);
        }
    }

    private void getWordOffset() {
        wordOffset = wordOffsetOfLine.get(lineOffset - 1);
    }

    private WordSymbol isReserved() {
        WordSymbol symbol = reserves.get(token);
        if (symbol == null) {
            symbol = WordSymbol.Identifier;
        }
        return symbol;
    }

    public WordSymbol getsym() throws WordException {
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
            if (isZero()) {
                catToken();
                read();
                if (isDigit()) {
                    symbol = WordSymbol.Unknown;
                    throw new WordException(WordError.NumberStartFromZero);
                } else if (ch == 'x' || ch == 'X') {
                    catToken();
                    read();
                    while (isHexadecimalDigit()) {
                        catToken();
                        read();
                    }
                    if (isLetter()) {
                        throw new WordException(WordError.InvalidHexNumber);
                    }
                    unread();
                    if (checkHexOverFlow()) {
                        throw new WordException(WordError.NumberOutOfRange);
                    }
                } else if (isLetter()) {
                    throw new WordException(WordError.InvalidIdentifier);
                } else {
                    unread();
                }
            } else {
                while (isDigit()) {
                    catToken();
                    read();
                }
                if (isLetter()) {
                    throw new WordException(WordError.InvalidIdentifier);
                }
                unread();
                if (checkIntOverFlow()) {
                    throw new WordException(WordError.NumberOutOfRange);
                }
            }
            symbol = WordSymbol.UnsignedInt;
        } else if (ch == '"') {
            read();
            while (ch != '"') {
                if (ch == '\\') {
                    read();
                    if (ch == 'x') {
                        String str = "";
                        read();
                        if (!isHexadecimalDigit()) {
                            catToken();
                            throw new WordException(WordError.InvalidStringLiteral);
                        }
                        str += ch;
                        read();
                        if (!isHexadecimalDigit()) {
                            catToken();
                            throw new WordException(WordError.InvalidStringLiteral);
                        }
                        str += ch;
                        ch = (char) Integer.parseInt(str, 16);
                    } else if (isEscapeChar()) {
                        switch (ch) {
                            case 'n':
                                ch = '\n';
                                break;
                            case 'r':
                                ch = '\r';
                                break;
                            case 't':
                                ch = '\t';
                                break;
                        }
                    } else {
                        catToken();
                        throw new WordException(WordError.InvalidEscape);
                    }
                } else if (ch == 65535) {
                    throw new WordException(WordError.InvalidStringLiteral);
                } else if (ch != '\t' && (ch <= 31 || ch >= 127)) {
                    throw new WordException(WordError.InvalidStringLiteral);
                }
                catToken();
                read();
            }
            symbol = WordSymbol.StringLiteral;
        } else if (ch == '\'') {
            read();
            if (ch == '\\') {
                read();
                if (ch == 'x') {
                    String str = "";
                    read();
                    if (!isHexadecimalDigit()) {
                        catToken();
                        throw new WordException(WordError.InvalidCharLiteral);
                    }
                    str += ch;
                    read();
                    if (!isHexadecimalDigit()) {
                        catToken();
                        throw new WordException(WordError.InvalidCharLiteral);
                    }
                    str += ch;
                    ch = (char) Integer.parseInt(str, 16);
                } else if (isEscapeChar()) {
                    switch (ch) {
                        case 'n':
                            ch = '\n';
                            break;
                        case 'r':
                            ch = '\r';
                            break;
                        case 't':
                            ch = '\t';
                            break;
                    }
                } else {
                    catToken();
                    throw new WordException(WordError.InvalidEscape);
                }
                catToken();
                read();
                if (ch != '\'') {
                    catToken();
                    throw new WordException(WordError.InvalidCharLiteral);
                }
            } else if (ch == '\'') {
                throw new WordException(WordError.InvalidCharLiteral);
            } else if (ch == 65535) {
                throw new WordException(WordError.InvalidCharLiteral);
            } else if (ch != '\t' && (ch <= 31 || ch >= 127)) {
                throw new WordException(WordError.InvalidCharLiteral);
            } else {
                catToken();
                read();
                if (ch != '\'') {
                    catToken();
                    throw new WordException(WordError.InvalidCharLiteral);
                }
            }
            symbol = WordSymbol.CharLiteral;
        } else if (ch == '=') {
            catToken();
            read();
            if (ch == '=') {
                symbol = WordSymbol.Equal;
                catToken();
            } else {
                unread();
                symbol = WordSymbol.Assign;
            }
        } else if (ch == '!') {
            catToken();
            read();
            if (ch == '=') {
                symbol = WordSymbol.NotEqual;
                catToken();
            } else {
                unread();
                symbol = WordSymbol.Unknown;
                throw new WordException(WordError.UnknownSeparator);
            }
        } else if (ch == '<') {
            catToken();
            read();
            if (ch == '=') {
                symbol = WordSymbol.LessOrEqual;
                catToken();
            } else {
                unread();
                symbol = WordSymbol.Less;
            }
        } else if (ch == '>') {
            catToken();
            read();
            if (ch == '=') {
                symbol = WordSymbol.GreaterOrEqual;
                catToken();
            } else {
                unread();
                symbol = WordSymbol.GreaterOrEqual;
            }
        } else if (ch == '+') {
            catToken();
            symbol = WordSymbol.Plus;
        } else if (ch == '-') {
            catToken();
            symbol = WordSymbol.Minus;
        } else if (ch == '*') {
            catToken();
            symbol = WordSymbol.Multi;
        } else if (ch == '(') {
            catToken();
            symbol = WordSymbol.LeftParenthesis;
        } else if (ch == ')') {
            catToken();
            symbol = WordSymbol.RightParenthesis;
        } else if (ch == '{') {
            catToken();
            symbol = WordSymbol.LeftBrace;
        } else if (ch == '}') {
            catToken();
            symbol = WordSymbol.RightBrace;
        } else if (ch == ',') {
            catToken();
            symbol = WordSymbol.Comma;
        } else if (ch == ';') {
            catToken();
            symbol = WordSymbol.Semicolon;
        } else if (ch == '/') {
            catToken();
            read();
            if (ch == '/') {
                while (ch != '\n') {
                    read();
                }
                getsym();
            } else if (ch == '*') {
                label1:
                while (true) {
                    read();
                    while (ch != '*') {
                        if (ch == 65535) {
                            throw new WordException(WordError.UnfinishedComment);
                        }
                        read();
                    }
                    while (ch == '*') {
                        read();
                        if (ch == '/') {
                            break label1;
                        } else if (ch == 65535) {
                            throw new WordException(WordError.UnfinishedComment);
                        }
                    }
                }
                getsym();
            } else {
                unread();
                symbol = WordSymbol.Div;
            }
        } else if (ch == 65535) {
            symbol = WordSymbol.EOF;
        } else {
            symbol = WordSymbol.Unknown;
        }
        return symbol;
    }

    private boolean checkIntOverFlow() {
        BigInteger bigInteger = new BigInteger(token, 10);
        String str = bigInteger.toString(16);
        token = "0x" + str;
        return bigInteger.bitLength() >= 32;
    }

    private boolean checkHexOverFlow() {
        BigInteger bigInteger = new BigInteger(token.substring(2), 16);
        String str = bigInteger.toString(16);
        token = "0x" + str;
        return bigInteger.bitLength() >= 32;
    }

    private boolean isNewLine() {
        return ch == '\n';
    }

    public boolean isEscapeChar() {
        return ch == '\\' || ch == '\'' || ch == '"' || ch == 'n' || ch == 'r' || ch == 't';
    }

    private boolean isSpace() {
        return ch == ' ' || ch == '\r';
    }

    private boolean isHexadecimalDigit() {
        return isDigit() || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
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
}
