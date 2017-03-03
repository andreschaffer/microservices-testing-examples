package welcomememberemailservice.it;

import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.subethamail.wiser.Wiser;
import welcomememberemailservice.bootstrap.WelcomeMemberEmailServiceApplication;
import welcomememberemailservice.bootstrap.WelcomeMemberEmailServiceConfiguration;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static welcomememberemailservice.it.IntegrationEnvironment.*;

public class WelcomeEmailConsumerIT {

    private static final EphemeralKafkaBroker KAFKA_BROKER = EphemeralKafkaBroker.create(KAFKA_PORT);
    private static final KafkaJunitRule KAFKA_RULE = new KafkaJunitRule(KAFKA_BROKER);

    private static final DropwizardAppRule<WelcomeMemberEmailServiceConfiguration> SERVICE_RULE =
            new DropwizardAppRule<>(WelcomeMemberEmailServiceApplication.class, INTEGRATION_YML);

    @ClassRule
    public static final RuleChain RULES = RuleChain.outerRule(KAFKA_RULE).around(SERVICE_RULE);

    private Wiser smtpServer;

    @Before
    public void setUp() throws Exception {
        smtpServer = new Wiser(SMTP_SERVER_PORT);
        smtpServer.start();
    }

    @After
    public void tearDown() throws Exception {
        smtpServer.stop();
    }

    @Test
    public void sendWelcomeEmailToNewMember() throws Exception {
        String type = "memberSignedUpEvent", email = "clark.kent@example.com";
        String memberSignedUpEvent = format("{\"@type\":\"%s\",\"email\":\"%s\"}", type, email);
        publishMessageAndWaitToBeConsumed(SPECIAL_MEMBERSHIP_TOPIC, memberSignedUpEvent, WELCOME_EMAIL_GROUP_ID);
        assertThat(smtpServer.getMessages(), hasSize(1));
        assertThat(smtpServer.getMessages().get(0).getEnvelopeReceiver(), equalTo(email));
    }

    @Test
    public void ignoreUnknownEvent() throws Exception {
        String type = "unknownEvent", email = "the.riddler@example.com";
        String memberSignedUpEvent = format("{\"@type\":\"%s\",\"email\":\"%s\"}", type, email);
        publishMessageAndWaitToBeConsumed(SPECIAL_MEMBERSHIP_TOPIC, memberSignedUpEvent, WELCOME_EMAIL_GROUP_ID);
        assertThat(smtpServer.getMessages(), hasSize(0));
    }

    @Test
    public void ignoreMemberSignedUpEventWithBadProperties() throws Exception {
        String type = "memberSignedUpEvent", email = "notAnEmail";
        String memberSignedUpEvent = format("{\"@type\":\"%s\",\"email\":\"%s\"}", type, email);
        publishMessageAndWaitToBeConsumed(SPECIAL_MEMBERSHIP_TOPIC, memberSignedUpEvent, WELCOME_EMAIL_GROUP_ID);
        assertThat(smtpServer.getMessages(), hasSize(0));
    }

    @Test
    public void ignoreMalformedEvent() throws Exception {
        publishMessageAndWaitToBeConsumed(SPECIAL_MEMBERSHIP_TOPIC, "foobar", WELCOME_EMAIL_GROUP_ID);
        assertThat(smtpServer.getMessages(), hasSize(0));
    }

    @Test
    public void forwardCompatibility() throws Exception {
        String type = "memberSignedUpEvent", email = "clark.kent@example.com";
        String memberSignedUpEvent = format("{\"@type\":\"%s\",\"email\":\"%s\",\"foo\":\"bar\"}", type, email);
        publishMessageAndWaitToBeConsumed(SPECIAL_MEMBERSHIP_TOPIC, memberSignedUpEvent, WELCOME_EMAIL_GROUP_ID);
        assertThat(smtpServer.getMessages(), hasSize(1));
        assertThat(smtpServer.getMessages().get(0).getEnvelopeReceiver(), equalTo(email));
    }

    private void publishMessageAndWaitToBeConsumed(String topic, String message, String groupId) {
        KafkaOffsets kafkaOffsets = new KafkaOffsets(KAFKA_HOST, KAFKA_RULE.helper().kafkaPort());
        long previousOffset = Math.max(kafkaOffsets.readOffset(topic, groupId), 0);
        KAFKA_RULE.helper().produceStrings(topic, message);
        await().atMost(5, SECONDS).until(() -> kafkaOffsets.readOffset(topic, groupId), equalTo(previousOffset + 1));
    }
}
