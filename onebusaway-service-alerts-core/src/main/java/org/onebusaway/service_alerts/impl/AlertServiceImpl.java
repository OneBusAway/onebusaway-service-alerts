package org.onebusaway.service_alerts.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.onebusaway.service_alerts.model.AbstractAlert;
import org.onebusaway.service_alerts.model.AlertDescription;
import org.onebusaway.service_alerts.model.NotFoundException;
import org.onebusaway.service_alerts.model.ResolvedAlert;
import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.model.UnresolvedAlert;
import org.onebusaway.service_alerts.model.properties.AlertProperties;
import org.onebusaway.service_alerts.services.AlertDescriptionService;
import org.onebusaway.service_alerts.services.AlertService;
import org.onebusaway.service_alerts.services.SituationService;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class AlertServiceImpl implements AlertDescriptionService, AlertService {

  private static Logger _log = LoggerFactory.getLogger(AlertServiceImpl.class);

  private AlertIndex<UnresolvedAlert> _unresolvedAlerts = new AlertIndex<UnresolvedAlert>();

  private ResolvedAlertIndex _resolvedAlerts = new ResolvedAlertIndex();

  private SituationService _situationService;

  private File _path;

  @Autowired
  public void setSituationService(SituationService situationService) {
    _situationService = situationService;
  }

  public void setPath(File path) {
    _path = path;
  }

  @PostConstruct
  public void setup() throws IOException, ClassNotFoundException {
    if (_path != null && _path.exists()) {
      Map<String, SituationConfiguration> configurations = ObjectSerializationLibrary.readObject(_path);
      for (SituationConfiguration configuration : configurations.values())
        _situationService.createSituation(configuration);
    }
  }

  /****
   * {@link AlertDescriptionService} Interface
   ****/

  @Override
  public void setActiveAlertDescriptions(String dataSourceId,
      List<AlertDescription> activeAlerts) {

    long time = System.currentTimeMillis();

    int index = 0;

    for (AlertDescription alert : activeAlerts) {
      processAlertDescription(alert, index, time, dataSourceId);
      index++;
    }

    /**
     * We can clear out any unresolved alerts that haven't been updated in this
     * round
     */
    Set<AlertProperties> unresolvedGroups = clearStaleAlerts(_unresolvedAlerts,
        _unresolvedAlerts.getAlertsForDataSourceId(dataSourceId), time,
        new HashSet<AlertProperties>());

    /**
     * We can clear out any alerts that haven't been updated since the last
     * route if there are not unresolved alerts with the same route+region
     */
    clearStaleAlerts(_resolvedAlerts,
        _resolvedAlerts.getAlertsForDataSourceId(dataSourceId), time,
        unresolvedGroups);
  }

  /****
   * {@link AlertService} Interface
   ****/

  public synchronized List<UnresolvedAlert> getUnresolvedAlerts() {
    return new ArrayList<UnresolvedAlert>(_unresolvedAlerts.getAlerts());
  }

  @Override
  public UnresolvedAlert getUnresolvedAlertForId(String id) {
    return _unresolvedAlerts.getAlertForId(id);
  }

  @Override
  public Collection<ResolvedAlert> getResolvedAlerts() {
    return _resolvedAlerts.getAlerts();
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
  public Collection<SituationConfiguration> getPotentialConfigurationsWithGroup(
      AlertProperties group) {
    return _situationService.getSituationsForGroup(group);
  }

  @Override
  public SituationConfiguration getSituationConfigurationForId(String id) {
    return _situationService.getSituationForId(id);
  }

  @Override
  public ResolvedAlert getResolvedAlertForId(String id) {
    return _resolvedAlerts.getAlertForId(id);
  }

  @Override
  public void resolveAlertToExistingAlert(String unresolvedAlertId,
      String existingResolvedAlertId) {

    UnresolvedAlert unresolvedAlert = _unresolvedAlerts.getAlertForId(unresolvedAlertId);

    if (unresolvedAlert == null)
      throw new NotFoundException(UnresolvedAlert.class, unresolvedAlertId);

    ResolvedAlert resolvedAlert = _resolvedAlerts.getAlertForId(existingResolvedAlertId);

    if (resolvedAlert == null)
      throw new NotFoundException(ResolvedAlert.class, existingResolvedAlertId);

    _unresolvedAlerts.removeAlert(unresolvedAlert);
    _resolvedAlerts.removeAlert(resolvedAlert);

    resolvedAlert.setKey(unresolvedAlert.getKey());

    _resolvedAlerts.addAlert(resolvedAlert);

    for (SituationConfiguration config : resolvedAlert.getConfigurations()) {

      Set<AlertProperties> keys = config.getKeys();

      if (!keys.contains(unresolvedAlert.getKey())) {
        _situationService.addKeyToSituationConfiguration(config,
            unresolvedAlert.getKey());
      }
    }
  }

  @Override
  public void resolveAlertToExistingConfigurations(String unresolvedAlertId,
      List<String> alertConfigurationIds) {

    UnresolvedAlert unresolvedAlert = _unresolvedAlerts.getAlertForId(unresolvedAlertId);

    if (unresolvedAlert == null)
      throw new NotFoundException(UnresolvedAlert.class, unresolvedAlertId);

    ResolvedAlert resolvedAlert = new ResolvedAlert();
    resolvedAlert.setId(unresolvedAlert.getId());
    resolvedAlert.setTimeOfCreation(unresolvedAlert.getTimeOfCreation());
    resolvedAlert.setTimeOfLastUpdate(unresolvedAlert.getTimeOfLastUpdate());
    resolvedAlert.setGroup(unresolvedAlert.getGroup());
    resolvedAlert.setKey(unresolvedAlert.getKey());

    List<SituationConfiguration> configurations = new ArrayList<SituationConfiguration>();

    for (String alertConfigurationId : alertConfigurationIds) {
      SituationConfiguration config = _situationService.getSituationForId(alertConfigurationId);

      if (config == null)
        throw new NotFoundException(SituationConfiguration.class,
            alertConfigurationIds);

      configurations.add(config);

      Set<AlertProperties> keys = config.getKeys();

      if (!keys.contains(unresolvedAlert.getKey()))
        _situationService.addKeyToSituationConfiguration(config,
            unresolvedAlert.getKey());
    }

    resolvedAlert.setConfigurations(configurations);
    _resolvedAlerts.addAlert(resolvedAlert);
  }

  public synchronized void setActiveAlerts(List<AlertDescription> activeAlerts) {

  }

  /****
   * 
   ****/

  private void processAlertDescription(AlertDescription desc, int index,
      long time, String dataSourceId) {

    AlertProperties key = desc.getKey();

    ResolvedAlert resolvedAlert = _resolvedAlerts.getAlertForKey(key);

    /**
     * If we've previously seen and resolved this alert, then we set the update
     * time and move on
     */
    if (resolvedAlert != null) {
      resolvedAlert.setTimeOfLastUpdate(time);
      return;
    }

    UnresolvedAlert unresolvedAlert = _unresolvedAlerts.getAlertForKey(key);

    /**
     * If we've previously seen but have not yet resolved this alert, then we
     * set the update time and move on
     */
    if (unresolvedAlert != null) {
      unresolvedAlert.setTimeOfLastUpdate(time);
      return;
    }

    /**
     * We have an alert that we haven't seen before!
     */

    String id = "1_" + time + "-" + index;

    /**
     * Can we map the alert to an existing configuration?
     */
    SituationConfiguration configuration = _situationService.getSituationForKey(key);

    if (configuration != null) {
      resolvedAlert = new ResolvedAlert();
      resolvedAlert.setId(id);
      resolvedAlert.setTimeOfCreation(time);
      resolvedAlert.setTimeOfLastUpdate(time);
      resolvedAlert.setGroup(configuration.getGroup());
      resolvedAlert.setKey(key);
      resolvedAlert.setDataSourceId(dataSourceId);

      _log.debug("adding new resolved alert: {}", resolvedAlert);

      _resolvedAlerts.addAlert(resolvedAlert);

      return;
    }

    /**
     * Leave the alert unresolved
     */

    unresolvedAlert = new UnresolvedAlert();
    unresolvedAlert.setId(id);
    unresolvedAlert.setTimeOfCreation(time);
    unresolvedAlert.setTimeOfLastUpdate(time);
    unresolvedAlert.setGroup(desc.getGroup());
    unresolvedAlert.setKey(desc.getKey());
    unresolvedAlert.setFullDescription(desc);

    _log.debug("adding new unresolved alert: {}", unresolvedAlert);

    _unresolvedAlerts.addAlert(unresolvedAlert);
  }

  private <T extends AbstractAlert> Set<AlertProperties> clearStaleAlerts(
      AlertIndex<T> alertIndex, Collection<T> alerts, long time,
      Set<AlertProperties> unresolvedGroups) {

    Set<AlertProperties> newUnresolvedGroups = new HashSet<AlertProperties>();
    List<T> toRemove = new ArrayList<T>();

    for (T alert : alerts) {

      AlertProperties ref = alert.getGroup();

      if (alert.getTimeOfLastUpdate() < time && !unresolvedGroups.contains(ref)) {
        _log.debug("expiring alert: {}", alert);
        toRemove.add(alert);
      } else {
        newUnresolvedGroups.add(ref);
      }
    }

    for (T alert : toRemove)
      alertIndex.removeAlert(alert);

    return newUnresolvedGroups;
  }

}
