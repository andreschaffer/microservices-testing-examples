package welcomememberemailservice.bootstrap;

import jakarta.validation.constraints.NotBlank;
import java.util.Properties;
import javax.mail.Session;

public class EmailSenderConfiguration {

  @NotBlank
  private String host;

  @NotBlank
  private String port;

  public Session getSession() {
    Properties properties = System.getProperties();
    properties.setProperty("mail.smtp.host", host);
    properties.setProperty("mail.smtp.port", port);
    return Session.getDefaultInstance(properties);
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }
}
