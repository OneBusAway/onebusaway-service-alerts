package org.onebusaway.king_county_metro.service_alerts.services;

import java.util.Collection;
import java.util.List;

import org.onebusaway.king_county_metro.service_alerts.model.AlertConfiguration;
import org.onebusaway.king_county_metro.service_alerts.model.AlertDescription;
import org.onebusaway.king_county_metro.service_alerts.model.ResolvedAlert;
import org.onebusaway.king_county_metro.service_alerts.model.RouteAndRegionRef;
import org.onebusaway.king_county_metro.service_alerts.model.UnresolvedAlert;

public interface AlertService {

  public List<UnresolvedAlert> getUnresolvedAlerts();

  public UnresolvedAlert getUnresolvedAlertForId(String id);

  public ResolvedAlert getResolvedAlertForId(String id);

  public Collection<ResolvedAlert> getResolvedAlerts();

  public Collection<ResolvedAlert> getResolvedAlertsWithRouteAndRegion(
      RouteAndRegionRef routeAndRegion);

  public Collection<AlertConfiguration> getPotentialConfigurationsWithRouteAndRegion(
      RouteAndRegionRef ref);

  public AlertConfiguration getAlertConfigurationForId(String id);

  public void resolveAlertToExistingAlert(String unresolvedAlertId,
      String existingResolvedAlertId);

  public void resolveAlertToExistingConfigurations(String unresolvedAlertId,
      List<String> alertConfigurationIds);

  public void setActiveAlerts(List<AlertDescription> activeAlerts);

}
