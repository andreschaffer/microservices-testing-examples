package welcomememberemailservice.it.pacts;

import static welcomememberemailservice.it.pacts.PactConstants.SPECIAL_MEMBERSHIP_SERVICE;
import static welcomememberemailservice.it.pacts.PactConstants.WELCOME_MEMBER_EMAIL_SERVICE;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Interaction;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.MessagePact;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import welcomememberemailservice.it.IntegrationTestBase;

public class MemberSignedUpEventPactContracts extends IntegrationTestBase {

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

  @Test
  @PactTestFor(providerName = SPECIAL_MEMBERSHIP_SERVICE, pactMethod = "newMemberTonyStark",
      providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V4)
  public void sendWelcomeEmailToTonyStark(V4Interaction.AsynchronousMessage message) {
    String memberSignedUpEvent = message.contentsAsString();
    publishMembershipMessageAndWaitToBeConsumed(memberSignedUpEvent);
    assertAnEmailWasSent();
  }
}
