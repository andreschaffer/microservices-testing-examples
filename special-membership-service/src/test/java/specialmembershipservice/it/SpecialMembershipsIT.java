package specialmembershipservice.it;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson;
import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.common.ContentTypes.APPLICATION_JSON;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_TYPE;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static specialmembershipservice.it.matchers.DateFormatMatcher.isIsoDateFormat;

import jakarta.ws.rs.core.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class SpecialMembershipsIT extends IntegrationTestBase {

  @Test
  public void create() throws Exception {
    String email = "tony.stark@example.com";
    setCreditResponse(email, responseDefinition().withBody(creditScoreDto(850))
        .withHeader(CONTENT_TYPE, APPLICATION_JSON));
    Map<String, Object> specialMembershipDto = specialMembershipDto(email);
    Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
    response.close();
    assertThat(response.getStatus(), equalTo(200));
    verifyPublishedMemberSignedUpEvent(email);
  }

  @Test
  public void denyDueToLowCreditScore() throws Exception {
    String email = "peter.parker@example.com";
    setCreditResponse(email, responseDefinition().withBody(creditScoreDto(300))
        .withHeader(CONTENT_TYPE, APPLICATION_JSON));
    Map<String, Object> specialMembershipDto = specialMembershipDto(email);
    Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
    response.close();
    assertThat(response.getStatus(), equalTo(403));
  }

  @Test
  public void denyDueToNoCreditScore() throws Exception {
    String email = "ninja.turtle@example.com";
    setCreditResponse(email, responseDefinition().withStatus(404));
    Map<String, Object> specialMembershipDto = specialMembershipDto(email);
    Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
    response.close();
    assertThat(response.getStatus(), equalTo(403));
  }

  @Test
  public void returnClientErrorForMissingEmail() throws Exception {
    Map<String, Object> specialMembershipDto = emptyMap();
    Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
    response.close();
    assertThat(response.getStatus(), equalTo(422));
  }

  @Test
  public void returnClientErrorForBadEmail() throws Exception {
    Map<String, Object> specialMembershipDto = specialMembershipDto("notAnEmail");
    Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
    response.close();
    assertThat(response.getStatus(), equalTo(422));
  }

  @Test
  public void returnServiceUnavailableOnCreditScoreServiceError() throws Exception {
    String email = "the.joker@example.com";
    setCreditResponse(email, responseDefinition().withStatus(500));
    Map<String, Object> specialMembershipDto = specialMembershipDto(email);
    Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
    response.close();
    assertThat(response.getStatus(), equalTo(503));
  }

  @Test
  public void returnServiceUnavailableOnCreditScoreServiceTimeout() throws Exception {
    String email = "barry.allen@example.com";
    setCreditResponse(email, okForJson(creditScoreDto(300)).withFixedDelay(3000));
    Map<String, Object> specialMembershipDto = specialMembershipDto(email);
    Response response = resourcesClient.postSpecialMembership(specialMembershipDto);
    response.close();
    assertThat(response.getStatus(), equalTo(503));
  }

  private void verifyPublishedMemberSignedUpEvent(String email) throws Exception {
    String memberSignedUpEvent = readPublishedMessage();
    String eventType = "memberSignedUpEvent";
    assertThat(memberSignedUpEvent, hasJsonPath("$.@type", equalTo(eventType)));
    assertThat(memberSignedUpEvent, hasJsonPath("$.email", equalTo(email)));
    assertThat(memberSignedUpEvent, hasJsonPath("$.timestamp", isIsoDateFormat()));
  }
}
