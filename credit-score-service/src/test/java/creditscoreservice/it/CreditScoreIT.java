package creditscoreservice.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CreditScoreIT extends IntegrationTestBase {

    @Test
    public void saveCreditScore() throws Exception {
        String email = "bruce.wayne@example.com";
        Integer creditScore = 850;

        Response response = resourcesClient.putCreditScore(email, creditScoreDto(creditScore));
        JsonNode creditScoreDto = response.readEntity(JsonNode.class);
        assertThat(creditScoreDto.path("creditScore").intValue(), equalTo(creditScore));
        assertThat(creditScoreDto.path("email").textValue(), equalTo(email));
        assertThat(response.getStatus(), equalTo(200));
    }

    @Test
    public void updateCreditScore() throws Exception {
        String email = "slumdog.millionaire@example.com";
        Integer oldCreditScore = 300, newCreditScore = 850;
        setupCreditScoreState(email, oldCreditScore);

        Response response = resourcesClient.putCreditScore(email, creditScoreDto(newCreditScore));
        JsonNode creditScoreDto = response.readEntity(JsonNode.class);
        assertThat(creditScoreDto.path("creditScore").intValue(), equalTo(newCreditScore));
        assertThat(creditScoreDto.path("email").textValue(), equalTo(email));
        assertThat(response.getStatus(), equalTo(200));
    }

    @Test
    public void returnClientErrorForInvalidLowCreditScore() throws Exception {
        String email = "ant.man@example.com";
        Response response = resourcesClient.putCreditScore(email, creditScoreDto(299));
        response.close();
        assertThat(response.getStatus(), equalTo(422));
    }

    @Test
    public void returnClientErrorForInvalidHighCreditScore() throws Exception {
        String email = "scrooge.mcduck@example.com";
        Response response = resourcesClient.putCreditScore(email, creditScoreDto(851));
        response.close();
        assertThat(response.getStatus(), equalTo(422));
    }

    @Test
    public void returnCreditScore() throws Exception {
        String email = "nemo@example.com";
        Integer creditScore = 600;
        setupCreditScoreState(email, creditScore);

        Response response = resourcesClient.getCreditScore(email);
        JsonNode creditScoreDto = response.readEntity(JsonNode.class);
        assertThat(creditScoreDto.path("creditScore").intValue(), equalTo(creditScore));
        assertThat(creditScoreDto.path("email").textValue(), equalTo(email));
        assertThat(response.getStatus(), equalTo(200));
    }

    @Test
    public void returnNotFound() throws Exception {
        String email = "houdini@example.com";
        Response response = resourcesClient.getCreditScore(email);
        response.close();
        assertThat(response.getStatus(), equalTo(404));
    }
}
