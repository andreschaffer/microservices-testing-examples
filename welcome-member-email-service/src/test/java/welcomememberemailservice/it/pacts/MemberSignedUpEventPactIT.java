package welcomememberemailservice.it.pacts;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.MessagePactProviderRule;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.v3.messaging.MessagePact;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.*;
import org.junit.rules.RuleChain;
import org.subethamail.wiser.Wiser;
import welcomememberemailservice.bootstrap.WelcomeMemberEmailServiceApplication;
import welcomememberemailservice.bootstrap.WelcomeMemberEmailServiceConfiguration;
import welcomememberemailservice.it.KafkaOffsets;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static welcomememberemailservice.it.IntegrationEnvironment.*;
import static welcomememberemailservice.it.pacts.PactConstants.SPECIAL_MEMBERSHIP_SERVICE;
import static welcomememberemailservice.it.pacts.PactConstants.WELCOME_MEMBER_EMAIL_SERVICE;

public class MemberSignedUpEventPactIT {

    private static final EphemeralKafkaBroker KAFKA_BROKER = EphemeralKafkaBroker.create(KAFKA_PORT);
    private static final KafkaJunitRule KAFKA_RULE = new KafkaJunitRule(KAFKA_BROKER);

    private static final DropwizardAppRule<WelcomeMemberEmailServiceConfiguration> SERVICE_RULE =
            new DropwizardAppRule<>(WelcomeMemberEmailServiceApplication.class, INTEGRATION_YML);

    @ClassRule
    public static final RuleChain RULES = RuleChain.outerRule(KAFKA_RULE).around(SERVICE_RULE);

    @Rule
    public final MessagePactProviderRule specialMembershipServiceRule =
            new MessagePactProviderRule(SPECIAL_MEMBERSHIP_SERVICE, this);

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

    @Pact(consumer = WELCOME_MEMBER_EMAIL_SERVICE, provider = SPECIAL_MEMBERSHIP_SERVICE)
    public MessagePact newMemberTonyStark(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody()
                .stringValue("@type", "memberSignedUpEvent")
                .stringMatcher("email", ".+@.+\\..+", "tony.stark@example.com");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("contentType", "application/json");

        return builder.given("Tony Stark became a new member")
                .expectsToReceive("An event notifying Tony Stark's new membership")
                .withMetadata(metadata)
                .withContent(body)
                .toPact();
    }

    @PactVerification(value = SPECIAL_MEMBERSHIP_SERVICE, fragment = "newMemberTonyStark")
    @Test
    public void sendWelcomeEmailToTonyStark() throws Exception {
        String memberSignedUpEvent = new String(specialMembershipServiceRule.getMessage(), UTF_8);
        publishMessageAndWaitToBeConsumed(SPECIAL_MEMBERSHIP_TOPIC, memberSignedUpEvent, WELCOME_EMAIL_GROUP_ID);
        assertThat(smtpServer.getMessages(), hasSize(1));
    }

    private void publishMessageAndWaitToBeConsumed(String topic, String message, String groupId) {
        KafkaOffsets kafkaOffsets = new KafkaOffsets(KAFKA_HOST, KAFKA_RULE.helper().kafkaPort());
        long previousOffset = Math.max(kafkaOffsets.readOffset(topic, groupId), 0);
        KAFKA_RULE.helper().produceStrings(topic, message);
        await().atMost(40, SECONDS).until(() -> kafkaOffsets.readOffset(topic, groupId), equalTo(previousOffset + 1));
    }
}
