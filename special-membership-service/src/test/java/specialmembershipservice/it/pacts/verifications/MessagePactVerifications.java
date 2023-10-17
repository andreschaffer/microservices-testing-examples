package specialmembershipservice.it.pacts.verifications;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static specialmembershipservice.it.pacts.PactConstants.PACT_BROKER_PORT;
import static specialmembershipservice.it.pacts.PactConstants.PACT_BROKER_URL;
import static specialmembershipservice.it.pacts.PactConstants.SPECIAL_MEMBERSHIP_SERVICE;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
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

  @RegisterExtension
  @Order(0)
  private static WireMockExtension CREDIT_SCORE_SERVICE_RULE = WireMockExtension.newInstance()
      .options(wireMockConfig().port(CREDIT_SCORE_SERVICE_PORT))
      .build();

  @TestTemplate
  @ExtendWith(PactVerificationInvocationContextProvider.class)
  public void pactVerificationTestTemplate(PactVerificationContext context) {
    context.verifyInteraction();
  }

  @BeforeEach
  public void before(PactVerificationContext context) {
    context.setTarget(new MessageTestTarget(singletonList(this.getClass().getPackage().getName())));
  }

  @State("Tony Stark became a new member")
  public void tonyStarkBecameANewMember() {
    String email = "tony.stark@example.com";
    setCreditResponse(email, responseDefinition().withBody(creditScoreDto(850))
        .withHeader(CONTENT_TYPE, APPLICATION_JSON));
    Map<String, Object> specialMembershipDto = specialMembershipDto(email);
    Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
    response.close();
    assertThat(response.getStatus(), equalTo(200));
  }

  @PactVerifyProvider("An event notifying Tony Stark's new membership")
  public String verifyTonyStarksNewMembershipEvent() throws Exception {
    return readPublishedMessage();
  }

  protected void setCreditResponse(String email, ResponseDefinitionBuilder response) {
    CREDIT_SCORE_SERVICE_RULE.stubFor(get("/credit-scores/" + email).willReturn(response));
  }
}
