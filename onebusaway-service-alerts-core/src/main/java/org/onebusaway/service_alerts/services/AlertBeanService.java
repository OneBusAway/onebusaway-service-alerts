package org.onebusaway.service_alerts.services;

import java.util.List;

import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.model.beans.ResolvedAlertBean;
import org.onebusaway.service_alerts.model.beans.UnresolvedAlertBean;
import org.onebusaway.service_alerts.model.properties.AlertProperties;

public interface AlertBeanService {

  public List<UnresolvedAlertBean> getUnresolvedAlerts();

  public UnresolvedAlertBean getUnresolvedAlertForId(String id);

  public List<ResolvedAlertBean> getResolvedAlerts();

  public List<ResolvedAlertBean> getResolvedAlertsWithGroup(
      AlertProperties group);

  public List<ResolvedAlertBean> getResolvedAlertsForSituationConfigurationId(
      String id);

  public ResolvedAlertBean getResolvedAlertForId(String id);

  public List<SituationConfiguration> getPotentialConfigurationsWithGroup(
      AlertProperties group);

  public SituationConfiguration getSituationConfigurationForId(String id);

  public void resolveAlertToExistingAlert(String unresolvedAlertId,
      String existingResolvedAlertId);

  public void resolveAlertToExistingConfiguration(String unresolvedAlertId,
      List<String> alertConfigurationIds);

}
