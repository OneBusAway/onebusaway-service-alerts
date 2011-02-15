package org.onebusaway.king_county_metro.service_alerts.model;

import java.util.List;

public class ResolvedAlert extends AbstractAlert {

  private List<AlertConfiguration> configurations;

  public List<AlertConfiguration> getConfigurations() {
    return configurations;
  }

  public void setConfigurations(List<AlertConfiguration> configurations) {
    this.configurations = configurations;
  }
}
