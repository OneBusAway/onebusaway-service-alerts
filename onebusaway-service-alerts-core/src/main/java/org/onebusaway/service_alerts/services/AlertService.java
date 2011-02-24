package org.onebusaway.service_alerts.services;

import java.util.Collection;
import java.util.List;

import org.onebusaway.service_alerts.model.ResolvedAlert;
import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.model.UnresolvedAlert;
import org.onebusaway.service_alerts.model.properties.AlertProperties;

public interface AlertService {

  public List<UnresolvedAlert> getUnresolvedAlerts();

  public UnresolvedAlert getUnresolvedAlertForId(String id);

  public ResolvedAlert getResolvedAlertForId(String id);

  public Collection<ResolvedAlert> getResolvedAlerts();

  public Collection<ResolvedAlert> getResolvedAlertsWithGroup(
      AlertProperties group);

  public Collection<ResolvedAlert> getResolvedAlertsForSituationConfigurationId(
      String id);

  public Collection<SituationConfiguration> getPotentialConfigurationsWithGroup(
      AlertProperties group);

  public SituationConfiguration getSituationConfigurationForId(String id);

  public void resolveAlertToExistingAlert(String unresolvedAlertId,
      String existingResolvedAlertId);

  public void resolveAlertToExistingConfigurations(String unresolvedAlertId,
      List<String> alertConfigurationIds);
}
