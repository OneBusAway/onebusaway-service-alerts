package org.onebusaway.service_alerts.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.onebusaway.service_alerts.model.properties.AlertProperties;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;

public class SituationConfiguration implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private long lastUpdate;

  private boolean visible;

  private SituationBean situation;

  private AlertProperties group;

  private Set<AlertProperties> keys = new HashSet<AlertProperties>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public long getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(long lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public SituationBean getSituation() {
    return situation;
  }

  public void setSituation(SituationBean situation) {
    this.situation = situation;
  }

  public AlertProperties getGroup() {
    return group;
  }

  public void setGroup(AlertProperties group) {
    this.group = group;
  }

  public Set<AlertProperties> getKeys() {
    return keys;
  }

  public void setKeys(Set<AlertProperties> keys) {
    this.keys = keys;
  }
}
