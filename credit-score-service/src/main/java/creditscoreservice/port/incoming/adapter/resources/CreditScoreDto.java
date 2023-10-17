package creditscoreservice.port.incoming.adapter.resources;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreditScoreDto {

  @JsonProperty(access = READ_ONLY)
  private String email;

  @NotNull
  @Min(300)
  @Max(850)
  private Integer creditScore;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Integer getCreditScore() {
    return creditScore;
  }

  public void setCreditScore(Integer creditScore) {
    this.creditScore = creditScore;
  }
}
