package org.onebusaway.king_county_metro.service_alerts.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.onebusaway.king_county_metro.service_alerts.model.AbstractAlert;
import org.onebusaway.king_county_metro.service_alerts.model.AlertConfiguration;
import org.onebusaway.king_county_metro.service_alerts.model.AlertDescription;
import org.onebusaway.king_county_metro.service_alerts.model.AlertDescriptionKey;
import org.onebusaway.king_county_metro.service_alerts.model.NotFoundException;
import org.onebusaway.king_county_metro.service_alerts.model.ResolvedAlert;
import org.onebusaway.king_county_metro.service_alerts.model.RouteAndRegionRef;
import org.onebusaway.king_county_metro.service_alerts.model.UnresolvedAlert;
import org.onebusaway.king_county_metro.service_alerts.services.AlertService;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class AlertServiceImpl implements AlertService {

  private static Logger _log = LoggerFactory.getLogger(AlertServiceImpl.class);

  private static final String[] _agencyIds = {"1", "40"};

  private Map<String, String> _routeIdCache = new HashMap<String, String>();

  private AlertConfigurationIndex _configurations = new AlertConfigurationIndex();

  private AlertIndex<UnresolvedAlert> _unresolvedAlerts = new AlertIndex<UnresolvedAlert>();

  private AlertIndex<ResolvedAlert> _resolvedAlerts = new AlertIndex<ResolvedAlert>();

  private TransitDataService _transitDataService;

  private File _path;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  public void setPath(File path) {
    _path = path;
  }

  @PostConstruct
  public void setup() throws IOException, ClassNotFoundException {
    if (_path != null && _path.exists()) {
      Map<String, AlertConfiguration> configurations = ObjectSerializationLibrary.readObject(_path);
      for (AlertConfiguration configuration : configurations.values())
        _configurations.addConfiguration(configuration);
    }
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
  public Collection<ResolvedAlert> getResolvedAlertsWithRouteAndRegion(
      RouteAndRegionRef routeAndRegion) {
    return _resolvedAlerts.getAlertsForRouteAndRegion(routeAndRegion);
  }

  @Override
  public Collection<AlertConfiguration> getPotentialConfigurationsWithRouteAndRegion(
      RouteAndRegionRef ref) {
    return _configurations.getConfigurationsForRouteAndRegion(ref);
  }

  @Override
  public AlertConfiguration getAlertConfigurationForId(String id) {
    return _configurations.getConfigurationForId(id);
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

    resolvedAlert.setDescription(unresolvedAlert.getDescription());

    _resolvedAlerts.addAlert(resolvedAlert);

    for (AlertConfiguration config : resolvedAlert.getConfigurations()) {

      List<String> descriptions = config.getDescriptions();

      if (descriptions == null
          || !descriptions.contains(unresolvedAlert.getDescription())) {
        _configurations.addDescriptionToConfiguration(config,
            unresolvedAlert.getDescription());
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
    resolvedAlert.setRouteId(unresolvedAlert.getRouteId());
    resolvedAlert.setRegion(unresolvedAlert.getRegion());
    resolvedAlert.setDescription(unresolvedAlert.getDescription());

    List<AlertConfiguration> configurations = new ArrayList<AlertConfiguration>();

    for (String alertConfigurationId : alertConfigurationIds) {
      AlertConfiguration config = _configurations.getConfigurationForId(alertConfigurationId);

      if (config == null)
        throw new NotFoundException(AlertConfiguration.class,
            alertConfigurationIds);

      configurations.add(config);

      List<String> descriptions = config.getDescriptions();

      if (descriptions == null
          || !descriptions.contains(unresolvedAlert.getDescription()))
        _configurations.addDescriptionToConfiguration(config,
            unresolvedAlert.getDescription());
    }
    
    resolvedAlert.setConfigurations(configurations);
    _resolvedAlerts.addAlert(resolvedAlert);
  }

  public synchronized void setActiveAlerts(List<AlertDescription> activeAlerts) {

    long time = System.currentTimeMillis();

    int index = 0;

    for (AlertDescription alert : activeAlerts) {
      processAlertDescription(alert, index, time);
      index++;
    }

    /**
     * We can clear out any unresolved alerts that haven't been updated in this
     * round
     */
    Set<RouteAndRegionRef> unresolvedRouteAndRegionRefs = clearStaleAlerts(
        _unresolvedAlerts, time, new HashSet<RouteAndRegionRef>());

    /**
     * We can clear out any alerts that haven't been updated since the last
     * route if there are not unresolved alerts with the same route+region
     */
    clearStaleAlerts(_resolvedAlerts, time, unresolvedRouteAndRegionRefs);
  }

  /****
   * 
   ****/

  private void processAlertDescription(AlertDescription desc, int index,
      long time) {

    String routeId = resolveRouteId(desc.getRouteId());
    String region = desc.getRegion();
    String description = desc.getDescription();
    AlertDescriptionKey key = new AlertDescriptionKey(routeId, region,
        description);

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
    AlertConfiguration configuration = _configurations.getConfigurationForKey(key);

    if (configuration != null) {
      resolvedAlert = new ResolvedAlert();
      resolvedAlert.setId(id);
      resolvedAlert.setTimeOfCreation(time);
      resolvedAlert.setTimeOfLastUpdate(time);
      resolvedAlert.setRouteId(routeId);
      resolvedAlert.setRegion(region);
      resolvedAlert.setDescription(description);

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
    unresolvedAlert.setRouteId(routeId);
    unresolvedAlert.setRegion(region);
    unresolvedAlert.setDescription(description);
    unresolvedAlert.setFullDescription(desc);

    _log.debug("adding new unresolved alert: {}", unresolvedAlert);

    _unresolvedAlerts.addAlert(unresolvedAlert);
  }

  private String resolveRouteId(String routeId) {

    if (_routeIdCache.containsKey(routeId))
      return _routeIdCache.get(routeId);

    for (String agencyId : _agencyIds) {
      String fullRouteId = agencyId + "_" + routeId;
      RouteBean route = _transitDataService.getRouteForId(fullRouteId);
      if (route != null) {
        _routeIdCache.put(routeId, fullRouteId);
        return fullRouteId;
      }
    }

    return null;
  }

  private <T extends AbstractAlert> Set<RouteAndRegionRef> clearStaleAlerts(
      AlertIndex<T> alerts, long time,
      Set<RouteAndRegionRef> unresolvedRouteAndRegionRefs) {

    Set<RouteAndRegionRef> newUnresolvedRouteAndRegionRefs = new HashSet<RouteAndRegionRef>();
    List<T> toRemove = new ArrayList<T>();

    for (T alert : alerts.getAlerts()) {

      AlertDescriptionKey key = alert.getKey();
      RouteAndRegionRef ref = key.getRouteAndRegion();

      if (alert.getTimeOfLastUpdate() < time
          && !unresolvedRouteAndRegionRefs.contains(ref)) {
        _log.debug("expiring alert: {}", alert);
        toRemove.add(alert);
      } else {
        newUnresolvedRouteAndRegionRefs.add(ref);
      }
    }

    for (T alert : toRemove)
      alerts.removeAlert(alert);

    return newUnresolvedRouteAndRegionRefs;
  }
}
