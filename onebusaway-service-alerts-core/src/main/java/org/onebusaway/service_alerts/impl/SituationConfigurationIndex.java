package org.onebusaway.service_alerts.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.model.properties.AlertProperties;

public class SituationConfigurationIndex {

  private Map<String, SituationConfiguration> _configurationsById = new HashMap<String, SituationConfiguration>();

  private Map<AlertProperties, Set<SituationConfiguration>> _configurationsByGroup = new HashMap<AlertProperties, Set<SituationConfiguration>>();

  private Map<AlertProperties, SituationConfiguration> _configurationsByKey = new HashMap<AlertProperties, SituationConfiguration>();

  public void addConfiguration(SituationConfiguration configuration) {

    String id = configuration.getId();
    AlertProperties group = configuration.getGroup();

    SituationConfiguration existing = _configurationsById.put(id, configuration);

    if (existing != null)
      removeConfigurationInternal(existing);

    Set<SituationConfiguration> set = _configurationsByGroup.get(group);

    if (set == null) {
      set = new HashSet<SituationConfiguration>();
      _configurationsByGroup.put(group, set);
    }

    set.add(configuration);

    for (AlertProperties key : configuration.getKeys()) {
      _configurationsByKey.put(key, configuration);
    }
  }

  public void addKeyToConfiguration(SituationConfiguration config,
      AlertProperties key) {

    Set<AlertProperties> keys = config.getKeys();

    if (keys.contains(key))
      return;

    keys.add(key);

    removeConfiguration(config);
    addConfiguration(config);
  }

  public void removeConfiguration(SituationConfiguration configuration) {

    SituationConfiguration existing = _configurationsById.remove(configuration.getId());

    if (existing == null)
      return;

    removeConfigurationInternal(existing);
  }

  public Collection<SituationConfiguration> getConfigurations() {
    return _configurationsById.values();
  }

  public SituationConfiguration getConfigurationForId(String id) {
    return _configurationsById.get(id);
  }

  public Collection<SituationConfiguration> getConfigurationsForGroup(
      AlertProperties group) {
    Set<SituationConfiguration> set = _configurationsByGroup.get(group);
    if (set == null)
      return Collections.emptySet();
    return set;
  }

  public SituationConfiguration getConfigurationForKey(AlertProperties key) {
    return _configurationsByKey.get(key);
  }

  /****
   * Private Methods
   *****/

  private void removeConfigurationInternal(SituationConfiguration existing) {

    AlertProperties group = existing.getGroup();

    Set<SituationConfiguration> set = _configurationsByGroup.get(group);

    if (set != null) {
      set.remove(existing);
    }

    for (AlertProperties key : existing.getKeys()) {
      _configurationsByKey.remove(key);
    }
  }

}
