package org.onebusaway.service_alerts.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.service_alerts.model.AbstractAlert;
import org.onebusaway.service_alerts.model.AlertDescription;
import org.onebusaway.service_alerts.model.NotFoundException;
import org.onebusaway.service_alerts.model.ResolvedAlert;
import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.model.UnresolvedAlert;
import org.onebusaway.service_alerts.model.properties.AlertProperties;
import org.onebusaway.service_alerts.services.AlertDao;
import org.onebusaway.service_alerts.services.AlertDescriptionService;
import org.onebusaway.service_alerts.services.AlertResolutionService;
import org.onebusaway.service_alerts.services.AlertRemover;
import org.onebusaway.service_alerts.services.SituationService;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class AlertResolutionServiceImpl implements AlertDescriptionService,
    AlertResolutionService {

  private static Logger _log = LoggerFactory.getLogger(AlertResolutionServiceImpl.class);

  private AlertDao _dao;

  private SituationService _situationService;

  private File _path;

  @Autowired
  public void setDao(AlertDao dao) {
    _dao = dao;
  }

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
    Set<AlertProperties> unresolvedGroups = clearStaleAlerts(
        _dao.getUnresolvedAlertRemover(),
        _dao.getUnresolvedAlertsForDataSourceId(dataSourceId), time,
        new HashSet<AlertProperties>());

    /**
     * We can clear out any alerts that haven't been updated since the last
     * route if there are not unresolved alerts with the same route+region
     */
    clearStaleAlerts(_dao.getResolvedAlertRemover(),
        _dao.getResolvedAlertsForDataSourceId(dataSourceId), time,
        unresolvedGroups);
  }

  /****
   * {@link AlertResolutionService} Interface
   ****/

  @Override
  public void resolveAlertToNothing(String unresolvedAlertId) {

    UnresolvedAlert unresolvedAlert = _dao.getUnresolvedAlertForId(unresolvedAlertId);

    if (unresolvedAlert == null)
      throw new NotFoundException(UnresolvedAlert.class, unresolvedAlertId);

    SituationConfiguration situation = _situationService.getDefaultSituation();

    resolveAlertToExistingConfigurations(unresolvedAlertId,
        Arrays.asList(situation.getId()));
  }

  @Override
  public void resolveAlertToExistingAlert(String unresolvedAlertId,
      String existingResolvedAlertId) {

    UnresolvedAlert unresolvedAlert = _dao.getUnresolvedAlertForId(unresolvedAlertId);

    if (unresolvedAlert == null)
      throw new NotFoundException(UnresolvedAlert.class, unresolvedAlertId);

    ResolvedAlert resolvedAlert = _dao.getResolvedAlertForId(existingResolvedAlertId);

    if (resolvedAlert == null)
      throw new NotFoundException(ResolvedAlert.class, existingResolvedAlertId);

    _dao.removeUnresolvedAlert(unresolvedAlert);
    _dao.removeResolvedAlert(resolvedAlert);

    resolvedAlert.setKey(unresolvedAlert.getKey());

    _dao.addResolvedAlert(resolvedAlert);

    for (String configId : resolvedAlert.getConfigurationIds()) {

      SituationConfiguration config = _dao.getConfigurationForId(configId);

      if (config == null) {
        _log.warn("resolved alert " + resolvedAlert.getId()
            + " references unknown situation config " + configId);
        continue;
      }

      Set<AlertProperties> keys = config.getKeys();

      if (!keys.contains(unresolvedAlert.getKey())) {
        _dao.addKeyToConfiguration(config, unresolvedAlert.getKey());
      }
    }

    activateAlert(resolvedAlert);
  }

  @Override
  public void resolveAlertToExistingConfigurations(String unresolvedAlertId,
      List<String> alertConfigurationIds) {

    UnresolvedAlert unresolvedAlert = _dao.getUnresolvedAlertForId(unresolvedAlertId);

    if (unresolvedAlert == null)
      throw new NotFoundException(UnresolvedAlert.class, unresolvedAlertId);

    ResolvedAlert resolvedAlert = new ResolvedAlert();
    resolvedAlert.setId(unresolvedAlert.getId());
    resolvedAlert.setTimeOfCreation(unresolvedAlert.getTimeOfCreation());
    resolvedAlert.setTimeOfLastUpdate(unresolvedAlert.getTimeOfLastUpdate());
    resolvedAlert.setGroup(unresolvedAlert.getGroup());
    resolvedAlert.setKey(unresolvedAlert.getKey());
    resolvedAlert.setDataSourceId(unresolvedAlert.getDataSourceId());

    List<String> configIds = new ArrayList<String>();

    for (String alertConfigurationId : alertConfigurationIds) {
      SituationConfiguration config = _dao.getConfigurationForId(alertConfigurationId);

      if (config == null)
        throw new NotFoundException(SituationConfiguration.class,
            alertConfigurationIds);

      configIds.add(alertConfigurationId);

      Set<AlertProperties> keys = config.getKeys();

      if (!keys.contains(unresolvedAlert.getKey()))
        _dao.addKeyToConfiguration(config, unresolvedAlert.getKey());
    }

    resolvedAlert.setConfigurationIds(configIds);

    deactivatelAlert(unresolvedAlert);
    _dao.removeUnresolvedAlert(unresolvedAlert);

    _dao.addResolvedAlert(resolvedAlert);
    activateAlert(resolvedAlert);
  }

  /****
   * 
   ****/

  private void processAlertDescription(AlertDescription desc, int index,
      long time, String dataSourceId) {

    AlertProperties key = desc.getKey();

    ResolvedAlert resolvedAlert = _dao.getResolvedAlertForKey(key);

    /**
     * If we've previously seen and resolved this alert, then we set the update
     * time and move on
     */
    if (resolvedAlert != null) {
      resolvedAlert.setTimeOfLastUpdate(time);
      return;
    }

    UnresolvedAlert unresolvedAlert = _dao.getUnresolvedAlertForKey(key);

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
    Collection<SituationConfiguration> configurations = _dao.getConfigurationsForKey(key);

    if (!configurations.isEmpty()) {
      resolvedAlert = new ResolvedAlert();
      resolvedAlert.setId(id);
      resolvedAlert.setTimeOfCreation(time);
      resolvedAlert.setTimeOfLastUpdate(time);
      resolvedAlert.setGroup(desc.getGroup());
      resolvedAlert.setKey(key);
      resolvedAlert.setDataSourceId(dataSourceId);

      List<String> configIds = MappingLibrary.map(configurations, "id");
      resolvedAlert.setConfigurationIds(configIds);

      _log.debug("adding new resolved alert: {}", resolvedAlert);

      _dao.addResolvedAlert(resolvedAlert);
      activateAlert(resolvedAlert);

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
    unresolvedAlert.setKey(key);
    unresolvedAlert.setFullDescription(desc);
    unresolvedAlert.setDataSourceId(dataSourceId);

    _log.debug("adding new unresolved alert: {}", unresolvedAlert);

    _dao.addUnresolvedAlert(unresolvedAlert);
  }

  private <T extends AbstractAlert> Set<AlertProperties> clearStaleAlerts(
      AlertRemover<T> alertIndex, Collection<T> alerts, long time,
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

    for (T alert : toRemove) {
      alertIndex.removeAlert(alert);
      deactivatelAlert(alert);
    }

    return newUnresolvedGroups;
  }

  private void activateAlert(AbstractAlert alert) {
    if (alert instanceof ResolvedAlert) {
      ResolvedAlert resolvedAlert = (ResolvedAlert) alert;
      for (String configId : resolvedAlert.getConfigurationIds()) {
        _situationService.updateVisibility(configId, true);
      }
    }
  }

  private void deactivatelAlert(AbstractAlert alert) {
    if (alert instanceof ResolvedAlert) {
      ResolvedAlert resolvedAlert = (ResolvedAlert) alert;
      for (String configId : resolvedAlert.getConfigurationIds()) {
        _situationService.updateVisibility(configId, false);
      }
    }
  }
}
