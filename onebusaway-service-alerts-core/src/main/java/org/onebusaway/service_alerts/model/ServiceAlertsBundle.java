package org.onebusaway.service_alerts.model;

import java.io.File;

public class ServiceAlertsBundle {

  private File _path;

  public void setPath(File path) {
    _path = path;
  }

  public File getSituationConfigurationsPath() {
    return new File(_path, "SituationConfigurations.obj");
  }

  public File getResolvedAlertsPath() {
    return new File(_path, "ResolvedAlerts.obj");
  }
}
