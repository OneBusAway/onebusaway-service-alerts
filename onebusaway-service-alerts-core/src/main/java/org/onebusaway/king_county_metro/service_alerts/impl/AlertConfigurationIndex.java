package org.onebusaway.king_county_metro.service_alerts.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.king_county_metro.service_alerts.model.AlertConfiguration;
import org.onebusaway.king_county_metro.service_alerts.model.AlertDescriptionKey;
import org.onebusaway.king_county_metro.service_alerts.model.RouteAndRegionRef;

public class AlertConfigurationIndex {

  private Map<String, AlertConfiguration> _configurationsById = new HashMap<String, AlertConfiguration>();

  private Map<RouteAndRegionRef, Set<AlertConfiguration>> _configurationsByRouteAndRegion = new HashMap<RouteAndRegionRef, Set<AlertConfiguration>>();

  private Map<AlertDescriptionKey, AlertConfiguration> _configurationsByKey = new HashMap<AlertDescriptionKey, AlertConfiguration>();

  public void addConfiguration(AlertConfiguration configuration) {

    String id = configuration.getId();
    RouteAndRegionRef rar = configuration.getRouteAndRegion();

    AlertConfiguration existing = _configurationsById.put(id, configuration);

    if (existing != null)
      removeConfigurationInternal(existing);

    Set<AlertConfiguration> set = _configurationsByRouteAndRegion.get(rar);

    if (set == null) {
      set = new HashSet<AlertConfiguration>();
      _configurationsByRouteAndRegion.put(rar, set);
    }

    set.add(configuration);

    List<String> descriptions = configuration.getDescriptions();
    if (descriptions != null) {
      for (String desc : descriptions) {
        AlertDescriptionKey key = new AlertDescriptionKey(rar, desc);
        _configurationsByKey.put(key, configuration);
      }
    }
  }

  public void addDescriptionToConfiguration(AlertConfiguration config,
      String description) {

    List<String> descs = config.getDescriptions();
    if (descs == null) {
      descs = new ArrayList<String>();
      config.setDescriptions(descs);
    }

    if (descs.contains(description))
      return;

    descs.add(description);

    Collections.sort(descs);

    removeConfiguration(config);
    addConfiguration(config);
  }

  public void removeConfiguration(AlertConfiguration configuration) {

    AlertConfiguration existing = _configurationsById.remove(configuration.getId());

    if (existing == null)
      return;

    removeConfigurationInternal(existing);
  }

  public Collection<AlertConfiguration> getConfigurations() {
    return _configurationsById.values();
  }

  public AlertConfiguration getConfigurationForId(String id) {
    return _configurationsById.get(id);
  }

  public Collection<AlertConfiguration> getConfigurationsForRouteAndRegion(
      RouteAndRegionRef routeAndRegion) {
    Set<AlertConfiguration> set = _configurationsByRouteAndRegion.get(routeAndRegion);
    if (set == null)
      return Collections.emptySet();
    return set;
  }

  public AlertConfiguration getConfigurationForKey(AlertDescriptionKey key) {
    return _configurationsByKey.get(key);
  }

  /****
   * Private Methods
   *****/

  private void removeConfigurationInternal(AlertConfiguration existing) {

    RouteAndRegionRef rar = existing.getRouteAndRegion();

    Set<AlertConfiguration> set = _configurationsByRouteAndRegion.get(rar);

    if (set != null) {
      set.remove(existing);
    }

    List<String> descriptions = existing.getDescriptions();
    if (descriptions != null) {
      for (String desc : descriptions) {
        AlertDescriptionKey key = new AlertDescriptionKey(rar, desc);
        _configurationsByKey.remove(key);
      }
    }
  }

}
