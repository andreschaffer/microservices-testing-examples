package welcomememberemailservice.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import welcomememberemailservice.port.incoming.adapter.kafka.WelcomeEmailConsumer;
import welcomememberemailservice.port.outgoing.adapter.email.SmtpEmailSender;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class WelcomeMemberEmailServiceApplication extends Application<WelcomeMemberEmailServiceConfiguration> {

    @Override
    public void run(WelcomeMemberEmailServiceConfiguration configuration, Environment environment) throws Exception {
        configureObjectMapper(environment.getObjectMapper());
        registerConsumers(configuration, environment);
    }

    private void configureObjectMapper(ObjectMapper objectMapper) {
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private void registerConsumers(WelcomeMemberEmailServiceConfiguration configuration, Environment environment) {
        SmtpEmailSender emailSender = new SmtpEmailSender(configuration.getEmailSender().getSession());
        WelcomeEmailConsumer welcomeEmailConsumer = new WelcomeEmailConsumer(
                configuration.getConsumer().getTopic(),
                configuration.getConsumer().getConfigs(),
                environment.getObjectMapper(),
                environment.getValidator(),
                emailSender);
        environment.lifecycle().manage(welcomeEmailConsumer);
    }
}
