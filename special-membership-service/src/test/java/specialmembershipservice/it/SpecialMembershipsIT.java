package specialmembershipservice.it;

import static com.github.restdriver.clientdriver.RestClientDriver.giveEmptyResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static specialmembershipservice.it.matchers.DateFormatMatcher.isIsoDateFormat;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.junit.Rule;
import org.junit.Test;
import specialmembershipservice.it.creditscoreservice.CreditScoreServiceRule;

public class SpecialMembershipsIT extends IntegrationTestBase {

    @Rule
    public final CreditScoreServiceRule creditScoreServiceRule =
        new CreditScoreServiceRule(CREDIT_SCORE_SERVICE_PORT);

    @Test
    public void create() throws Exception {
        String email = "tony.stark@example.com";
        creditScoreServiceRule.setCreditResponse(email,
            giveResponse("{\"creditScore\":850}", APPLICATION_JSON));
        Map<String, Object> specialMembershipDto = singletonMap("email", email);
        Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
        response.close();
        assertThat(response.getStatus(), equalTo(200));
        verifyPublishedMemberSignedUpEvent(email);
    }

    @Test
    public void denyDueToLowCreditScore() throws Exception {
        String email = "peter.parker@example.com";
        creditScoreServiceRule.setCreditResponse(email,
            giveResponse("{\"creditScore\":300}", APPLICATION_JSON));
        Map<String, Object> specialMembershipDto = singletonMap("email", email);
        Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
        response.close();
        assertThat(response.getStatus(), equalTo(403));
    }

    @Test
    public void denyDueToNoCreditScore() throws Exception {
        String email = "ninja.turtle@example.com";
        creditScoreServiceRule.setCreditResponse(email, giveEmptyResponse().withStatus(404));
        Map<String, Object> specialMembershipDto = singletonMap("email", email);
        Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
        response.close();
        assertThat(response.getStatus(), equalTo(403));
    }

    @Test
    public void returnClientErrorForMissingEmail() throws Exception {
        Map<String, Object> specialMembershipDto = emptyMap();
        Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
        response.close();
        assertThat(response.getStatus(), equalTo(422));
    }

    @Test
    public void returnClientErrorForBadEmail() throws Exception {
        Map<String, Object> specialMembershipDto = ImmutableMap.of("email", "notAnEmail");
        Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
        response.close();
        assertThat(response.getStatus(), equalTo(422));
    }

    @Test
    public void returnServiceUnavailableOnCreditScoreServiceError() throws Exception {
        String email = "the.joker@example.com";
        creditScoreServiceRule.setCreditResponse(email, giveEmptyResponse().withStatus(500));
        Map<String, Object> specialMembershipDto = singletonMap("email", email);
        Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
        response.close();
        assertThat(response.getStatus(), equalTo(503));
    }

    @Test
    public void returnServiceUnavailableOnCreditScoreServiceTimeout() throws Exception {
        String email = "barry.allen@example.com";
        creditScoreServiceRule.setCreditResponse(email,
            giveResponse("{\"creditScore\":300}", APPLICATION_JSON).after(3, SECONDS));
        Map<String, Object> specialMembershipDto = singletonMap("email", email);
        Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
        response.close();
        assertThat(response.getStatus(), equalTo(503));
    }

    @Test
    public void forwardCompatibility() throws Exception {
        String email = "marty.mcfly@example.com";
        creditScoreServiceRule.setCreditResponse(email,
            giveResponse("{\"creditScore\":850,\"foo\":\"bar\"}", APPLICATION_JSON));
        Map<String, Object> specialMembershipDto = ImmutableMap.of("email", email, "foo", "bar");
        Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
        response.close();
        assertThat(response.getStatus(), equalTo(200));
        verifyPublishedMemberSignedUpEvent(email);
    }

    private void verifyPublishedMemberSignedUpEvent(String email) throws Exception {
        String memberSignedUpEvent = readPublishedMessage();
        String eventType = "memberSignedUpEvent";
        assertThat(memberSignedUpEvent, hasJsonPath("$.@type", equalTo(eventType)));
        assertThat(memberSignedUpEvent, hasJsonPath("$.email", equalTo(email)));
        assertThat(memberSignedUpEvent, hasJsonPath("$.timestamp", isIsoDateFormat()));
    }
}
