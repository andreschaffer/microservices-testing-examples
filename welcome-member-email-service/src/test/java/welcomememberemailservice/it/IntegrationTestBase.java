package welcomememberemailservice.it;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import io.dropwizard.testing.junit5.DropwizardAppExtension;
import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.WiserMessage;
import welcomememberemailservice.bootstrap.WelcomeMemberEmailServiceApplication;
import welcomememberemailservice.bootstrap.WelcomeMemberEmailServiceConfiguration;
import welcomememberemailservice.it.kafka.KafkaConsumerOffsets;
import welcomememberemailservice.it.kafka.KafkaContainerExtension;
import welcomememberemailservice.it.smtp.SmtpServerExtension;

public abstract class IntegrationTestBase {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String INTEGRATION_YML = resourceFilePath("integration.yml");
  private static final String KAFKA_HOST = "localhost";
  private static final int KAFKA_PORT = 19092;
  private static final String SPECIAL_MEMBERSHIP_TOPIC = "special-membership-topic";
  private static final String WELCOME_EMAIL_GROUP_ID = "welcome-member-email-consumer";
  private static final int SMTP_SERVER_PORT = 2525;

  @RegisterExtension
  @Order(0)
  private static final SmtpServerExtension SMTP_SERVER_RULE = new SmtpServerExtension(
      SMTP_SERVER_PORT);

  @RegisterExtension
  @Order(1)
  private static final KafkaContainerExtension KAFKA_RULE = new KafkaContainerExtension(
      KAFKA_HOST, KAFKA_PORT);

  @RegisterExtension
  @Order(Integer.MAX_VALUE)
  private static final DropwizardAppExtension<WelcomeMemberEmailServiceConfiguration> SERVICE_RULE =
      new DropwizardAppExtension<>(WelcomeMemberEmailServiceApplication.class, INTEGRATION_YML);

  private List<WiserMessage> emailsBeforeTest;

  @BeforeEach
  public void setUp() {
    this.emailsBeforeTest = getEmails();
  }

  private List<WiserMessage> getEmails() {
    return new ArrayList<>(SMTP_SERVER_RULE.getSmtpServer().getMessages());
  }


  protected void assertNoEmailWasSent() {
    assertThat(getEmails(), hasSize(emailsBeforeTest.size()));
  }

  protected void assertAnEmailWasSent() {
    assertThat(getEmails(), hasSize(emailsBeforeTest.size() + 1));
  }

  protected WiserMessage getLastSentEmail() {
    return getEmails().get(getEmails().size() - 1);
  }

  protected void publishMembershipMessageAndWaitToBeConsumed(String message) {
    publishMessageAndWaitToBeConsumed(SPECIAL_MEMBERSHIP_TOPIC, message, WELCOME_EMAIL_GROUP_ID);
  }

  protected void publishMessageAndWaitToBeConsumed(String topic, String message, String groupId) {
    KafkaConsumerOffsets kafkaConsumerOffsets =
        new KafkaConsumerOffsets(KAFKA_HOST, KAFKA_RULE.getPort(), groupId);

    final long previousOffset = Math.max(kafkaConsumerOffsets.readOffset(topic), 0);

    LOG.info("Publishing message {} to topic {}", message, topic);
    KAFKA_RULE.produceStrings(topic, message);

    LOG.info("Waiting for message to be consumed from topic {}", topic);
    await().atMost(Duration.ofSeconds(5))
        .until(() -> kafkaConsumerOffsets.readOffset(topic), equalTo(previousOffset + 1));
  }
}
