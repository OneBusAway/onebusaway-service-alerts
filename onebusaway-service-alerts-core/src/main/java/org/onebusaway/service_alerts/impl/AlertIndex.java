package org.onebusaway.service_alerts.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.onebusaway.service_alerts.model.AbstractAlert;
import org.onebusaway.service_alerts.model.properties.AlertProperties;

public class AlertIndex<T extends AbstractAlert> {

  private Map<AlertProperties, T> _alerts = new HashMap<AlertProperties, T>();

  private Map<String, T> _alertsById = new HashMap<String, T>();

  private Map<AlertProperties, Set<T>> _alertsByGroup = new HashMap<AlertProperties, Set<T>>();

  private Map<String, Set<T>> _alertsByDataSourceId = new HashMap<String, Set<T>>();

  public void addAlert(T alert) {

    AlertProperties key = alert.getKey();
    AlertProperties group = alert.getGroup();
    T existingAlert = _alerts.put(key, alert);

    _alertsById.put(alert.getId(), alert);

    Set<T> set = _alertsByGroup.get(group);

    if (set == null) {
      set = new HashSet<T>();
      _alertsByGroup.put(group, set);
    }

    if (existingAlert != null) {
      set.remove(existingAlert);
      _alertsById.remove(existingAlert.getId());
    }

    set.add(alert);

    /****
     * Alerts by Data Source Id
     ****/

    Set<T> alertsForDataSourceId = _alertsByDataSourceId.get(alert.getDataSourceId());
    if (alertsForDataSourceId == null) {
      alertsForDataSourceId = new HashSet<T>();
      _alertsByDataSourceId.put(alert.getDataSourceId(), alertsForDataSourceId);
    }
    alertsForDataSourceId.add(alert);
  }

  public void removeAlert(T alert) {

    AlertProperties key = alert.getKey();
    AlertProperties group = alert.getGroup();
    T existingAlert = _alerts.remove(key);

    if (existingAlert == null)
      return;

    _alertsById.remove(existingAlert.getId());

    Set<T> set = _alertsByGroup.get(group);

    if (set != null) {
      set.remove(existingAlert);
    }

    /****
     * Alerts by Data Source Id
     ****/

    Set<T> alertsForDataSourceId = _alertsByDataSourceId.get(alert.getDataSourceId());
    if (alertsForDataSourceId != null)
      alertsForDataSourceId.remove(alert);
  }

  public Collection<T> getAlerts() {
    return _alerts.values();
  }

  public T getAlertForId(String id) {
    return _alertsById.get(id);
  }

  public T getAlertForKey(AlertProperties key) {
    return _alerts.get(key);
  }

  public Collection<T> getAlertsForGroup(AlertProperties group) {
    Set<T> set = _alertsByGroup.get(group);
    if (set == null)
      return Collections.emptySet();
    return set;
  }

  public Collection<T> getAlertsForDataSourceId(String dataSourceId) {
    Set<T> set = _alertsByDataSourceId.get(dataSourceId);
    if (set == null)
      return Collections.emptySet();
    return set;
  }
}
