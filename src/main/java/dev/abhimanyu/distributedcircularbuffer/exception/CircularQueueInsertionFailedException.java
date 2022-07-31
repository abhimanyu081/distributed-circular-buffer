package dev.abhimanyu.distributedcircularbuffer.exception;

/**
 * @author abhimanyu.abhimanyu1
 */
public class CircularQueueInsertionFailedException extends RuntimeException {

    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public CircularQueueInsertionFailedException() {
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public CircularQueueInsertionFailedException(String message) {
        super(message);
    }
}
