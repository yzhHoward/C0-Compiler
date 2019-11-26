package SyntaxAnalyzer;

import SymbolTable.DataType;
import SymbolTable.FunctionSymbol;
import SymbolTable.Symbol;
import SymbolTable.SymbolType;
import WordAnalyzer.WordAnalyzer;
import WordAnalyzer.WordException;
import WordAnalyzer.WordSymbol;

import java.math.BigInteger;
import java.util.ArrayList;

import static SymbolTable.SymbolTable.*;
import static SyntaxAnalyzer.Utils.tokenToDataType;

public class SyntaxAnalyzer {
    public String token;
    public int lineOffset;
    public int wordOffset;
    public Errors error;
    private WordAnalyzer wordAnalyzer;
    private PCodeWriter pCodeWriter;
    private WordSymbol wordSymbol;
    private ArrayList<NextWord> buf = new ArrayList<>();
    private int cursor = -1;
    private int level = 0;

    public SyntaxAnalyzer(WordAnalyzer wordAnalyzer) {
        this.wordAnalyzer = wordAnalyzer;
    }

    private void read() {
        ++cursor;
        NextWord nextWord = buf.get(cursor);
        token = nextWord.token;
        wordSymbol = nextWord.symbolType;
        lineOffset = nextWord.lineOffset;
        wordOffset = nextWord.wordOffset;
    }

    private void unread() {
        if (cursor > 0) {
            --cursor;
            NextWord nextWord = buf.get(cursor);
            token = nextWord.token;
            wordSymbol = nextWord.symbolType;
            lineOffset = nextWord.lineOffset;
            wordOffset = nextWord.wordOffset;
        } else if (cursor == 0) {
            --cursor;
            token = null;
            wordSymbol = null;
            lineOffset = 1;
            wordOffset = 0;
        } else {
            error = Errors.UnreadError;
        }
    }

    public void start() {
        try {
            wordAnalyzer.getsym();
            while (wordAnalyzer.symbol != WordSymbol.EOF) {
                buf.add(new NextWord(wordAnalyzer.token, wordAnalyzer.symbol, wordAnalyzer.lineOffset, wordAnalyzer.wordOffset));
                wordAnalyzer.getsym();
            }
        } catch (WordException e) {
            System.out.println(e.getMessage() + " at " + wordAnalyzer.lineOffset + ":" + (wordAnalyzer.wordOffset - token.length() + 1) + " word: " + wordAnalyzer.token);
        }
        nextLevel();
        try {
            program();
        } catch (SyntaxException e) {
            System.out.println(e.getMessage() + " at " + lineOffset + ":" + wordOffset + " word: " + token);
        }
    }

    // 程序
    private void program() throws SyntaxException {
        while (!variableDeclaration()) {
            break;
        }
        read();
        if (wordSymbol == WordSymbol.EOF) {
            if (findFunctionSymbol("main") == null) {
                throw new SyntaxException(Errors.MissingMain);
            }
        }
        /*while (wordSymbol == WordSymbol.Const) {
            unread();
            if (!constantDeclaration()) {
                return;
            }
            read();
        }
        unread();
        while (true) {
            if (!variableDeclaration()) {
                return;
            }
            read();
            read();
            read();
            if (wordSymbol == WordSymbol.LeftBrace) {
                unread();
                unread();
                unread();
                break;
            } else {
                unread();
                unread();
                unread();
            }
        }
        while (functionDefinition()) {
            if (cursor == buf.size() - 1) {
                if (findFunctionSymbol("main", 1) == null) {
                    error = Errors.MissingMain;
                }
                return;
            }
        }*/
    }

    // 常量说明部分
    private boolean constantDeclaration() {
        boolean exist = false;
        DataType dataType;
        String value;
        read();
        if (wordSymbol == WordSymbol.Const) {
            read();
        } else {
            unread();
            return true;
        }
        if (wordSymbol == WordSymbol.Char || wordSymbol == WordSymbol.Int) {
            dataType = tokenToDataType(token);
        } else {
            error = Errors.ExpectType;
            return false;
        }
        while (wordSymbol == WordSymbol.Identifier) {
            exist = true;
            if (findVariableSymbol(token, level) != null) {
                error = Errors.DuplicateSymbol;
                return false;
            }
            String temp = token;
            read();
            if (wordSymbol != WordSymbol.Assign) {
                error = Errors.InvalidConstantDeclaration;
                return false;
            }
            read();
            switch (dataType) {
                case SignedChar:
                    value = String.valueOf(new BigInteger(token).byteValue());
                case SignedInt:
                    value = String.valueOf(new BigInteger(token).intValue());
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + dataType);
            }
            if (!insertVariableSymbol(level, temp, SymbolType.Variable, dataType, lineOffset, wordOffset, value)) {
                error = Errors.DuplicateSymbol;
                return false;
            }
            read();
            if (wordSymbol == WordSymbol.Semicolon) {
                return true;
            } else if (wordSymbol == WordSymbol.Comma) {
                read();
            }
        }
        if (!exist) {
            error = Errors.ExpectIdentifier;
            return false;
        }
        error = Errors.ExpectCorrectSeparator;
        return false;
    }

    // 变量说明部分
    private boolean variableDeclaration() {
        DataType dataType;
        read();
        if (wordSymbol == WordSymbol.Char || wordSymbol == WordSymbol.Int) {
            dataType = tokenToDataType(token);
        } else {
            // 不是声明头部
            unread();
            return true;
        }
        read();
        if (wordSymbol == WordSymbol.Main) {
            unread();
            unread();
            return true;
        }
        if (wordSymbol != WordSymbol.Identifier) {
            error = Errors.ExpectIdentifier;
            return false;
        }
        read();
        if (wordSymbol == WordSymbol.Comma || wordSymbol == WordSymbol.Semicolon) {
            unread();
            String temp;
            int length;
            while (wordSymbol == WordSymbol.Identifier) {
                temp = token;
                read();
                if (!insertVariableSymbol(level, temp, SymbolType.Variable, dataType, lineOffset, wordOffset)) {
                    error = Errors.DuplicateSymbol;
                    return false;
                }
                read();
                if (wordSymbol == WordSymbol.Semicolon) {
                    return true;
                } else if (wordSymbol == WordSymbol.Assign) {
                    error = Errors.AssignWithDeclaration;
                    return false;
                } else if (wordSymbol != WordSymbol.Comma) {
                    error = Errors.ExpectCorrectSeparator;
                    return false;
                }
                read();
            }
            error = Errors.ExpectIdentifier;
            return false;
        } else if (wordSymbol == WordSymbol.LeftParenthesis) {
            // 不是变量声明
            unread();
            unread();
            unread();
            return true;
        } else if (wordSymbol == WordSymbol.Assign) {
            error = Errors.AssignWithDeclaration;
            return false;
        } else {
            error = Errors.InvalidVariableDeclaration; // UnknownError
            return false;
        }
    }

    // 函数定义部分
    private boolean functionDefinition() {
        DataType dataType = null;
        String functionName;
        read();
        if (wordSymbol == WordSymbol.Char || wordSymbol == WordSymbol.Int || wordSymbol == WordSymbol.Float ||
                wordSymbol == WordSymbol.Double || wordSymbol == WordSymbol.Void) {
            dataType = tokenToDataType(token);
        }
        if (dataType == null) {
            if (wordSymbol == WordSymbol.Identifier) {
                error = Errors.ExpectReturnType;
            } else {
                error = Errors.InvalidSentenceSequence;
            }
            return false;
        }
        read();
        if (wordSymbol == WordSymbol.Main) {
            if (dataType != DataType.SignedInt) {
                error = Errors.InvalidReturnType;
                return false;
            }
//            writeSymbols();
        }
        if (wordSymbol != WordSymbol.Main && wordSymbol != WordSymbol.Identifier) {
            error = Errors.ExpectIdentifier;
            return false;
        }
        if (!insertFunctionSymbol(token, SymbolType.Function, dataType, lineOffset, wordOffset)) {
            error = Errors.DuplicateSymbol;
            return false;
        }
        ++level;
        nextLevel();
        functionName = token;
        if (!argument(functionName)) {
            return false;
        }
        read();
        if (wordSymbol == WordSymbol.LeftBrace) {
            read();
            while (wordSymbol == WordSymbol.Const) {
                unread();
                if (!constantDeclaration()) {
                    return false;
                }
                read();
            }
            unread();
            while (true) {
                read();
                if (wordSymbol == WordSymbol.Char || wordSymbol == WordSymbol.Int) {
                    unread();
                } else {
                    break;
                }
                if (!variableDeclaration()) {
                    return false;
                }
            }
            if (!statementSequence()) {
                return false;
            }
        } else {
            error = Errors.MissingFunctionBody;
            return false;
        }
        if (dataType == DataType.Void) {
            pCodeWriter.write("RET", 0, 0);
            return true;
        }
        pCodeWriter.write("LIT", 0, 0);
        pCodeWriter.write("STO", 0, 0);
        pCodeWriter.write("RET", 0, 0);
        return true;
    }

    // 参数
    private boolean argument(String functionName) {
        DataType dataType;
        read();
        if (wordSymbol == WordSymbol.LeftParenthesis) {
            read();
            if (wordSymbol == WordSymbol.RightParenthesis) {
                return true;
            }
            unread();
            while (true) {
                read();
                if (wordSymbol == WordSymbol.Char || wordSymbol == WordSymbol.Int) {
                    dataType = tokenToDataType(token);
                } else {
                    error = Errors.ExpectType;
                    return false;
                }
                read();
                if (wordSymbol != WordSymbol.Identifier) {
                    error = Errors.ExpectIdentifier;
                    return false;
                }
                if (!updateFunctionSymbol(functionName, token, dataType, lineOffset, wordOffset)) {
                    error = Errors.DuplicateSymbol;
                    return false;
                }
                read();
                if (wordSymbol == WordSymbol.RightParenthesis) {
                    return true;
                } else if (wordSymbol != WordSymbol.Comma) {
                    error = Errors.ExpectCorrectSeparator;
                    return false;
                }
            }
        } else {
            error = Errors.InvalidArgumentDeclaration; // Unknown
            return false;
        }
    }

    // 语句序列
    private boolean statementSequence() {
        read();
        if (wordSymbol == WordSymbol.RightBrace) {
            --level;
            prevLevel();
            return true;
        }
        unread();
        while (statement()) {
            if (error != null) {
                return false;
            }
            read();
            if (wordSymbol == WordSymbol.RightBrace) {
                --level;
                prevLevel();
                return true;
            }
            unread();
        }
        return false;
    }

    // 语句 在这里true表示已经成功处理，false表示没有语句或者遇到异常了
    private boolean statement() {
        Symbol symbol;
        read();
        switch (wordSymbol) {
            case If:
                return ifStatement();
            case While:
                return whileStatement();
            case For:
//                return forStatement();
            case LeftBrace:
                ++level;
                nextLevel();
                return statementSequence();
            case Identifier:
                if ((symbol = findVariableSymbol(token)) == null) {
                    error = Errors.SymbolNotFound;
                    return false;
                }
                if (symbol.symbolType == SymbolType.Function) {
                    unread();
                    if (!callFunction()) {
                        return false;
                    }
                    read();
                    if (wordSymbol != WordSymbol.Semicolon) {
                        error = Errors.ExpectCorrectSeparator;
                        return false;
                    }
                    return true;
                } else {
                    unread();
//                    if (!assignStatement()) {
//                        return false;
//                    }
                    read();
                    if (wordSymbol != WordSymbol.Semicolon) {
                        error = Errors.ExpectCorrectSeparator;
                        return false;
                    }
                    return true;
                }
            case Scan:
//                if (!scanf()) {
//                    return false;
//                }
                read();
                if (wordSymbol != WordSymbol.Semicolon) {
                    error = Errors.ExpectCorrectSeparator;
                    return false;
                }
                return true;
            case Print:
//                if (!printf()) {
//                    return false;
//                }
                read();
                if (wordSymbol != WordSymbol.Semicolon) {
                    error = Errors.ExpectCorrectSeparator;
                    return false;
                }
                return true;
            case Return:
//                if (!returnAnalyse()) {
//                    return false;
//                }
                read();
                if (wordSymbol != WordSymbol.Semicolon) {
                    error = Errors.ExpectCorrectSeparator;
                    return false;
                }
            case Semicolon:
                return true;
            default:
                error = Errors.InvalidArgumentDeclaration;
                return false;
                /*System.out.println("这句话不应该出现的");
                return false;*/
        }
    }

    // 条件语句
    private boolean ifStatement() {
        read();
        if (wordSymbol != WordSymbol.LeftParenthesis) {
            error = Errors.ExpectCorrectSeparator;
            return false;
        }
//        if (!condition()) {
//            return false;
//        }
        read();
        if (wordSymbol != WordSymbol.RightParenthesis) {
            error = Errors.ExpectCorrectSeparator;
            return false;
        }
        if (!statement()) {
            return false;
        }
        read();
        if (wordSymbol == WordSymbol.Else) {
            return statement();
        }
        unread();
        return true;
    }

    // 循环语句
    private boolean whileStatement() {
        read();
        if (wordSymbol != WordSymbol.LeftParenthesis) {
            error = Errors.ExpectCorrectSeparator;
            return false;
        }
//        if (!condition()) {
//            return false;
//        }
        read();
        if (wordSymbol != WordSymbol.RightParenthesis) {
            error = Errors.ExpectCorrectSeparator;
            return false;
        }
        return statement();
    }

    // 函数调用语句
    private boolean callFunction() {
        FunctionSymbol functionSymbol;
        read();
        if (wordSymbol != WordSymbol.Identifier) {
            error = Errors.ExpectIdentifier;
            return false;
        }
        if ((functionSymbol = findFunctionSymbol(token)) == null) {
            error = Errors.SymbolNotFound;
            return false;
        }
        read();
        if (wordSymbol != WordSymbol.LeftParenthesis) {
            error = Errors.ExpectCorrectSeparator;
            return false;
        }
        read();
        if (wordSymbol == WordSymbol.RightParenthesis) {
            return true;
        } else {
            unread();
//            while (expression()) {
//                read();
//                if (symbol == WordSymbol.RightParenthesis) {
//                    return true;
//                } else if (symbol != WordSymbol.Comma) {
//                    error = Errors.ExpectCorrectSeparator;
//                    return false;
//                }
//            }
            return false;
        }
    }

    public enum Errors {
        InvalidConstantDeclaration,
        InvalidVariableDeclaration,
        DuplicateSymbol,
        SymbolNotFound,
        ExpectIdentifier,
        ExpectInt32,
        ExpectCorrectSeparator,
        ExpectType,
        ExpectReturnType,
        InvalidFunctionDeclaration,
        InvalidArgumentDeclaration,
        MissingFunctionBody,
        InvalidSentenceSequence,
        InvalidExpression,
        InvalidFactor,
        InvalidIfStatement,
        InvalidWhileStatement,
        InvalidAssignment,
        InvalidReturn,
        InvalidReturnType,
        InvalidScanf,
        InvalidPrintf,
        InvalidCall,
        InvalidCondition,
        LessArguments,
        MoreArguments,
        AssignToConstant,
        AssignToFunction,
        AssignWithDeclaration,
        MissingMain,
        MissingSemicolon,
        MissingSentence,
        ReturnValueForVoidFunction,
        NoReturnValueForIntFunction,
        NotCallingFunction,
        UnsupportedFeature,
        UnreadError
    }
}
