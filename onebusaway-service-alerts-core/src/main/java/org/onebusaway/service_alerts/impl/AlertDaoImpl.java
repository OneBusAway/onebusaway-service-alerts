package org.onebusaway.service_alerts.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.onebusaway.service_alerts.model.ResolvedAlert;
import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.model.UnresolvedAlert;
import org.onebusaway.service_alerts.model.properties.AlertProperties;
import org.onebusaway.service_alerts.services.AlertDao;
import org.onebusaway.service_alerts.services.AlertRemover;
import org.springframework.stereotype.Component;

/**
 * TODO : Lots of work... make it thread safe or backed by database
 * 
 * @author bdferris
 */
@Component
class AlertDaoImpl implements AlertDao {

  private SituationConfigurationIndex _configurations = new SituationConfigurationIndex();

  private AlertIndex<UnresolvedAlert> _unresolvedAlerts = new AlertIndex<UnresolvedAlert>();

  private ResolvedAlertIndex _resolvedAlerts = new ResolvedAlertIndex();

  @Override
  public Collection<SituationConfiguration> getConfigurations() {
    return _configurations.getConfigurations();
  }

  @Override
  public SituationConfiguration getConfigurationForId(String id) {
    return _configurations.getConfigurationForId(id);
  }

  @Override
  public Collection<SituationConfiguration> getConfigurationsForGroup(
      AlertProperties group) {
    return _configurations.getConfigurationsForGroup(group);
  }

  @Override
  public Collection<SituationConfiguration> getConfigurationsForKey(
      AlertProperties key) {
    return _configurations.getConfigurationsForKey(key);
  }

  @Override
  public void addConfiguration(SituationConfiguration config) {
    _configurations.addConfiguration(config);
  }

  @Override
  public void addKeyToConfiguration(SituationConfiguration config,
      AlertProperties key) {
    _configurations.addKeyToConfiguration(config, key);
  }

  @Override
  public void removeKeyFromConfiguration(SituationConfiguration config,
      AlertProperties key) {
    _configurations.removeKeyFromConfiguration(config, key);
  }

  @Override
  public void removeConfiguration(SituationConfiguration config) {

    Collection<ResolvedAlert> resolvedAlerts = getResolvedAlertsForSituationConfigurationId(config.getId());

    for (ResolvedAlert resolvedAlert : resolvedAlerts)
      _resolvedAlerts.removeConfigurationFromAlert(config, resolvedAlert);

    _configurations.removeConfiguration(config);
  }

  /*****
   * 
   ****/

  @Override
  public Collection<ResolvedAlert> getAllResolvedAlerts() {
    return _resolvedAlerts.getAlerts();
  }

  @Override
  public ResolvedAlert getResolvedAlertForId(String id) {
    return _resolvedAlerts.getAlertForId(id);
  }

  @Override
  public ResolvedAlert getResolvedAlertForKey(AlertProperties key) {
    return _resolvedAlerts.getAlertForKey(key);
  }

  @Override
  public Collection<ResolvedAlert> getResolvedAlertsWithGroup(
      AlertProperties group) {
    return _resolvedAlerts.getAlertsForGroup(group);
  }

  @Override
  public Collection<ResolvedAlert> getResolvedAlertsForSituationConfigurationId(
      String id) {
    return _resolvedAlerts.getAlertsForSituationConfigurationId(id);
  }

  @Override
  public Collection<ResolvedAlert> getResolvedAlertsForDataSourceId(
      String dataSourceId) {
    return _resolvedAlerts.getAlertsForDataSourceId(dataSourceId);
  }

  @Override
  public void addResolvedAlert(ResolvedAlert resolvedAlert) {
    _resolvedAlerts.addAlert(resolvedAlert);
  }

  @Override
  public void removeResolvedAlert(ResolvedAlert resolvedAlert) {
    _resolvedAlerts.removeAlert(resolvedAlert);
  }

  @Override
  public AlertRemover<ResolvedAlert> getResolvedAlertRemover() {
    return _resolvedAlerts;
  }

  /****
   * 
   ****/

  @Override
  public List<UnresolvedAlert> getAllUnresolvedAlerts() {
    return new ArrayList<UnresolvedAlert>(_unresolvedAlerts.getAlerts());
  }

  @Override
  public UnresolvedAlert getUnresolvedAlertForId(String id) {
    return _unresolvedAlerts.getAlertForId(id);
  }

  @Override
  public UnresolvedAlert getUnresolvedAlertForKey(AlertProperties key) {
    return _unresolvedAlerts.getAlertForKey(key);
  }

  @Override
  public Collection<UnresolvedAlert> getUnresolvedAlertsForDataSourceId(
      String dataSourceId) {
    return _unresolvedAlerts.getAlertsForDataSourceId(dataSourceId);
  }

  @Override
  public void addUnresolvedAlert(UnresolvedAlert unresolvedAlert) {
    _unresolvedAlerts.addAlert(unresolvedAlert);
  }

  @Override
  public void removeUnresolvedAlert(UnresolvedAlert unresolvedAlert) {
    _unresolvedAlerts.removeAlert(unresolvedAlert);
  }

  @Override
  public AlertRemover<UnresolvedAlert> getUnresolvedAlertRemover() {
    return _unresolvedAlerts;
  }
}
