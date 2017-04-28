package specialmembershipservice.it.pacts.dependents;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactBroker;
import au.com.dius.pact.provider.junit.target.AmqpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import com.github.restdriver.clientdriver.ClientDriverResponse;
import com.github.restdriver.clientdriver.ClientDriverRule;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import specialmembership.bootstrap.SpecialMembershipServiceApplication;
import specialmembership.bootstrap.SpecialMembershipServiceConfiguration;
import specialmembershipservice.it.client.ResourcesClient;

import javax.ws.rs.core.Response;
import java.util.Map;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.giveResponse;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static specialmembershipservice.it.IntegrationEnvironment.*;
import static specialmembershipservice.it.pacts.PactConstants.*;

@RunWith(PactRunner.class)
@PactBroker(host = BROKER_HOST, port = BROKER_PORT)
@Provider(SPECIAL_MEMBERSHIP_SERVICE)
public class MessagePactsVerifications {

    private static final EphemeralKafkaBroker KAFKA_BROKER = EphemeralKafkaBroker.create(KAFKA_PORT);
    private static final KafkaJunitRule KAFKA_RULE = new KafkaJunitRule(KAFKA_BROKER);

    private static final ClientDriverRule CREDIT_SCORE_SERVICE_RULE = new ClientDriverRule(CREDIT_SCORE_SERVICE_PORT);

    private static final DropwizardAppRule<SpecialMembershipServiceConfiguration> SPECIAL_MEMBERSHIP_SERVICE_RULE =
            new DropwizardAppRule<>(SpecialMembershipServiceApplication.class, INTEGRATION_YML);

    @ClassRule
    public static final RuleChain RULES = RuleChain
            .outerRule(KAFKA_RULE).around(CREDIT_SCORE_SERVICE_RULE).around(SPECIAL_MEMBERSHIP_SERVICE_RULE);

    private static ResourcesClient resourcesClient;

    @BeforeClass
    public static void setUpClass() throws Exception {
        resourcesClient = new ResourcesClient(
                SPECIAL_MEMBERSHIP_SERVICE_RULE.getEnvironment(), SPECIAL_MEMBERSHIP_SERVICE_RULE.getLocalPort());
    }

    @Before
    public void setUp() {
        CREDIT_SCORE_SERVICE_RULE.reset();
    }

    @TestTarget
    public final Target target = new AmqpTarget(singletonList(this.getClass().getPackage().getName() + ".*"));

    @State("Tony Stark became a new member")
    public void tonyStarkBecameANewMember() {
        String email = "tony.stark@example.com";
        setCreditScoreServiceResponse(email, giveResponse("{\"creditScore\":850}", APPLICATION_JSON));
        Map<String, Object> specialMembershipDto = singletonMap("email", email);
        Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
        response.close();
        assertThat(response.getStatus(), equalTo(200));
    }

    @PactVerifyProvider("An event notifying Tony Stark's new membership")
    public String verifyTonyStarksNewMembershipEvent() throws Exception {
        return KAFKA_RULE.helper().consumeStrings(SPECIAL_MEMBERSHIP_TOPIC, 1).get(1, SECONDS).get(0);
    }

    private void setCreditScoreServiceResponse(String email, ClientDriverResponse giveResponse) {
        CREDIT_SCORE_SERVICE_RULE.addExpectation(onRequestTo("/credit-scores/" + email).withMethod(GET), giveResponse);
    }
}
