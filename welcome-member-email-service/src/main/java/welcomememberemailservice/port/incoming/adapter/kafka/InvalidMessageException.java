package welcomememberemailservice.port.incoming.adapter.kafka;

public class InvalidMessageException extends RuntimeException {

    public InvalidMessageException(Throwable e) {
        super(e);
    }
}
