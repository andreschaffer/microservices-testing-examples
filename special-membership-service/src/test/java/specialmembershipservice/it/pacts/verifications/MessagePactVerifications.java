package specialmembershipservice.it.pacts.verifications;

import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static specialmembershipservice.it.pacts.PactConstants.PACT_BROKER_PORT;
import static specialmembershipservice.it.pacts.PactConstants.PACT_BROKER_URL;
import static specialmembershipservice.it.pacts.PactConstants.SPECIAL_MEMBERSHIP_SERVICE;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junit.MessagePactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit.target.AmqpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.junit.Rule;
import org.junit.runner.RunWith;
import specialmembershipservice.it.IntegrationTestBase;
import specialmembershipservice.it.creditscoreservice.CreditScoreServiceRule;

@RunWith(MessagePactRunner.class)
@Provider(SPECIAL_MEMBERSHIP_SERVICE)
@IgnoreNoPactsToVerify
@PactBroker(
    host = "${pactbroker.host:" + PACT_BROKER_URL + "}", port = "${pactbroker.port:"
    + PACT_BROKER_PORT + "}",
    tags = {"${pactbroker.tags:prod}"},
    authentication = @PactBrokerAuth(
        username = "${pactbroker.user:ro_user}", password = "${pactbroker.pass:ro_pass}")
)
public class MessagePactVerifications extends IntegrationTestBase {

  @Rule
  public final CreditScoreServiceRule creditScoreServiceRule = new CreditScoreServiceRule(
      CREDIT_SCORE_SERVICE_PORT);

  @TestTarget
  public final Target target = new AmqpTarget(
      singletonList(this.getClass().getPackage().getName()));

  @State("Tony Stark became a new member")
  public void tonyStarkBecameANewMember() {
    String email = "tony.stark@example.com";
    creditScoreServiceRule.setCreditResponse(email,
        giveResponse(creditScoreDto(850), APPLICATION_JSON));
    Map<String, Object> specialMembershipDto = specialMembershipDto(email);
    Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
    response.close();
    assertThat(response.getStatus(), equalTo(200));
  }

  @PactVerifyProvider("An event notifying Tony Stark's new membership")
  public String verifyTonyStarksNewMembershipEvent() throws Exception {
    return readPublishedMessage();
  }
}
