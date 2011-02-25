package org.onebusaway.service_alerts.services;

import java.util.Collection;
import java.util.List;

import org.onebusaway.service_alerts.model.ResolvedAlert;
import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.model.UnresolvedAlert;
import org.onebusaway.service_alerts.model.properties.AlertProperties;

public interface AlertDao {

  public Collection<SituationConfiguration> getConfigurations();

  public SituationConfiguration getConfigurationForId(String id);

  public Collection<SituationConfiguration> getConfigurationsForGroup(
      AlertProperties group);

  public Collection<SituationConfiguration> getConfigurationsForKey(AlertProperties key);

  public void addConfiguration(SituationConfiguration config);

  public void addKeyToConfiguration(SituationConfiguration config,
      AlertProperties key);

  public void removeKeyFromConfiguration(SituationConfiguration config,
      AlertProperties key);

  public void removeConfiguration(SituationConfiguration config);

  /****
   * 
   ****/

  public Collection<ResolvedAlert> getAllResolvedAlerts();

  public ResolvedAlert getResolvedAlertForId(String id);

  public ResolvedAlert getResolvedAlertForKey(AlertProperties key);

  public Collection<ResolvedAlert> getResolvedAlertsWithGroup(
      AlertProperties group);

  public Collection<ResolvedAlert> getResolvedAlertsForSituationConfigurationId(
      String id);

  public Collection<ResolvedAlert> getResolvedAlertsForDataSourceId(
      String dataSourceId);

  public void addResolvedAlert(ResolvedAlert resolvedAlert);
  
  public void removeResolvedAlert(ResolvedAlert resolvedAlert);

  public AlertRemover<ResolvedAlert> getResolvedAlertRemover();

  /****
   * 
   ****/

  public List<UnresolvedAlert> getAllUnresolvedAlerts();

  public UnresolvedAlert getUnresolvedAlertForId(String id);

  public UnresolvedAlert getUnresolvedAlertForKey(AlertProperties key);

  public Collection<UnresolvedAlert> getUnresolvedAlertsForDataSourceId(
      String dataSourceId);

  public void addUnresolvedAlert(UnresolvedAlert unresolvedAlert);

  public void removeUnresolvedAlert(UnresolvedAlert unresolvedAlert);
  
  public AlertRemover<UnresolvedAlert> getUnresolvedAlertRemover();

}
