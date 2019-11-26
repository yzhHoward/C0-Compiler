package SyntaxAnalyzer;

public class SyntaxException extends Exception {
    private SyntaxAnalyzer.Errors error;

    public SyntaxException(SyntaxAnalyzer.Errors error) {
        this.error = error;
    }

    @Override
    public String getMessage() {
        return error.toString();
    }
}
