package specialmembershipservice.it;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import specialmembershipservice.bootstrap.SpecialMembershipServiceApplication;
import specialmembershipservice.bootstrap.SpecialMembershipServiceConfiguration;
import specialmembershipservice.it.client.ResourcesClient;

public abstract class IntegrationTestBase {

  private static final String INTEGRATION_YML = resourceFilePath("integration.yml");
  private static final int KAFKA_PORT = 9092;
  private static final String SPECIAL_MEMBERSHIP_TOPIC = "special-membership-topic";
  protected static final String CREDIT_SCORE_SERVICE_HOST = "localhost";
  protected static final int CREDIT_SCORE_SERVICE_PORT = 8088;

  private static final EphemeralKafkaBroker KAFKA_BROKER = EphemeralKafkaBroker.create(KAFKA_PORT);
  private static final KafkaJunitRule KAFKA_RULE = new KafkaJunitRule(KAFKA_BROKER);
  private static final DropwizardAppRule<SpecialMembershipServiceConfiguration> SERVICE_RULE =
      new DropwizardAppRule<>(SpecialMembershipServiceApplication.class, INTEGRATION_YML);

  @ClassRule
  public static final RuleChain RULES = RuleChain
      .outerRule(KAFKA_RULE)
      .around(SERVICE_RULE);

  protected static ResourcesClient resourcesClient;

  @BeforeClass
  public static void setUpClass() throws Exception {
      resourcesClient = new ResourcesClient(SERVICE_RULE.getEnvironment(), SERVICE_RULE.getLocalPort());
  }

  protected String readPublishedMessage()
      throws InterruptedException, ExecutionException, TimeoutException {

    return readOneMessage(SPECIAL_MEMBERSHIP_TOPIC);
  }

  protected String readOneMessage(String topic)
      throws InterruptedException, ExecutionException, TimeoutException {

      return KAFKA_RULE.helper()
          .consumeStrings(topic, 1)
          .get(1, SECONDS)
          .get(0);
  }
}
