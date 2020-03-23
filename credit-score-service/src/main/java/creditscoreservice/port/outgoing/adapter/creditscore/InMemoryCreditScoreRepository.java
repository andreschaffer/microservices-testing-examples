package creditscoreservice.port.outgoing.adapter.creditscore;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCreditScoreRepository {

  private final Map<String, CreditScore> creditScores = new ConcurrentHashMap<>();

  public Optional<CreditScore> lookup(String email) {
    return Optional.ofNullable(creditScores.get(email));
  }

  public void save(CreditScore creditScore) {
    creditScores.put(creditScore.getEmail(), creditScore);
  }
}
