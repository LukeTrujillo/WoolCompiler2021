package wool.utility;

public class WoolException extends RuntimeException
{
    /**
     * There must be at least a message in a COOL exception
     * @param message the text of the  message
     */
    public WoolException(String message)
    {
        super(message);
    }

    /**
     * This is a constructor for an exception that is caused by another exception.
     * @param message the text of the message
     * @param cause the causing exception
     */
    public WoolException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
