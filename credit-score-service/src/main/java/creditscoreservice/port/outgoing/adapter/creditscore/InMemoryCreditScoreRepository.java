package creditscoreservice.port.outgoing.adapter.creditscore;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCreditScoreRepository {

    private final Map<String, Integer> creditScores = new ConcurrentHashMap<>();

    public Optional<Integer> lookup(String email) {
        return Optional.ofNullable(creditScores.get(email));
    }

    public void save(String email, Integer creditScore) {
        creditScores.put(email, creditScore);
    }
}
