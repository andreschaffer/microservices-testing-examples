package specialmembershipservice.port.outgoing.adapter.creditscore;

import javax.validation.constraints.NotNull;

public class CreditScoreDto {

    @NotNull
    private Integer creditScore;

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }
}
