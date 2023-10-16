package specialmembershipservice.it.pacts;

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static specialmembershipservice.it.pacts.PactConstants.CREDIT_SCORE_SERVICE;
import static specialmembershipservice.it.pacts.PactConstants.SPECIAL_MEMBERSHIP_SERVICE;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import specialmembershipservice.it.IntegrationTestBase;

@ExtendWith(PactConsumerTestExt.class)
public class CreditScoreServicePactContracts extends IntegrationTestBase {

  @Pact(provider = CREDIT_SCORE_SERVICE, consumer = SPECIAL_MEMBERSHIP_SERVICE)
  public RequestResponsePact tonyStarkCreditScore(PactDslWithProvider pact) {
    return pact.given("There is a tony.stark@example.com")
        .uponReceiving("A credit score request for tony.stark@example.com")
        .path("/credit-scores/tony.stark@example.com").method("GET")
        .willRespondWith()
        .status(200)
        .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON))
        .body(new PactDslJsonBody().integerType("creditScore", 850))
        .toPact();
  }

  @Test
  @PactTestFor(providerName = CREDIT_SCORE_SERVICE, pactMethod = "tonyStarkCreditScore")
  public void createSpecialMembershipToTonyStark() throws Exception {
    Map<String, Object> specialMembershipDto = specialMembershipDto("tony.stark@example.com");
    Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
    response.close();
    assertThat(response.getStatus(), equalTo(200));
  }

  @Pact(provider = CREDIT_SCORE_SERVICE, consumer = SPECIAL_MEMBERSHIP_SERVICE)
  public RequestResponsePact hawleyGriffinCreditScore(PactDslWithProvider pact) {
    return pact.given("There is not a hawley.griffin@example.com")
        .uponReceiving("A credit score request for hawley.griffin@example.com")
        .path("/credit-scores/hawley.griffin@example.com").method("GET")
        .willRespondWith()
        .status(404)
        .toPact();
  }

  @Test
  @PactTestFor(providerName = CREDIT_SCORE_SERVICE, pactMethod = "hawleyGriffinCreditScore")
  public void denySpecialMembershipToHawleyGriffin() throws Exception {
    Map<String, Object> specialMembershipDto = specialMembershipDto("hawley.griffin@example.com");
    Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
    response.close();
    assertThat(response.getStatus(), equalTo(403));
  }
}
