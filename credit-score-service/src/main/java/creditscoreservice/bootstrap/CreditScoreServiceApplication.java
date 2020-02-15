package creditscoreservice.bootstrap;

import creditscoreservice.port.incoming.adapter.resources.CreditScoreResource;
import creditscoreservice.port.outgoing.adapter.creditscore.InMemoryCreditScoreRepository;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class CreditScoreServiceApplication extends Application<CreditScoreServiceConfiguration> {

    @Override
    public void run(CreditScoreServiceConfiguration configuration, Environment environment) throws Exception {
        ObjectMapperConfig.applyTo(environment.getObjectMapper());
        registerResources(configuration, environment);
    }

    private void registerResources(CreditScoreServiceConfiguration configuration, Environment environment) {
        InMemoryCreditScoreRepository creditScoreRepository = new InMemoryCreditScoreRepository();
        environment.jersey().register(new CreditScoreResource(creditScoreRepository));
    }
}
