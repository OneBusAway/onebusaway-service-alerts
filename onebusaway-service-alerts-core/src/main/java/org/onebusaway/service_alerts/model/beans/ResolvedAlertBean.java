package org.onebusaway.service_alerts.model.beans;

import java.util.List;

public class ResolvedAlertBean extends AbstractAlertBean {

  private List<AlertConfigurationBean> configurations;

  public List<AlertConfigurationBean> getConfigurations() {
    return configurations;
  }

  public void setConfigurations(List<AlertConfigurationBean> configurations) {
    this.configurations = configurations;
  }
}
