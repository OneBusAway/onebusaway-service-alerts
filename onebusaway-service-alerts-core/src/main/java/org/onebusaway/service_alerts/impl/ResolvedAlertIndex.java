package org.onebusaway.service_alerts.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.onebusaway.service_alerts.model.ResolvedAlert;
import org.onebusaway.service_alerts.model.SituationConfiguration;

public class ResolvedAlertIndex extends AlertIndex<ResolvedAlert> {

  private Map<String, Set<ResolvedAlert>> _alertsBySituationConfigurationId = new HashMap<String, Set<ResolvedAlert>>();

  public Collection<ResolvedAlert> getAlertsForSituationConfigurationId(
      String id) {
    Set<ResolvedAlert> alerts = _alertsBySituationConfigurationId.get(id);
    if (alerts == null)
      alerts = Collections.emptySet();
    return alerts;
  }

  public void removeConfigurationFromAlert(SituationConfiguration config,
      ResolvedAlert resolvedAlert) {
    Set<ResolvedAlert> resolvedAlerts = _alertsBySituationConfigurationId.get(config.getId());
    if (resolvedAlerts != null)
      resolvedAlerts.remove(resolvedAlert);
  }

  @Override
  public void addAlert(ResolvedAlert alert) {
    super.addAlert(alert);

    for (String configId : alert.getConfigurationIds()) {
      Set<ResolvedAlert> alerts = _alertsBySituationConfigurationId.get(configId);
      if (alerts == null) {
        alerts = new HashSet<ResolvedAlert>();
        _alertsBySituationConfigurationId.put(configId, alerts);
      }

      alerts.add(alert);
    }
  }

  @Override
  public void removeAlert(ResolvedAlert alert) {
    super.removeAlert(alert);

    for (String configId : alert.getConfigurationIds()) {
      Set<ResolvedAlert> alerts = _alertsBySituationConfigurationId.get(configId);
      if (alerts != null)
        alerts.remove(alert);
    }
  }

}
