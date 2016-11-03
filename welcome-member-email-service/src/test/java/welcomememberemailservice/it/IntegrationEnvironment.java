package welcomememberemailservice.it;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class IntegrationEnvironment {

    public static final String INTEGRATION_YML = resourceFilePath("integration.yml");
    public static final int SMTP_SERVER_PORT = 2525;
    public static final String KAFKA_HOST = "localhost";
    public static final int KAFKA_PORT = 9092;
    public static final String SPECIAL_MEMBERSHIP_TOPIC = "special-membership-topic";
    public static final String WELCOME_EMAIL_GROUP_ID = "welcome-member-email-consumer";
}
