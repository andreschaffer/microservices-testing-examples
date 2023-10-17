package creditscoreservice.bootstrap;

import creditscoreservice.port.incoming.adapter.resources.CreditScoreResource;
import creditscoreservice.port.outgoing.adapter.creditscore.InMemoryCreditScoreRepository;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;

public class CreditScoreServiceApplication extends Application<Configuration> {

  @Override
  public void run(Configuration configuration, Environment environment) throws Exception {
    ObjectMapperConfig.applyTo(environment.getObjectMapper());
    registerResources(environment);
  }

  private void registerResources(Environment environment) {
    InMemoryCreditScoreRepository creditScoreRepository = new InMemoryCreditScoreRepository();
    environment.jersey().register(new CreditScoreResource(creditScoreRepository));
  }
}
