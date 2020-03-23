package specialmembershipservice.port.outgoing.adapter.creditscore;

public class TemporarilyUnavailableException extends RuntimeException {

  public TemporarilyUnavailableException(Throwable e) {
    super(e);
  }
}
