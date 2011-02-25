package org.onebusaway.service_alerts.services;

import org.onebusaway.service_alerts.model.AbstractAlert;

public interface AlertRemover<T extends AbstractAlert> {
  public void removeAlert(T alert);
}
