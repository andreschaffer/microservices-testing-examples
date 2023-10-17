package welcomememberemailservice.bootstrap;


import io.dropwizard.core.Configuration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class WelcomeMemberEmailServiceConfiguration extends Configuration {

  @NotNull
  @Valid
  private ConsumerConfiguration consumer;

  @NotNull
  @Valid
  private EmailSenderConfiguration emailSender;

  public ConsumerConfiguration getConsumer() {
    return consumer;
  }

  public void setConsumer(ConsumerConfiguration consumer) {
    this.consumer = consumer;
  }

  public EmailSenderConfiguration getEmailSender() {
    return emailSender;
  }

  public void setEmailSender(EmailSenderConfiguration emailSender) {
    this.emailSender = emailSender;
  }
}
