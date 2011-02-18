package org.onebusaway.service_alerts.model;

import org.onebusaway.transit_data.model.service_alerts.SituationBean;

public class SituationConfiguration {

  private String id;

  private long lastUpdate;

  private boolean visible;

  private SituationBean situation;

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
}
