package org.onebusaway.service_alerts.services;

import java.util.List;

import org.onebusaway.service_alerts.model.AlertDescription;

public interface AlertDescriptionService {
  public void setActiveAlertDescriptions(String dataSourceId,
      List<AlertDescription> descriptions);
}
