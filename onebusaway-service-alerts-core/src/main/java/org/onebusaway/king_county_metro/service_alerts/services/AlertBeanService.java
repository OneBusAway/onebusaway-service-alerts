package org.onebusaway.king_county_metro.service_alerts.services;

import java.util.List;

import org.onebusaway.king_county_metro.service_alerts.model.RouteAndRegionRef;
import org.onebusaway.king_county_metro.service_alerts.model.beans.AlertConfigurationBean;
import org.onebusaway.king_county_metro.service_alerts.model.beans.ResolvedAlertBean;
import org.onebusaway.king_county_metro.service_alerts.model.beans.UnresolvedAlertBean;

public interface AlertBeanService {

  public List<UnresolvedAlertBean> getUnresolvedAlerts();

  public UnresolvedAlertBean getUnresolvedAlertForId(String id);

  public List<ResolvedAlertBean> getResolvedAlerts();

  public List<ResolvedAlertBean> getResolvedAlertsWithRouteAndRegion(
      RouteAndRegionRef routeAndRegion);

  public ResolvedAlertBean getResolvedAlertForId(String id);

  public List<AlertConfigurationBean> getPotentialConfigurationsWithRouteAndRegion(
      RouteAndRegionRef ref);

  public AlertConfigurationBean getAlertConfigurationForId(
      String id);

  public void resolveAlertToExistingAlert(String unresolvedAlertId,
      String existingResolvedAlertId);

  public void resolveAlertToExistingConfiguration(String unresolvedAlertId,
      List<String> alertConfigurationIds);
}
