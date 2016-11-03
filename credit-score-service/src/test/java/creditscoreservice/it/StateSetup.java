package creditscoreservice.it;

import creditscoreservice.it.client.ResourcesClient;

import javax.ws.rs.core.Response;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class StateSetup {

    private final ResourcesClient resourcesClient;

    public StateSetup(ResourcesClient resourcesClient) {
        this.resourcesClient = checkNotNull(resourcesClient);
    }

    public void newCreditScore(String email, Integer creditScore) {
        Response response = resourcesClient.putCreditScore(email, singletonMap("creditScore", creditScore));
        response.close();
        assertThat(response.getStatus(), equalTo(200));
    }
}
