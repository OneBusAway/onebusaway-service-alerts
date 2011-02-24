package org.onebusaway.service_alerts.model;

import java.util.List;

public class ResolvedAlert extends AbstractAlert {

  private List<SituationConfiguration> configurations;

  public List<SituationConfiguration> getConfigurations() {
    return configurations;
  }

  public void setConfigurations(List<SituationConfiguration> configurations) {
    this.configurations = configurations;
  }
}
