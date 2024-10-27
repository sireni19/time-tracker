package by.prokopovich.time_tracker.exception;

public class IllegalRecordIdException extends RuntimeException {
    private static final String ILLEGAL_RECORD_ID = "Такой записи нет или она вам не принадлежит";

    public IllegalRecordIdException() {
        super(ILLEGAL_RECORD_ID);
    }

    public IllegalRecordIdException(String message) {
        super(message);
    }
}
