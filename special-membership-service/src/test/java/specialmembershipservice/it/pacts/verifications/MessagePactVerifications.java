package specialmembershipservice.it.pacts.verifications;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static specialmembershipservice.it.pacts.PactConstants.PACT_BROKER_PORT;
import static specialmembershipservice.it.pacts.PactConstants.PACT_BROKER_URL;
import static specialmembershipservice.it.pacts.PactConstants.SPECIAL_MEMBERSHIP_SERVICE;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junitsupport.target.TestTarget;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import specialmembershipservice.it.IntegrationTestBase;

@Provider(SPECIAL_MEMBERSHIP_SERVICE)
@IgnoreNoPactsToVerify
@PactBroker(
    host = "${pactbroker.host:" + PACT_BROKER_URL + "}",
    port = "${pactbroker.port:" + PACT_BROKER_PORT + "}",
    tags = {"${pactbroker.tags:prod}"},
    authentication = @PactBrokerAuth(
        username = "${pactbroker.user:ro_user}", password = "${pactbroker.pass:ro_pass}")
)
public class MessagePactVerifications extends IntegrationTestBase {

  @TestTarget
  public final MessageTestTarget target = new MessageTestTarget(
      singletonList(this.getClass().getPackage().getName()));

  @State("Tony Stark became a new member")
  public void tonyStarkBecameANewMember() {
    String email = "tony.stark@example.com";
    setCreditResponse(email, okForJson(creditScoreDto(850)));
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
