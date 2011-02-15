package org.onebusaway.service_alerts.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.onebusaway.service_alerts.model.AbstractAlert;
import org.onebusaway.service_alerts.model.AlertDescriptionKey;
import org.onebusaway.service_alerts.model.RouteAndRegionRef;

public class AlertIndex<T extends AbstractAlert> {

  private Map<AlertDescriptionKey, T> _alerts = new HashMap<AlertDescriptionKey, T>();

  private Map<String, T> _alertsById = new HashMap<String, T>();

  private Map<RouteAndRegionRef, Set<T>> _alertsByRouteAndRegion = new HashMap<RouteAndRegionRef, Set<T>>();

  public void addAlert(T alert) {

    AlertDescriptionKey key = alert.getKey();
    T existingAlert = _alerts.put(key, alert);

    Set<T> set = _alertsByRouteAndRegion.get(key.getRouteAndRegion());

    if (set == null) {
      set = new HashSet<T>();
      _alertsByRouteAndRegion.put(key.getRouteAndRegion(), set);
    }

    if (existingAlert != null) {
      set.remove(existingAlert);
      _alertsById.remove(existingAlert.getId());
    }

    set.add(alert);
    _alertsById.put(alert.getId(), alert);
  }

  public void removeAlert(T alert) {

    AlertDescriptionKey key = alert.getKey();
    T existingAlert = _alerts.remove(key);

    if (existingAlert == null)
      return;

    _alertsById.remove(existingAlert.getId());

    Set<T> set = _alertsByRouteAndRegion.get(key.getRouteAndRegion());

    if (set != null) {
      set.remove(existingAlert);
    }
  }

  public Collection<T> getAlerts() {
    return _alerts.values();
  }

  public T getAlertForId(String id) {
    return _alertsById.get(id);
  }

  public T getAlertForKey(AlertDescriptionKey key) {
    return _alerts.get(key);
  }

  public Collection<T> getAlertsForRouteAndRegion(
      RouteAndRegionRef routeAndRegion) {
    Set<T> set = _alertsByRouteAndRegion.get(routeAndRegion);
    if (set == null)
      return Collections.emptySet();
    return set;
  }

}
