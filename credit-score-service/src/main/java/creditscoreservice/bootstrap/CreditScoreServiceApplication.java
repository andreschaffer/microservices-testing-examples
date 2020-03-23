package creditscoreservice.bootstrap;

import creditscoreservice.port.incoming.adapter.resources.CreditScoreResource;
import creditscoreservice.port.outgoing.adapter.creditscore.InMemoryCreditScoreRepository;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

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
