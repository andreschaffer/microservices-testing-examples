package specialmembershipservice.it;

import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import com.github.restdriver.clientdriver.ClientDriverResponse;
import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import specialmembershipservice.bootstrap.SpecialMembershipServiceApplication;
import specialmembershipservice.bootstrap.SpecialMembershipServiceConfiguration;
import specialmembershipservice.it.client.ResourcesClient;

import javax.ws.rs.core.Response;
import java.util.Map;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.*;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static specialmembershipservice.it.DateFormatMatcher.isIsoDateFormat;
import static specialmembershipservice.it.IntegrationEnvironment.*;

public class SpecialMembershipsIT {

    private static final EphemeralKafkaBroker KAFKA_BROKER = EphemeralKafkaBroker.create(KAFKA_PORT);
    private static final KafkaJunitRule KAFKA_RULE = new KafkaJunitRule(KAFKA_BROKER);

    private static final DropwizardAppRule<SpecialMembershipServiceConfiguration> SPECIAL_MEMBERSHIP_SERVICE_RULE =
            new DropwizardAppRule<>(SpecialMembershipServiceApplication.class, INTEGRATION_YML);

    @ClassRule
    public static final RuleChain RULES = RuleChain.outerRule(KAFKA_RULE).around(SPECIAL_MEMBERSHIP_SERVICE_RULE);

    private static ResourcesClient resourcesClient;

    @BeforeClass
    public static void setUpClass() throws Exception {
        resourcesClient = new ResourcesClient(
                SPECIAL_MEMBERSHIP_SERVICE_RULE.getEnvironment(), SPECIAL_MEMBERSHIP_SERVICE_RULE.getLocalPort());
    }

    @Rule
    public ClientDriverRule creditScoreServiceRule = new ClientDriverRule(CREDIT_SCORE_SERVICE_PORT);

    @Test
    public void create() throws Exception {
        String email = "tony.stark@example.com";
        setCreditScoreServiceResponse(email, giveResponse("{\"creditScore\":850}", APPLICATION_JSON));
        Map<String, Object> specialMembershipDto = singletonMap("email", email);
        Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
        response.close();
        assertThat(response.getStatus(), equalTo(200));
        verifyPublishedMemberSignedUpEvent(email);
    }

    @Test
    public void denyDueToLowCreditScore() throws Exception {
        String email = "peter.parker@example.com";
        setCreditScoreServiceResponse(email, giveResponse("{\"creditScore\":300}", APPLICATION_JSON));
        Map<String, Object> specialMembershipDto = singletonMap("email", email);
        Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
        response.close();
        assertThat(response.getStatus(), equalTo(403));
    }

    @Test
    public void denyDueToNoCreditScore() throws Exception {
        String email = "ninja.turtle@example.com";
        setCreditScoreServiceResponse(email, giveEmptyResponse().withStatus(404));
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
        setCreditScoreServiceResponse(email, giveEmptyResponse().withStatus(500));
        Map<String, Object> specialMembershipDto = singletonMap("email", email);
        Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
        response.close();
        assertThat(response.getStatus(), equalTo(503));
    }

    @Test
    public void returnServiceUnavailableOnCreditScoreServiceTimeout() throws Exception {
        String email = "barry.allen@example.com";
        setCreditScoreServiceResponse(email, giveResponse("{\"creditScore\":300}", APPLICATION_JSON).after(3, SECONDS));
        Map<String, Object> specialMembershipDto = singletonMap("email", email);
        Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
        response.close();
        assertThat(response.getStatus(), equalTo(503));
    }

    @Test
    public void forwardCompatibility() throws Exception {
        String email = "marty.mcfly@example.com";
        setCreditScoreServiceResponse(email, giveResponse("{\"creditScore\":850,\"foo\":\"bar\"}", APPLICATION_JSON));
        Map<String, Object> specialMembershipDto = ImmutableMap.of("email", email, "foo", "bar");
        Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
        response.close();
        assertThat(response.getStatus(), equalTo(200));
        verifyPublishedMemberSignedUpEvent(email);
    }

    private void setCreditScoreServiceResponse(String email, ClientDriverResponse giveResponse) {
        creditScoreServiceRule.addExpectation(onRequestTo("/credit-scores/" + email).withMethod(GET), giveResponse);
    }

    private void verifyPublishedMemberSignedUpEvent(String email) throws Exception {
        String memberSignedUpEvent = KAFKA_RULE.helper().consumeStrings(SPECIAL_MEMBERSHIP_TOPIC, 1).get(1, SECONDS)
                .get(0);
        String eventType = "memberSignedUpEvent";
        assertThat(memberSignedUpEvent, hasJsonPath("$.@type", equalTo(eventType)));
        assertThat(memberSignedUpEvent, hasJsonPath("$.email", equalTo(email)));
        assertThat(memberSignedUpEvent, hasJsonPath("$.timestamp", isIsoDateFormat()));
    }
}
