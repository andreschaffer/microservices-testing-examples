package creditscoreservice.it.pacts.verifications;

import static creditscoreservice.it.pacts.PactConstants.CREDIT_SCORE_SERVICE;
import static creditscoreservice.it.pacts.PactConstants.PACT_BROKER_PORT;
import static creditscoreservice.it.pacts.PactConstants.PACT_BROKER_URL;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import creditscoreservice.it.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@Provider(CREDIT_SCORE_SERVICE)
@IgnoreNoPactsToVerify
@PactBroker(
    host = "${pactbroker.host:" + PACT_BROKER_URL + "}",
    port = "${pactbroker.port:" + PACT_BROKER_PORT + "}",
    tags = {"${pactbroker.tags:prod}"},
    authentication = @PactBrokerAuth(
        username = "${pactbroker.user:ro_user}", password = "${pactbroker.pass:ro_pass}")
)
public class HttpPactVerifications extends IntegrationTestBase {

  @TestTemplate
  @ExtendWith(PactVerificationInvocationContextProvider.class)
  public void pactVerificationTestTemplate(PactVerificationContext context) {
    context.verifyInteraction();
  }

  @BeforeEach
  public void before(PactVerificationContext context) {
    context.setTarget(new HttpTestTarget("localhost", SERVICE_RULE.getLocalPort()));
  }

  @State("There is a tony.stark@example.com")
  public void tonyStarkCreditScore() {
    setupCreditScoreState("tony.stark@example.com", 850);
  }

  @State("There is not a hawley.griffin@example.com")
  public void hawleyGriffinCreditScore() {
    // do nothing
  }
}
