package welcomememberemailservice.it;

import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.subethamail.wiser.WiserMessage;

public class WelcomeEmailConsumerIT extends IntegrationTestBase {

  @Test
  public void sendWelcomeEmailToNewMember() throws Exception {
    String email = "clark.kent@example.com";
    String memberSignedUpEvent = memberSignedUpEvent(email);

    publishMembershipMessageAndWaitToBeConsumed(memberSignedUpEvent);

    assertAnEmailWasSent();
    WiserMessage emailSent = getLastSentEmail();
    assertThat(emailSent.getEnvelopeReceiver(), equalTo(email));
    assertThat(emailSent.getMimeMessage().getSubject(), equalTo("Welcome!"));
  }

  @Test
  public void ignoreMemberSignedUpEventWithBadProperties() throws Exception {
    String email = "notAnEmail";
    String memberSignedUpEvent = memberSignedUpEvent(email);
    publishMembershipMessageAndWaitToBeConsumed(memberSignedUpEvent);
    assertNoEmailWasSent();
  }

  @Test
  public void ignoreUnknownEvent() throws Exception {
    String unknownEvent = "{\"@type\":\"unknownEvent\"}";
    publishMembershipMessageAndWaitToBeConsumed(unknownEvent);
    assertNoEmailWasSent();
  }

  @Test
  public void ignoreMalformedEvent() throws Exception {
    publishMembershipMessageAndWaitToBeConsumed("foobar");
    assertNoEmailWasSent();
  }

  private String memberSignedUpEvent(String email) {
    return format("{\"@type\":\"memberSignedUpEvent\",\"email\":\"%s\"}", email);
  }
}
