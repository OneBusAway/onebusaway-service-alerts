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

  @Override
  public void addAlert(ResolvedAlert alert) {
    super.addAlert(alert);

    for (SituationConfiguration config : alert.getConfigurations()) {
      Set<ResolvedAlert> alerts = _alertsBySituationConfigurationId.get(config.getId());
      if (alerts == null) {
        alerts = new HashSet<ResolvedAlert>();
        _alertsBySituationConfigurationId.put(config.getId(), alerts);
      }

      alerts.add(alert);
    }
  }

  @Override
  public void removeAlert(ResolvedAlert alert) {
    super.removeAlert(alert);

    for (SituationConfiguration config : alert.getConfigurations()) {
      Set<ResolvedAlert> alerts = _alertsBySituationConfigurationId.get(config.getId());
      if (alerts != null)
        alerts.remove(alert);
    }
  }

  public Collection<ResolvedAlert> getAlertsForSituationConfigurationId(
      String id) {
    Set<ResolvedAlert> alerts = _alertsBySituationConfigurationId.get(id);
    if (alerts == null)
      alerts = Collections.emptySet();
    return alerts;
  }

}
