package creditscoreservice.it;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import creditscoreservice.bootstrap.CreditScoreServiceApplication;
import creditscoreservice.it.client.ResourcesClient;
import io.dropwizard.core.Configuration;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

public abstract class IntegrationTestBase {

  private static final String INTEGRATION_YML = resourceFilePath("integration.yml");

  @RegisterExtension
  @Order(Integer.MAX_VALUE)
  public static final DropwizardAppExtension<Configuration> SERVICE_RULE =
      new DropwizardAppExtension<>(CreditScoreServiceApplication.class, INTEGRATION_YML);

  protected static ResourcesClient resourcesClient;

  @BeforeAll
  public static void setUpClass() {
    resourcesClient = new ResourcesClient(SERVICE_RULE.getEnvironment(),
        SERVICE_RULE.getLocalPort());
  }

  protected void setupCreditScoreState(String email, Integer creditScore) {
    Response response = resourcesClient.putCreditScore(email, creditScoreDto(creditScore));
    response.close();
    assertThat(response.getStatus(), equalTo(200));
  }

  protected Map<String, Object> creditScoreDto(Integer creditScore) {
    return singletonMap("creditScore", creditScore);
  }
}
