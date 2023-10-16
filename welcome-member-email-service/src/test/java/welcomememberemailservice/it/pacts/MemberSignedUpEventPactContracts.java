package welcomememberemailservice.it.pacts;

import static welcomememberemailservice.it.pacts.PactConstants.SPECIAL_MEMBERSHIP_SERVICE;
import static welcomememberemailservice.it.pacts.PactConstants.WELCOME_MEMBER_EMAIL_SERVICE;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import welcomememberemailservice.it.IntegrationTestBase;

@ExtendWith(PactConsumerTestExt.class)
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
  @PactTestFor(pactMethod = "newMemberTonyStark",
      providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V3)
  public void sendWelcomeEmailToTonyStark(List<Message> messages) {
    String memberSignedUpEvent = messages.get(0).contentsAsString();
    publishMembershipMessageAndWaitToBeConsumed(memberSignedUpEvent);
    assertAnEmailWasSent();
  }
}
