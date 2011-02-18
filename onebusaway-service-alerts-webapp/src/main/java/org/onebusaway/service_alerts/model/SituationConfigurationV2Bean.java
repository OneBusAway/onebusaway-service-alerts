package org.onebusaway.service_alerts.model;

import org.onebusaway.api.model.transit.service_alerts.SituationV2Bean;

public class SituationConfigurationV2Bean {
  private String id;

  private boolean visible;

  private SituationV2Bean situation;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public SituationV2Bean getSituation() {
    return situation;
  }

  public void setSituation(SituationV2Bean situation) {
    this.situation = situation;
  }
}
