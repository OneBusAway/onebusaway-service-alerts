package org.onebusaway.service_alerts.model;

public final class UnresolvedAlert extends AbstractAlert {

  private static final long serialVersionUID = 1L;
  
  private AlertDescription fullDescription;

  public AlertDescription getFullDescription() {
    return fullDescription;
  }

  public void setFullDescription(AlertDescription fullDescription) {
    this.fullDescription = fullDescription;
  }
}
