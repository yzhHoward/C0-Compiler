package SyntaxAnalyzer;

public class SyntaxException extends Exception {
    private SyntaxError error;

    public SyntaxException(SyntaxError error) {
        this.error = error;
    }

    @Override
    public String getMessage() {
        return error.toString();
    }
}
