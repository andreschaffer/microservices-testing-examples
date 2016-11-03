package specialmembershipservice.it;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class IntegrationEnvironment {

    public static final String INTEGRATION_YML = resourceFilePath("integration.yml");
    public static final String CREDIT_SCORE_SERVICE_HOST = "localhost";
    public static final int CREDIT_SCORE_SERVICE_PORT = 8088;
    public static final int KAFKA_PORT = 9092;
    public static final String SPECIAL_MEMBERSHIP_TOPIC = "special-membership-topic";
}
