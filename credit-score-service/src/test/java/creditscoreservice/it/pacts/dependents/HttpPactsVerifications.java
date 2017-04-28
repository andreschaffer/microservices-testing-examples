package creditscoreservice.it.pacts.dependents;

import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import creditscoreservice.bootstrap.CreditScoreServiceApplication;
import creditscoreservice.bootstrap.CreditScoreServiceConfiguration;
import creditscoreservice.it.StateSetup;
import creditscoreservice.it.client.ResourcesClient;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

import static creditscoreservice.it.IntegrationEnvironment.INTEGRATION_YML;
import static creditscoreservice.it.pacts.PactConstants.CREDIT_SCORE_SERVICE;
import static creditscoreservice.it.pacts.PactConstants.PACTS_DOWNLOAD_FOLDER;

@RunWith(PactRunner.class)
@PactFolder(PACTS_DOWNLOAD_FOLDER)
@Provider(CREDIT_SCORE_SERVICE)
public class HttpPactsVerifications {

    @ClassRule
    public static final DropwizardAppRule<CreditScoreServiceConfiguration> CREDIT_SCORE_SERVICE_RULE =
            new DropwizardAppRule<>(CreditScoreServiceApplication.class, INTEGRATION_YML);

    private static StateSetup stateSetup;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ResourcesClient resourcesClient = new ResourcesClient(
                CREDIT_SCORE_SERVICE_RULE.getEnvironment(), CREDIT_SCORE_SERVICE_RULE.getLocalPort());
        stateSetup = new StateSetup(resourcesClient);
    }

    @TestTarget
    public final Target target = new HttpTarget(CREDIT_SCORE_SERVICE_RULE.getLocalPort());

    @State("There is a tony.stark@example.com")
    public void tonyStarkCreditScore() {
        stateSetup.newCreditScore("tony.stark@example.com", 850);
    }

    @State("There is not a hawley.griffin@example.com")
    public void hawleyGriffinCreditScore() {
        // do nothing
    }
}
