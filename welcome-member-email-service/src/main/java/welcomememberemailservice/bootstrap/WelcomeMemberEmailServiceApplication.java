package welcomememberemailservice.bootstrap;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import welcomememberemailservice.port.incoming.adapter.kafka.WelcomeEmailConsumer;
import welcomememberemailservice.port.outgoing.adapter.email.SmtpEmailSender;

public class WelcomeMemberEmailServiceApplication extends
    Application<WelcomeMemberEmailServiceConfiguration> {

  @Override
  public void run(WelcomeMemberEmailServiceConfiguration configuration, Environment environment)
      throws Exception {
    ObjectMapperConfig.applyTo(environment.getObjectMapper());
    registerConsumers(configuration, environment);
  }

  private void registerConsumers(WelcomeMemberEmailServiceConfiguration configuration,
      Environment environment) {
    SmtpEmailSender emailSender = new SmtpEmailSender(configuration.getEmailSender().getSession());
    WelcomeEmailConsumer welcomeEmailConsumer = new WelcomeEmailConsumer(
        configuration.getConsumer().getTopic(),
        configuration.getConsumer().getConfigs(),
        environment.getObjectMapper().copy(),
        environment.getValidator(),
        emailSender);
    environment.lifecycle().manage(welcomeEmailConsumer);
  }
}
