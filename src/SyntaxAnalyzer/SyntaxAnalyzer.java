package SyntaxAnalyzer;

import InstructionWriter.InstructionWriter;
import InstructionWriter.Instructions;
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
    private InstructionWriter instructionWriter;
    private WordSymbol wordSymbol;
    private ArrayList<NextWord> buf = new ArrayList<>();
    private int cursor = -1;
    private int level = 0;
    private int startOffset = 0;
    private int functionOffset = 0;

    public SyntaxAnalyzer(WordAnalyzer wordAnalyzer) {
        this.wordAnalyzer = wordAnalyzer;
        instructionWriter = new InstructionWriter();
    }

    public SyntaxAnalyzer(WordAnalyzer wordAnalyzer, String outputPath) {
        this.wordAnalyzer = wordAnalyzer;
        instructionWriter = new InstructionWriter(outputPath);
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
            buf.add(new NextWord(wordAnalyzer.token, wordAnalyzer.symbol, wordAnalyzer.lineOffset, wordAnalyzer.wordOffset));
        } catch (WordException e) {
            System.out.println(e.getMessage() + " at " + wordAnalyzer.lineOffset + ":" + (wordAnalyzer.wordOffset - token.length() + 1) + " word: " + wordAnalyzer.token);
        }
        nextLevel();
        try {
            program();
            instructionWriter.output();
        } catch (SyntaxException e) {
            System.out.println(e.getMessage() + " at " + lineOffset + ":" + wordOffset + " word: " + token);
        }
    }

    // 程序
    private void program() throws SyntaxException {
        while (variableDeclaration(startOffset)) {
            ++startOffset;
        }
        while (true) {
            read();
            if (wordSymbol == WordSymbol.EOF) {
                unread();
                break;
            }
            unread();
            if (!functionDefinition()) {
                ++functionOffset;
                break;
            }
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
                instructionWriter.write(level, Instructions.ipush, 0);
                expression();
                instructionWriter.write(level, Instructions.loada, 0, offset);
                instructionWriter.write(level, Instructions.istore);
            } else if (symbolType == SymbolType.Constant) {
                throw new SyntaxException(SyntaxError.UninitializedConstant);
            } else if (wordSymbol == WordSymbol.LeftParenthesis) {
                unread();
                unread();
                unread();
                return false;
            } else {
                unread();
                instructionWriter.write(level, Instructions.ipush, 0);
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
    private boolean functionDefinition() throws SyntaxException {
        DataType dataType = null;
        FunctionSymbol functionSymbol;
        String functionName;
        int constantIndex;
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
        constantIndex = instructionWriter.writeConstants(token);
        instructionWriter.newFunction();
        functionSymbol = insertFunctionSymbol(token, SymbolType.Function, dataType, functionOffset, lineOffset, wordOffset);
        ++level;
        nextLevel();
        functionName = token;
        parameter(functionName);
        instructionWriter.writeFunctions(functionOffset, constantIndex, functionSymbol.getArgsSize());
        read();
        if (wordSymbol == WordSymbol.LeftBrace) {
            int variableOffset = 0;
            while (variableDeclaration(variableOffset)) {
                ++variableOffset;
            }
            statementSequence(functionSymbol);
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
    private void statementSequence(FunctionSymbol functionSymbol) throws SyntaxException {
        while (true) {
            if (!statement(functionSymbol)) {
                break;
            }
        }
    }

    // 语句
    private boolean statement(FunctionSymbol functionSymbol) throws SyntaxException {
        read();
        if (wordSymbol == WordSymbol.RightBrace) {
            unread();
            return false;
        } else if (wordSymbol == WordSymbol.LeftBrace) {
            ++level;
            nextLevel();
            statementSequence(functionSymbol);
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
                FunctionSymbol function = findFunctionSymbol(token);
                unread();
                callFunction();
                // 如果调用者不需要返回值，执行 pop 系列指令清除调用者栈帧得到的返回值
                if (function == null) {
                    throw new SyntaxException(SyntaxError.SymbolNotFound);
                }
                if (function.dataType != DataType.Void) {
                    instructionWriter.write(level, Instructions.pop);
                }
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
                    ifStatement(functionSymbol);
                    return true;
                case While:
                    unread();
                    whileStatement(functionSymbol);
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
                    returnStatement(functionSymbol);
                    return true;
                case Semicolon:
                    return true;
                default:
                    throw new SyntaxException(SyntaxError.InvalidSentenceSequence);
            }
        }
    }

    // 条件
    private Instructions condition() throws SyntaxException {
        Instructions instructions;
        expression();
        read();
        if (wordSymbol == WordSymbol.RightParenthesis) {
            return Instructions.je;
        } else {
            switch (wordSymbol) {
                case Less:
                    instructions = Instructions.jge;
                    break;
                case LessOrEqual:
                    instructions = Instructions.jg;
                    break;
                case Greater:
                    instructions = Instructions.jle;
                    break;
                case GreaterOrEqual:
                    instructions = Instructions.jl;
                    break;
                case NotEqual:
                    instructions = Instructions.je;
                    break;
                case Equal:
                    instructions = Instructions.jne;
                    break;
                default:
                    throw new SyntaxException(SyntaxError.InvalidCondition);
            }
            expression();
            instructionWriter.write(level, Instructions.icmp);
            return instructions;
        }
    }

    // 条件语句
    private void ifStatement(FunctionSymbol functionSymbol) throws SyntaxException {
        int index;
        Instructions conditionInstruction;
        read();
        if (wordSymbol != WordSymbol.If) {
            throw new SyntaxException(SyntaxError.InvalidIfStatement);
        }
        read();
        if (wordSymbol != WordSymbol.LeftParenthesis) {
            throw new SyntaxException(SyntaxError.MissingCondition);
        }
        conditionInstruction = condition();
        index = instructionWriter.getSize();
        read();
        if (wordSymbol != WordSymbol.RightParenthesis) {
            throw new SyntaxException(SyntaxError.ExpectRightParenthesis);
        }
        statement(functionSymbol);
        read();
        instructionWriter.insert(index, conditionInstruction, instructionWriter.getSize());
        if (wordSymbol == WordSymbol.Else) {
            statement(functionSymbol);
        } else {
            unread();
        }
    }

    // 循环语句
    private void whileStatement(FunctionSymbol functionSymbol) throws SyntaxException {
        int loopIndex;
        int conditionIndex;
        Instructions conditionInstruction;
        read();
        if (wordSymbol != WordSymbol.While) {
            throw new SyntaxException(SyntaxError.InvalidWhileStatement);
        }
        read();
        if (wordSymbol != WordSymbol.LeftParenthesis) {
            throw new SyntaxException(SyntaxError.MissingCondition);
        }
        loopIndex = instructionWriter.getSize();
        conditionInstruction = condition();
        conditionIndex = instructionWriter.getSize();
        read();
        if (wordSymbol != WordSymbol.RightParenthesis) {
            throw new SyntaxException(SyntaxError.ExpectRightParenthesis);
        }
        statement(functionSymbol);
        instructionWriter.insert(conditionIndex, conditionInstruction, instructionWriter.getSize() + 1);
        instructionWriter.write(level, Instructions.jmp, loopIndex);
    }

    // 返回语句
    private void returnStatement(FunctionSymbol functionSymbol) throws SyntaxException {
        read();
        if (wordSymbol != WordSymbol.Return) {
            throw new SyntaxException(SyntaxError.InvalidReturn);
        }
        read();
        if (wordSymbol != WordSymbol.Semicolon) {
            if (functionSymbol.dataType == DataType.Void) {
                throw new SyntaxException(SyntaxError.ReturnValueForVoidFunction);
            }
            expression();
            read();
            if (wordSymbol != WordSymbol.Semicolon) {
                throw new SyntaxException(SyntaxError.MissingSemicolon);
            }
            if (functionSymbol.dataType == DataType.Char) {
                instructionWriter.write(level, Instructions.i2c);
            }
            instructionWriter.write(level, Instructions.iret);
        } else if (functionSymbol.dataType == DataType.Void) {
            instructionWriter.write(level, Instructions.ret);
        } else {
            throw new SyntaxException(SyntaxError.NoReturnValueForIntFunction);
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
        if (variableSymbol.dataType == DataType.Char) {
            instructionWriter.write(level, Instructions.cscan);
        } else {
            instructionWriter.write(level, Instructions.iscan);
        }
        if (level < variableSymbol.level) {
            throw new SyntaxException(SyntaxError.UnknownError);
        }
        instructionWriter.write(level, Instructions.loada, level - variableSymbol.level, variableSymbol.offset);
        instructionWriter.write(level, Instructions.istore);
    }

    // 写语句
    private void printStatement() throws SyntaxException {
        int stringLiteralIndex;
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
            stringLiteralIndex = instructionWriter.writeConstants(token);
            instructionWriter.write(level, Instructions.loadc, stringLiteralIndex);
            instructionWriter.write(level, Instructions.sprint);
        } else {
            boolean convertToChar = expression();
            if (convertToChar) {
                instructionWriter.write(level, Instructions.cprint);
            } else {
                instructionWriter.write(level, Instructions.iprint);
            }
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
        if (level < variableSymbol.level) {
            throw new SyntaxException(SyntaxError.UnknownError);
        }
        instructionWriter.write(level, Instructions.loada, level - variableSymbol.level, variableSymbol.offset);
        instructionWriter.write(level, Instructions.istore);
    }

    // 表达式
    private boolean expression() throws SyntaxException {
        boolean convertToChar = multiplicativeExpression();
        while (true) {
            read();
            if (wordSymbol == WordSymbol.Plus) {
                convertToChar = false;
                instructionWriter.write(level, Instructions.iadd);
                multiplicativeExpression();
            } else if (wordSymbol == WordSymbol.Minus) {
                convertToChar = false;
                instructionWriter.write(level, Instructions.isub);
                multiplicativeExpression();
            } else {
                unread();
                break;
            }
        }
        return convertToChar;
    }

    private boolean multiplicativeExpression() throws SyntaxException {
        boolean convertToChar = castExpression();
        while (true) {
            read();
            if (wordSymbol == WordSymbol.Multi) {
                convertToChar = false;
                instructionWriter.write(level, Instructions.imul);
                castExpression();
            } else if (wordSymbol == WordSymbol.Div) {
                convertToChar = false;
                instructionWriter.write(level, Instructions.idiv);
                castExpression();
            } else {
                unread();
                break;
            }
        }
        return convertToChar;
    }

    private boolean castExpression() throws SyntaxException {
        boolean convertToChar = false;
        boolean convertToInt = false;
        while (true) {
            read();
            if (wordSymbol == WordSymbol.LeftParenthesis) {
                read();
                if (wordSymbol == WordSymbol.Int) {
                    convertToChar = false;
                    convertToInt = true;
                } else if (wordSymbol == WordSymbol.Char) {
                    convertToChar = true;
                    convertToInt = false;
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
        if (wordSymbol == WordSymbol.Plus) {
            primaryExpression();
        } else if (wordSymbol == WordSymbol.Minus) {
            instructionWriter.write(level, Instructions.ineg);
            primaryExpression();
        } else {
            unread();
            if (primaryExpression() && !convertToInt) {
                convertToChar = true;
            }
        }
        if (convertToChar) {
            instructionWriter.write(level, Instructions.i2c);
        }
        return convertToChar;
    }

    private boolean primaryExpression() throws SyntaxException {
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
            instructionWriter.write(level, Instructions.ipush, token);
        } else if (wordSymbol == WordSymbol.CharLiteral) {
            instructionWriter.write(level, Instructions.bipush, token.charAt(0));
            return true;
        } else if (wordSymbol == WordSymbol.Identifier) {
            variableSymbol = findVariableSymbol(token);
            functionSymbol = findFunctionSymbol(token);
            if (variableSymbol != null) {
                if (!variableSymbol.isInitialized()) {
                    throw new SyntaxException(SyntaxError.UninitializedVariable);
                }
                if (level < variableSymbol.level) {
                    throw new SyntaxException(SyntaxError.UnknownError);
                }
                instructionWriter.write(level, Instructions.loada, level - variableSymbol.level, variableSymbol.offset);
                instructionWriter.write(level, Instructions.iload);
            } else if (functionSymbol != null) {
                unread();
                callFunction();
            } else {
                throw new SyntaxException(SyntaxError.SymbolNotFound);
            }
        }
        return false;
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
        instructionWriter.write(level, Instructions.call, functionSymbol.offset);
    }
}
