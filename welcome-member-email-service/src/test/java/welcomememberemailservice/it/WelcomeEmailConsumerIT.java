package welcomememberemailservice.it;

import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class WelcomeEmailConsumerIT extends IntegrationTestBase {

    @Test
    public void sendWelcomeEmailToNewMember() throws Exception {
        String type = "memberSignedUpEvent", email = "clark.kent@example.com";
        String memberSignedUpEvent = format("{\"@type\":\"%s\",\"email\":\"%s\"}", type, email);
        publishMembershipMessageAndWaitToBeConsumed(memberSignedUpEvent);
        assertAnEmailWasSent();
        assertThat(getLastSentEmail().getEnvelopeReceiver(), equalTo(email));
    }

    @Test
    public void ignoreUnknownEvent() throws Exception {
        String type = "unknownEvent", email = "the.riddler@example.com";
        String memberSignedUpEvent = format("{\"@type\":\"%s\",\"email\":\"%s\"}", type, email);
        publishMembershipMessageAndWaitToBeConsumed(memberSignedUpEvent);
        assertNoEmailWasSent();
    }

    @Test
    public void ignoreMemberSignedUpEventWithBadProperties() throws Exception {
        String type = "memberSignedUpEvent", email = "notAnEmail";
        String memberSignedUpEvent = format("{\"@type\":\"%s\",\"email\":\"%s\"}", type, email);
        publishMembershipMessageAndWaitToBeConsumed(memberSignedUpEvent);
        assertNoEmailWasSent();
    }

    @Test
    public void ignoreMalformedEvent() throws Exception {
        publishMembershipMessageAndWaitToBeConsumed("foobar");
        assertNoEmailWasSent();
    }

    @Test
    public void forwardCompatibility() throws Exception {
        String type = "memberSignedUpEvent", email = "clark.kent@example.com";
        String memberSignedUpEvent = format("{\"@type\":\"%s\",\"email\":\"%s\",\"foo\":\"bar\"}", type, email);
        publishMembershipMessageAndWaitToBeConsumed(memberSignedUpEvent);
        assertAnEmailWasSent();
        assertThat(getLastSentEmail().getEnvelopeReceiver(), equalTo(email));
    }
}
