package welcomememberemailservice.it;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.subethamail.wiser.Wiser;
import welcomememberemailservice.bootstrap.WelcomeMemberEmailServiceApplication;
import welcomememberemailservice.bootstrap.WelcomeMemberEmailServiceConfiguration;
import welcomememberemailservice.it.kafka.KafkaOffsets;

public abstract class IntegrationTestBase {

  protected static final int SMTP_SERVER_PORT = 2525;
  protected static final String KAFKA_HOST = "localhost";
  protected static final int KAFKA_PORT = 9092;
  protected static final String SPECIAL_MEMBERSHIP_TOPIC = "special-membership-topic";
  protected static final String WELCOME_EMAIL_GROUP_ID = "welcome-member-email-consumer";

  protected static final EphemeralKafkaBroker KAFKA_BROKER = EphemeralKafkaBroker.create(KAFKA_PORT);
  protected static final KafkaJunitRule KAFKA_RULE = new KafkaJunitRule(KAFKA_BROKER);

  protected static final DropwizardAppRule<WelcomeMemberEmailServiceConfiguration> SERVICE_RULE =
      new DropwizardAppRule<>(WelcomeMemberEmailServiceApplication.class, resourceFilePath("integration.yml"));

  @ClassRule
  public static final RuleChain RULES = RuleChain.outerRule(KAFKA_RULE).around(SERVICE_RULE);

  protected Wiser smtpServer;

  @Before
  public void setUp() throws Exception {
    smtpServer = new Wiser(SMTP_SERVER_PORT);
    smtpServer.start();
  }

  @After
  public void tearDown() throws Exception {
    smtpServer.stop();
  }

  protected void publishMessageAndWaitToBeConsumed(String topic, String message, String groupId) {
    KafkaOffsets kafkaOffsets = new KafkaOffsets(KAFKA_HOST, KAFKA_RULE.helper().kafkaPort());
    long previousOffset = Math.max(kafkaOffsets.readOffset(topic, groupId), 0);
    KAFKA_RULE.helper().produceStrings(topic, message);
    await().atMost(30, SECONDS).until(() -> kafkaOffsets.readOffset(topic, groupId), equalTo(previousOffset + 1));
  }
}
