package org.onebusaway.service_alerts.model;

import java.util.List;

public final class ResolvedAlert extends AbstractAlert {

  private static final long serialVersionUID = 1L;

  private List<SituationConfiguration> configurations;

  public List<SituationConfiguration> getConfigurations() {
    return configurations;
  }

  public void setConfigurations(List<SituationConfiguration> configurations) {
    this.configurations = configurations;
  }
}
