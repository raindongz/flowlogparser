package custom_exceptions;

public class FileIsEmptyException extends RuntimeException{
    private String message;

    public FileIsEmptyException(String message) {
        super(message);
    }
}