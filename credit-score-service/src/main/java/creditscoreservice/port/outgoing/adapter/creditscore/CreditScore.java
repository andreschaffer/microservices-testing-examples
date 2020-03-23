package creditscoreservice.port.outgoing.adapter.creditscore;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class CreditScore {

  private String email;
  private Integer creditScore;

  public CreditScore(String email, Integer creditScore) {
    this.email = checkNotNull(email);
    checkArgument(creditScore >= 300 && creditScore <= 850);
    this.creditScore = creditScore;
  }

  public String getEmail() {
    return email;
  }

  public Integer getCreditScore() {
    return creditScore;
  }
}
