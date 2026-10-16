package trd.home.tcg.exception;

public class DeckCsvExportException extends RuntimeException {
    public DeckCsvExportException(String message) {
        super(message);
    }

    public DeckCsvExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
