package specialmembershipservice.it.pacts.verifications;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junit.MessagePactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.target.AmqpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import org.junit.Rule;
import org.junit.runner.RunWith;
import specialmembershipservice.it.IntegrationTestBase;
import specialmembershipservice.it.creditscoreservice.CreditScoreServiceRule;

import javax.ws.rs.core.Response;
import java.util.Map;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static specialmembershipservice.it.pacts.PactConstants.*;

@RunWith(MessagePactRunner.class)
@Provider(SPECIAL_MEMBERSHIP_SERVICE)
@IgnoreNoPactsToVerify
@PactBroker(host = PACT_BROKER_URL, port = PACT_BROKER_PORT, tags = {"${pactbroker.tags:prod}"})
public class MessagePactVerifications extends IntegrationTestBase {

    @Rule
    public final CreditScoreServiceRule creditScoreServiceRule = new CreditScoreServiceRule(CREDIT_SCORE_SERVICE_PORT);

    @TestTarget
    public final Target target = new AmqpTarget(singletonList(this.getClass().getPackage().getName()));

    @State("Tony Stark became a new member")
    public void tonyStarkBecameANewMember() {
        String email = "tony.stark@example.com";
        creditScoreServiceRule.setCreditResponse(email,
            giveResponse("{\"creditScore\":850}", APPLICATION_JSON));
        Map<String, Object> specialMembershipDto = singletonMap("email", email);
        Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
        response.close();
        assertThat(response.getStatus(), equalTo(200));
    }

    @PactVerifyProvider("An event notifying Tony Stark's new membership")
    public String verifyTonyStarksNewMembershipEvent() throws Exception {
        return readPublishedMessage();
    }
}
