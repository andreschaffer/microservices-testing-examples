package specialmembershipservice.it.creditscoreservice;

import static com.github.restdriver.clientdriver.ClientDriverRequest.Method.GET;
import static com.github.restdriver.clientdriver.RestClientDriver.onRequestTo;

import com.github.restdriver.clientdriver.ClientDriverResponse;
import com.github.restdriver.clientdriver.ClientDriverRule;

public class CreditScoreServiceRule extends ClientDriverRule {

  public CreditScoreServiceRule(int port) {
    super(port);
  }

  public void setCreditResponse(String email, ClientDriverResponse giveResponse) {
    addExpectation(
        onRequestTo("/credit-scores/" + email)
            .withMethod(GET),
        giveResponse);
  }
}
