package welcomememberemailservice.it.smtp;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.subethamail.wiser.Wiser;

public class SmtpServerExtension implements BeforeAllCallback, AfterAllCallback {

  private final int port;
  private Wiser smtpServer;

  public SmtpServerExtension(int port) {
    this.port = port;
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) {
    smtpServer = new Wiser(port);
    smtpServer.start();
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    if (smtpServer != null) {
      smtpServer.stop();
    }
  }

  public Wiser getSmtpServer() {
    return smtpServer;
  }
}
