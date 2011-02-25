package org.onebusaway.service_alerts.model;

import java.util.List;

public final class ResolvedAlert extends AbstractAlert {

  private static final long serialVersionUID = 1L;

  private List<String> configurationIds;

  public List<String> getConfigurationIds() {
    return configurationIds;
  }

  public void setConfigurationIds(List<String> configurationIds) {
    this.configurationIds = configurationIds;
  }
}
