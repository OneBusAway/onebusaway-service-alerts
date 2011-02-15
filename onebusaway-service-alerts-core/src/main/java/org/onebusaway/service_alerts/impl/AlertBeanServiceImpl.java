package org.onebusaway.service_alerts.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.onebusaway.service_alerts.model.AbstractAlert;
import org.onebusaway.service_alerts.model.AlertConfiguration;
import org.onebusaway.service_alerts.model.ResolvedAlert;
import org.onebusaway.service_alerts.model.RouteAndRegionRef;
import org.onebusaway.service_alerts.model.UnresolvedAlert;
import org.onebusaway.service_alerts.model.beans.AbstractAlertBean;
import org.onebusaway.service_alerts.model.beans.AlertConfigurationBean;
import org.onebusaway.service_alerts.model.beans.ResolvedAlertBean;
import org.onebusaway.service_alerts.model.beans.UnresolvedAlertBean;
import org.onebusaway.service_alerts.services.AlertBeanService;
import org.onebusaway.service_alerts.services.AlertService;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class AlertBeanServiceImpl implements AlertBeanService {

  private AlertService _alertService;

  private TransitDataService _transitDataService;

  @Autowired
  public void setAlertService(AlertService alertService) {
    _alertService = alertService;
  }

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Override
  public List<UnresolvedAlertBean> getUnresolvedAlerts() {
    List<UnresolvedAlert> alerts = _alertService.getUnresolvedAlerts();
    List<UnresolvedAlertBean> beans = new ArrayList<UnresolvedAlertBean>();
    for (UnresolvedAlert alert : alerts)
      beans.add(getUnresolvedAlertAsBean(alert));
    return beans;
  }

  @Override
  public UnresolvedAlertBean getUnresolvedAlertForId(String id) {
    return getUnresolvedAlertAsBean(_alertService.getUnresolvedAlertForId(id));
  }

  @Override
  public List<ResolvedAlertBean> getResolvedAlerts() {
    Collection<ResolvedAlert> alerts = _alertService.getResolvedAlerts();
    List<ResolvedAlertBean> beans = new ArrayList<ResolvedAlertBean>();
    for (ResolvedAlert alert : alerts)
      beans.add(getResolvedAlertAsBean(alert));
    return beans;
  }

  @Override
  public List<ResolvedAlertBean> getResolvedAlertsWithRouteAndRegion(
      RouteAndRegionRef routeAndRegion) {
    Collection<ResolvedAlert> alerts = _alertService.getResolvedAlertsWithRouteAndRegion(routeAndRegion);
    List<ResolvedAlertBean> beans = new ArrayList<ResolvedAlertBean>();
    for (ResolvedAlert alert : alerts)
      beans.add(getResolvedAlertAsBean(alert));
    return beans;
  }

  @Override
  public ResolvedAlertBean getResolvedAlertForId(String id) {
    return getResolvedAlertAsBean(_alertService.getResolvedAlertForId(id));
  }

  @Override
  public List<AlertConfigurationBean> getPotentialConfigurationsWithRouteAndRegion(
      RouteAndRegionRef ref) {
    Collection<AlertConfiguration> configs = _alertService.getPotentialConfigurationsWithRouteAndRegion(ref);
    List<AlertConfigurationBean> beans = new ArrayList<AlertConfigurationBean>();
    for (AlertConfiguration config : configs)
      beans.add(getAlertConfigurationAsBean(config));
    return beans;
  }

  @Override
  public AlertConfigurationBean getAlertConfigurationForId(String id) {
    AlertConfiguration config = _alertService.getAlertConfigurationForId(id);
    return getAlertConfigurationAsBean(config);
  }

  @Override
  public void resolveAlertToExistingAlert(String unresolvedAlertId,
      String existingResolvedAlertId) {
    _alertService.resolveAlertToExistingAlert(unresolvedAlertId,
        existingResolvedAlertId);
  }

  @Override
  public void resolveAlertToExistingConfiguration(String unresolvedAlertId,
      List<String> alertConfigurationIds) {
    _alertService.resolveAlertToExistingConfigurations(unresolvedAlertId,
        alertConfigurationIds);
  }

  /****
   * 
   ****/

  private UnresolvedAlertBean getUnresolvedAlertAsBean(UnresolvedAlert alert) {

    if (alert == null)
      return null;

    UnresolvedAlertBean bean = new UnresolvedAlertBean();
    populateAlertBean(alert, bean);
    bean.setFullDescription(alert.getFullDescription());

    return bean;
  }

  private ResolvedAlertBean getResolvedAlertAsBean(ResolvedAlert alert) {

    if (alert == null)
      return null;

    ResolvedAlertBean bean = new ResolvedAlertBean();
    populateAlertBean(alert, bean);

    List<AlertConfigurationBean> beans = new ArrayList<AlertConfigurationBean>();
    for (AlertConfiguration config : alert.getConfigurations()) {
      beans.add(getAlertConfigurationAsBean(config));
    }
    bean.setConfigurations(beans);

    return bean;
  }

  private void populateAlertBean(AbstractAlert alert, AbstractAlertBean bean) {
    bean.setId(alert.getId());
    bean.setRegion(alert.getRegion());
    bean.setDescription(alert.getDescription());
    bean.setTimeOfCreation(alert.getTimeOfCreation());
    bean.setTimeOfLastUpdate(alert.getTimeOfLastUpdate());

    if( alert.getRouteId() != null ) {
      RouteBean route = _transitDataService.getRouteForId(alert.getRouteId());
      bean.setRoute(route);
    }
  }

  private AlertConfigurationBean getAlertConfigurationAsBean(
      AlertConfiguration config) {

    if (config == null)
      return null;

    AlertConfigurationBean bean = new AlertConfigurationBean();

    bean.setDescriptions(config.getDescriptions());
    bean.setDirectionId(config.getDirectionId());
    bean.setId(config.getId());
    bean.setRegion(config.getRegion());
    bean.setReroteAsPolylineString(config.getReroteAsPolylineString());
    bean.setReroute(config.getReroute());

    if( config.getRouteId() != null) {
      RouteBean route = _transitDataService.getRouteForId(config.getRouteId());
      bean.setRoute(route);
    }

    bean.setStopIds(config.getStopIds());
    bean.setType(config.getType());

    return bean;
  }
}
