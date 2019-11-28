package creditscoreservice.it.pacts.verifications;

import au.com.dius.pact.provider.junit.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.RestPactRunner;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import creditscoreservice.it.IntegrationTestBase;
import org.junit.runner.RunWith;

import static creditscoreservice.it.pacts.PactConstants.*;

@RunWith(RestPactRunner.class)
@Provider(CREDIT_SCORE_SERVICE)
@IgnoreNoPactsToVerify
@PactBroker(
        host = "${pactbroker.host:" + PACT_BROKER_URL + "}", port = "${pactbroker.port:" + PACT_BROKER_PORT + "}",
        tags = {"${pactbroker.tags:prod}"},
        authentication = @PactBrokerAuth(
                username = "${pactbroker.user:ro_user}", password = "${pactbroker.pass:ro_pass}")
)
public class HttpPactVerifications extends IntegrationTestBase {

    @TestTarget
    public final Target target = new HttpTarget(SERVICE_RULE.getLocalPort());

    @State("There is a tony.stark@example.com")
    public void tonyStarkCreditScore() {
        setupCreditScoreState("tony.stark@example.com", 850);
    }

    @State("There is not a hawley.griffin@example.com")
    public void hawleyGriffinCreditScore() {
        // do nothing
    }
}
