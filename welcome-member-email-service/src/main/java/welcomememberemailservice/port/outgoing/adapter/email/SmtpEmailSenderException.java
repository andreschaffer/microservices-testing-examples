package welcomememberemailservice.port.outgoing.adapter.email;

public class SmtpEmailSenderException extends RuntimeException {

  public SmtpEmailSenderException(Throwable e) {
    super(e);
  }
}
