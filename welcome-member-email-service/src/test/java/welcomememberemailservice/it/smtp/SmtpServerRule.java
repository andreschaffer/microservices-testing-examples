package welcomememberemailservice.it.smtp;

import org.junit.rules.ExternalResource;
import org.subethamail.wiser.Wiser;

public class SmtpServerRule extends ExternalResource {

  private final int port;
  private Wiser smtpServer;

  public SmtpServerRule(final int port) {
    this.port = port;
  }

  @Override
  protected void before() throws Throwable {
    smtpServer = new Wiser(port);
    smtpServer.start();
  }

  @Override
  protected void after() {
    if (smtpServer != null) {
      smtpServer.stop();
    }
  }

  public Wiser getSmtpServer() {
    return smtpServer;
  }
}
