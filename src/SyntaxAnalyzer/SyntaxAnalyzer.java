package SyntaxAnalyzer;

import SymbolTable.DataType;
import SymbolTable.FunctionSymbol;
import SymbolTable.SymbolType;
import SymbolTable.VariableSymbol;
import WordAnalyzer.WordAnalyzer;
import WordAnalyzer.WordException;
import WordAnalyzer.WordSymbol;

import java.util.ArrayList;

import static SymbolTable.SymbolTable.*;
import static SyntaxAnalyzer.Utils.tokenToDataType;

public class SyntaxAnalyzer {
    public String token;
    public int lineOffset;
    public int wordOffset;
    public SyntaxError error;
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
            error = SyntaxError.UnreadError;
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
        int offset = 0;
        while (variableDeclaration(offset)) {
            ++offset;
        }
        offset = 0;
        while (true) {
            read();
            if (wordSymbol == WordSymbol.EOF) {
                unread();
                break;
            }
            if (!functionDefinition(offset)) {
                break;
            }
            ++offset;
        }
        read();
        if (wordSymbol == WordSymbol.EOF) {
            if (findFunctionSymbol("main") == null) {
                throw new SyntaxException(SyntaxError.MissingMain);
            }
        }
    }

    // 变量说明部分
    private boolean variableDeclaration(int offset) throws SyntaxException {
        DataType dataType;
        SymbolType symbolType;
        String identifier;
        read();
        if (wordSymbol == WordSymbol.Const) {
            symbolType = SymbolType.Constant;
            read();
        } else {
            symbolType = SymbolType.Variable;
        }
        if (wordSymbol == WordSymbol.Char) {
            dataType = DataType.Char;
        } else if (wordSymbol == WordSymbol.Int) {
            dataType = DataType.Int;
        } else if (symbolType == SymbolType.Constant) {
            throw new SyntaxException(SyntaxError.InvalidConstantDeclaration);
        } else {
            // 不是变量
            unread();
            return false;
        }
        while (true) {
            boolean initialized = false;
            read();
            if (wordSymbol != WordSymbol.Identifier) {
                throw new SyntaxException(SyntaxError.ExpectIdentifier);
            }
            identifier = token;
            read();
            if (wordSymbol == WordSymbol.Assign) {
                initialized = true;
                expression();
            } else if (symbolType == SymbolType.Constant) {
                throw new SyntaxException(SyntaxError.UninitializedConstant);
            } else if (wordSymbol == WordSymbol.LeftParenthesis) {
                unread();
                unread();
                unread();
                return false;
            } else {
                unread();
            }
            read();
            if (wordSymbol == WordSymbol.Semicolon) {
                insertVariableSymbol(level, identifier, initialized, symbolType, dataType, offset, lineOffset, wordOffset);
                return true;
            } else if (wordSymbol == WordSymbol.Comma) {
                insertVariableSymbol(level, identifier, initialized, symbolType, dataType, offset, lineOffset, wordOffset);
            } else {
                throw new SyntaxException(SyntaxError.ExpectCorrectSeparator);
            }
        }
    }

    // 函数定义部分
    private boolean functionDefinition(int offset) throws SyntaxException {
        DataType dataType = null;
        String functionName;
        read();
        if (wordSymbol == WordSymbol.Char || wordSymbol == WordSymbol.Int || wordSymbol == WordSymbol.Void) {
            dataType = tokenToDataType(token);
        }
        if (dataType == null) {
            if (wordSymbol == WordSymbol.Identifier) {
                throw new SyntaxException(SyntaxError.ExpectReturnType);
            } else {
                throw new SyntaxException(SyntaxError.InvalidSentenceSequence);
            }
        }
        read();
        if (wordSymbol != WordSymbol.Identifier) {
            throw new SyntaxException(SyntaxError.ExpectIdentifier);
        }
        insertFunctionSymbol(token, SymbolType.Function, dataType, offset, lineOffset, wordOffset);
        ++level;
        nextLevel();
        functionName = token;
        parameter(functionName);
        read();
        if (wordSymbol == WordSymbol.LeftBrace) {
            int variableOffset = 0;
            while (variableDeclaration(variableOffset)) {
                ++variableOffset;
            }
            statementSequence();
            read();
            if (wordSymbol == WordSymbol.RightBrace) {
                --level;
                prevLevel();
            } else {
                throw new SyntaxException(SyntaxError.ExpectCorrectSeparator);
            }
        } else {
            throw new SyntaxException(SyntaxError.MissingFunctionBody);
        }
        read();
        if (wordSymbol == WordSymbol.EOF) {
            unread();
            return false;
        } else {
            unread();
            return true;
        }
    }

    // 参数
    private void parameter(String functionName) throws SyntaxException {
        DataType dataType;
        SymbolType symbolType;
        int offset = 0;
        read();
        if (wordSymbol == WordSymbol.LeftParenthesis) {
            read();
            if (wordSymbol == WordSymbol.RightParenthesis) {
                return;
            }
            unread();
            while (true) {
                read();
                if (wordSymbol == WordSymbol.Const) {
                    symbolType = SymbolType.Constant;
                    read();
                } else {
                    symbolType = SymbolType.Variable;
                }
                if (wordSymbol == WordSymbol.Char) {
                    dataType = DataType.Char;
                } else if (wordSymbol == WordSymbol.Int) {
                    dataType = DataType.Int;
                } else {
                    throw new SyntaxException(SyntaxError.ExpectType);
                }
                read();
                if (wordSymbol != WordSymbol.Identifier) {
                    throw new SyntaxException(SyntaxError.ExpectIdentifier);
                }
                updateFunctionSymbol(functionName, token, symbolType, dataType, offset, lineOffset, wordOffset);
                read();
                if (wordSymbol == WordSymbol.RightParenthesis) {
                    return;
                } else if (wordSymbol != WordSymbol.Comma) {
                    throw new SyntaxException(SyntaxError.ExpectCorrectSeparator);
                }
                ++offset;
            }
        } else {
            throw new SyntaxException(SyntaxError.MissingParameter);
        }
    }

    // 语句序列
    private void statementSequence() throws SyntaxException {
        while (true) {
            if (!statement()) {
                break;
            }
        }
    }

    // 语句
    private boolean statement() throws SyntaxException {
        read();
        if (wordSymbol == WordSymbol.RightBrace) {
            unread();
            return false;
        } else if (wordSymbol == WordSymbol.LeftBrace) {
            ++level;
            nextLevel();
            statementSequence();
            read();
            if (wordSymbol == WordSymbol.RightBrace) {
                --level;
                prevLevel();
            }
            return true;
        } else if (wordSymbol == WordSymbol.Identifier) {
            read();
            if (wordSymbol == WordSymbol.LeftParenthesis) {
                unread();
                unread();
                callFunction();
            } else {
                unread();
                unread();
                assignment();
            }
            read();
            if (wordSymbol != WordSymbol.Semicolon) {
                throw new SyntaxException(SyntaxError.MissingSemicolon);
            }
            return true;
        } else {
            switch (wordSymbol) {
                case If:
                    unread();
                    ifStatement();
                    return true;
                case While:
                    unread();
                    whileStatement();
                    return true;
                case Scan:
                    unread();
                    scanStatement();
                    return true;
                case Print:
                    unread();
                    printStatement();
                    return true;
                case Return:
                    unread();
                    returnStatement();
                    return true;
                case Semicolon:
                    return true;
                default:
                    throw new SyntaxException(SyntaxError.InvalidSentenceSequence);
            }
        }
    }

    // 条件
    private void condition() throws SyntaxException {
        expression();
        read();
        if (wordSymbol == WordSymbol.RightParenthesis) {

        } else {
            switch (wordSymbol) {
                case Less:
                    break;
                case LessOrEqual:
                    break;
                case Greater:
                    break;
                case GreaterOrEqual:
                    break;
                case NotEqual:
                    break;
                case Equal:
                    break;
                default:
                    throw new SyntaxException(SyntaxError.InvalidCondition);
            }
            expression();
        }
    }

    // 条件语句
    private void ifStatement() throws SyntaxException {
        read();
        if (wordSymbol != WordSymbol.If) {
            throw new SyntaxException(SyntaxError.InvalidIfStatement);
        }
        read();
        if (wordSymbol != WordSymbol.LeftParenthesis) {
            throw new SyntaxException(SyntaxError.MissingCondition);
        }
        condition();
        read();
        if (wordSymbol != WordSymbol.RightParenthesis) {
            throw new SyntaxException(SyntaxError.ExpectRightParenthesis);
        }
        statement();
        read();
        if (wordSymbol == WordSymbol.Else) {
            statement();
        } else {
            unread();
        }
    }

    // 循环语句
    private void whileStatement() throws SyntaxException {
        read();
        if (wordSymbol != WordSymbol.While) {
            throw new SyntaxException(SyntaxError.InvalidWhileStatement);
        }
        read();
        if (wordSymbol != WordSymbol.LeftParenthesis) {
            throw new SyntaxException(SyntaxError.MissingCondition);
        }
        condition();
        read();
        if (wordSymbol != WordSymbol.RightParenthesis) {
            throw new SyntaxException(SyntaxError.ExpectRightParenthesis);
        }
        statement();
    }

    // 返回语句
    private void returnStatement() throws SyntaxException {
        read();
        if (wordSymbol != WordSymbol.Return) {
            throw new SyntaxException(SyntaxError.InvalidReturn);
        }
        read();
        if (wordSymbol != WordSymbol.Semicolon) {
            expression();
            read();
            if (wordSymbol != WordSymbol.Semicolon) {
                throw new SyntaxException(SyntaxError.MissingSemicolon);
            }
        }
    }

    // 读语句
    private void scanStatement() throws SyntaxException {
        VariableSymbol variableSymbol;
        read();
        if (wordSymbol != WordSymbol.Scan) {
            throw new SyntaxException(SyntaxError.InvalidScan);
        }
        read();
        if (wordSymbol != WordSymbol.LeftParenthesis) {
            throw new SyntaxException(SyntaxError.ExpectLeftParenthesis);
        }
        read();
        if (wordSymbol != WordSymbol.Identifier) {
            throw new SyntaxException(SyntaxError.ExpectIdentifier);
        }
        variableSymbol = findVariableSymbol(token);
        if (variableSymbol == null) {
            throw new SyntaxException(SyntaxError.SymbolNotFound);
        }
        read();
        if (wordSymbol != WordSymbol.RightParenthesis) {
            throw new SyntaxException(SyntaxError.ExpectRightParenthesis);
        }
    }

    // 写语句
    private void printStatement() throws SyntaxException {
        read();
        if (wordSymbol != WordSymbol.Scan) {
            throw new SyntaxException(SyntaxError.InvalidScan);
        }
        read();
        if (wordSymbol != WordSymbol.LeftParenthesis) {
            throw new SyntaxException(SyntaxError.ExpectLeftParenthesis);
        }
        read();
        if (wordSymbol == WordSymbol.StringLiteral) {

        } else {
            expression();
        }
        read();
        if (wordSymbol != WordSymbol.RightParenthesis) {
            throw new SyntaxException(SyntaxError.ExpectRightParenthesis);
        }
    }

    // 赋值语句
    private void assignment() throws SyntaxException {
        VariableSymbol variableSymbol;
        read();
        if (wordSymbol != WordSymbol.Identifier) {
            throw new SyntaxException(SyntaxError.ExpectIdentifier);
        }
        variableSymbol = findVariableSymbol(token);
        if (variableSymbol == null) {
            throw new SyntaxException(SyntaxError.SymbolNotFound);
        } else if (!variableSymbol.isInitialized()) {
            throw new SyntaxException(SyntaxError.UninitializedVariable);
        }
        read();
        if (wordSymbol != WordSymbol.Assign) {
            throw new SyntaxException(SyntaxError.InvalidAssignment);
        }
        expression();
    }

    // 表达式
    private void expression() throws SyntaxException {
        multiplicativeExpression();
        while (true) {
            read();
            if (wordSymbol == WordSymbol.Plus || wordSymbol == WordSymbol.Minus) {
                multiplicativeExpression();
            } else {
                unread();
                break;
            }
        }
    }

    private void multiplicativeExpression() throws SyntaxException {
        castExpression();
        while (true) {
            read();
            if (wordSymbol == WordSymbol.Multi || wordSymbol == WordSymbol.Div) {
                castExpression();
            } else {
                unread();
                break;
            }
        }
    }

    private void castExpression() throws SyntaxException {
        while (true) {
            read();
            if (wordSymbol == WordSymbol.LeftParenthesis) {
                read();
                if (wordSymbol == WordSymbol.Int) {

                } else if (wordSymbol == WordSymbol.Char) {

                } else {
                    throw new SyntaxException(SyntaxError.ExpectTypeSpecifier);
                }
                read();
                if (wordSymbol != WordSymbol.RightParenthesis) {
                    throw new SyntaxException(SyntaxError.ExpectRightParenthesis);
                }
            } else {
                unread();
                break;
            }
        }
        read();
        if (wordSymbol == WordSymbol.Plus || wordSymbol == WordSymbol.Minus) {
            primaryExpression();
        } else {
            unread();
            primaryExpression();
        }
    }

    private void primaryExpression() throws SyntaxException {
        VariableSymbol variableSymbol;
        FunctionSymbol functionSymbol;
        read();
        if (wordSymbol == WordSymbol.LeftParenthesis) {
            expression();
            read();
            if (wordSymbol != WordSymbol.RightParenthesis) {
                throw new SyntaxException(SyntaxError.ExpectRightParenthesis);
            }
        } else if (wordSymbol == WordSymbol.UnsignedInt) {

        } else if (wordSymbol == WordSymbol.CharLiteral) {

        } else if (wordSymbol == WordSymbol.Identifier) {
            variableSymbol = findVariableSymbol(token);
            functionSymbol = findFunctionSymbol(token);
            if (variableSymbol != null) {
                if (!variableSymbol.isInitialized()) {
                    throw new SyntaxException(SyntaxError.UninitializedVariable);
                }
            } else if (functionSymbol != null) {
                unread();
                callFunction();
            } else {
                throw new SyntaxException(SyntaxError.SymbolNotFound);
            }
        }
    }

    // 函数调用语句
    private void callFunction() throws SyntaxException {
        FunctionSymbol functionSymbol;
        read();
        if (wordSymbol != WordSymbol.Identifier) {
            throw new SyntaxException(SyntaxError.ExpectIdentifier);
        }
        functionSymbol = findFunctionSymbol(token);
        if (functionSymbol == null) {
            throw new SyntaxException(SyntaxError.SymbolNotFound);
        }
        read();
        if (wordSymbol != WordSymbol.LeftParenthesis) {
            throw new SyntaxException(SyntaxError.ExpectLeftParenthesis);
        }
        read();
        if (wordSymbol != WordSymbol.RightParenthesis) {
            unread();
            while (true) {
                expression();
                read();
                if (wordSymbol == WordSymbol.RightParenthesis) {
                    break;
                } else if (wordSymbol != WordSymbol.Comma) {
                    throw new SyntaxException(SyntaxError.ExpectCorrectSeparator);
                }
            }
        }
    }
}
