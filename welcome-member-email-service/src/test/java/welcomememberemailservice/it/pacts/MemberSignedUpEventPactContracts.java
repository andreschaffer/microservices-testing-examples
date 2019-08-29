package welcomememberemailservice.it.pacts;

import static java.nio.charset.StandardCharsets.UTF_8;
import static welcomememberemailservice.it.pacts.PactConstants.SPECIAL_MEMBERSHIP_SERVICE;
import static welcomememberemailservice.it.pacts.PactConstants.WELCOME_MEMBER_EMAIL_SERVICE;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit.MessagePactProviderRule;
import java.util.HashMap;
import java.util.Map;

import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.MessagePact;
import org.junit.Rule;
import org.junit.Test;
import welcomememberemailservice.it.IntegrationTestBase;

public class MemberSignedUpEventPactContracts extends IntegrationTestBase {

    @Rule
    public final MessagePactProviderRule specialMembershipServiceRule =
            new MessagePactProviderRule(SPECIAL_MEMBERSHIP_SERVICE, this);

    @Pact(consumer = WELCOME_MEMBER_EMAIL_SERVICE, provider = SPECIAL_MEMBERSHIP_SERVICE)
    public MessagePact newMemberTonyStark(MessagePactBuilder builder) {
        PactDslJsonBody body = new PactDslJsonBody()
                .stringValue("@type", "memberSignedUpEvent")
                .stringMatcher("email", ".+@.+\\..+", "tony.stark@example.com");

        Map<String, String> metadata = new HashMap<>();

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
        publishMembershipMessageAndWaitToBeConsumed(memberSignedUpEvent);
        assertAnEmailWasSent();
    }
}
