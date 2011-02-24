package org.onebusaway.service_alerts.model.beans;

import java.util.List;

import org.onebusaway.service_alerts.model.SituationConfiguration;

public class ResolvedAlertBean extends AbstractAlertBean {

  private List<SituationConfiguration> configurations;

  public List<SituationConfiguration> getConfigurations() {
    return configurations;
  }

  public void setConfigurations(List<SituationConfiguration> configurations) {
    this.configurations = configurations;
  }
}
