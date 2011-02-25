package org.onebusaway.service_alerts.services;

import java.util.List;

public interface AlertResolutionService {

  public void resolveAlertToNothing(String unresolvedAlertId);

  public void resolveAlertToExistingAlert(String unresolvedAlertId,
      String existingResolvedAlertId);

  public void resolveAlertToExistingConfigurations(String unresolvedAlertId,
      List<String> alertConfigurationIds);
}
