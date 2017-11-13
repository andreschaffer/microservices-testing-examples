package creditscoreservice.it.pacts.dependents;

import static creditscoreservice.it.pacts.PactConstants.CREDIT_SCORE_SERVICE;
import static creditscoreservice.it.pacts.PactConstants.PACTS_DOWNLOAD_FOLDER;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.RestPactRunner;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import creditscoreservice.it.IntegrationTestBase;
import org.junit.runner.RunWith;

@RunWith(RestPactRunner.class)
@PactFolder(PACTS_DOWNLOAD_FOLDER)
@Provider(CREDIT_SCORE_SERVICE)
public class HttpPactsVerifications extends IntegrationTestBase {

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
