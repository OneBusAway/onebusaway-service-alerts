package org.onebusaway.king_county_metro.service_alerts.model.beans;

import org.onebusaway.king_county_metro.service_alerts.model.AlertDescription;

public class UnresolvedAlertBean extends AbstractAlertBean {

  private AlertDescription fullDescription;

  public AlertDescription getFullDescription() {
    return fullDescription;
  }

  public void setFullDescription(AlertDescription fullDescription) {
    this.fullDescription = fullDescription;
  }
}
