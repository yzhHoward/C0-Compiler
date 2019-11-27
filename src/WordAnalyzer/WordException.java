package WordAnalyzer;

public class WordException extends Exception {
    private WordError error;

    public WordException(WordError error) {
        this.error = error;
    }

    @Override
    public String getMessage() {
        return error.toString();
    }
}
