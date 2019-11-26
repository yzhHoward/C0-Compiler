package WordAnalyzer;

public class WordException extends Exception {
    private WordAnalyzer.Errors error;

    public WordException(WordAnalyzer.WordAnalyzer.Errors error) {
        this.error = error;
    }

    @Override
    public String getMessage() {
        return error.toString();
    }
}
