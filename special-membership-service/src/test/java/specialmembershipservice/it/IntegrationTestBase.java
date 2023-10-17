package specialmembershipservice.it;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.util.Collections.singletonMap;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;
import specialmembershipservice.bootstrap.SpecialMembershipServiceApplication;
import specialmembershipservice.bootstrap.SpecialMembershipServiceConfiguration;
import specialmembershipservice.it.client.ResourcesClient;
import specialmembershipservice.it.kafka.KafkaContainerExtension;

public abstract class IntegrationTestBase {

  protected static final String CREDIT_SCORE_SERVICE_HOST = "localhost";
  protected static final int CREDIT_SCORE_SERVICE_PORT = 18088;
  protected static final String KAFKA_HOST = "localhost";
  protected static final int KAFKA_PORT = 19092;
  private static final String INTEGRATION_YML = resourceFilePath("integration.yml");
  private static final String SPECIAL_MEMBERSHIP_TOPIC = "special-membership-topic";

  @RegisterExtension
  @Order(0)
  private static WireMockExtension CREDIT_SCORE_SERVICE_RULE = WireMockExtension.newInstance()
      .options(wireMockConfig().port(CREDIT_SCORE_SERVICE_PORT))
      .build();

  @RegisterExtension
  @Order(1)
  private static final KafkaContainerExtension KAFKA_RULE = new KafkaContainerExtension(
      KAFKA_HOST, KAFKA_PORT);

  @RegisterExtension
  @Order(Integer.MAX_VALUE)
  private static final DropwizardAppExtension<SpecialMembershipServiceConfiguration> SERVICE_RULE =
      new DropwizardAppExtension<>(SpecialMembershipServiceApplication.class, INTEGRATION_YML);

  protected static ResourcesClient resourcesClient;

  @BeforeAll
  public static void setUpClass() throws Exception {
    resourcesClient = new ResourcesClient(SERVICE_RULE.getEnvironment(),
        SERVICE_RULE.getLocalPort());
  }

  protected String readPublishedMessage()
      throws InterruptedException, ExecutionException, TimeoutException {

    return readOneMessage(SPECIAL_MEMBERSHIP_TOPIC);
  }

  protected String readOneMessage(String topic) {
    return KAFKA_RULE.consumeString(topic, Duration.ofSeconds(5));
  }

  protected Map<String, Object> specialMembershipDto(String email) {
    return singletonMap("email", email);
  }

  protected String creditScoreDto(Integer creditScore) {
    return String.format("{\"creditScore\":%d}", creditScore);
  }

  protected void setCreditResponse(String email, ResponseDefinitionBuilder response) {
    CREDIT_SCORE_SERVICE_RULE.stubFor(get("/credit-scores/" + email).willReturn(response));
  }
}
