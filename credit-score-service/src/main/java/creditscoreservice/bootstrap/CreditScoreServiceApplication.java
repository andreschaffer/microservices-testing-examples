package creditscoreservice.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import creditscoreservice.port.incoming.adapter.resources.CreditScoreResource;
import creditscoreservice.port.outgoing.adapter.creditscore.InMemoryCreditScoreRepository;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class CreditScoreServiceApplication extends Application<CreditScoreServiceConfiguration> {

    @Override
    public void run(CreditScoreServiceConfiguration configuration, Environment environment) throws Exception {
        configureObjectMapper(environment.getObjectMapper());
        registerResources(configuration, environment);
    }

    private void configureObjectMapper(ObjectMapper objectMapper) {
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private void registerResources(CreditScoreServiceConfiguration configuration, Environment environment) {
        InMemoryCreditScoreRepository creditScoreRepository = new InMemoryCreditScoreRepository();
        environment.jersey().register(new CreditScoreResource(creditScoreRepository));
    }
}
