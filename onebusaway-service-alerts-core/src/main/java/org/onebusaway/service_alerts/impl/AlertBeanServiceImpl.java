package org.onebusaway.service_alerts.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.onebusaway.service_alerts.model.AbstractAlert;
import org.onebusaway.service_alerts.model.ResolvedAlert;
import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.model.UnresolvedAlert;
import org.onebusaway.service_alerts.model.beans.AbstractAlertBean;
import org.onebusaway.service_alerts.model.beans.ResolvedAlertBean;
import org.onebusaway.service_alerts.model.beans.UnresolvedAlertBean;
import org.onebusaway.service_alerts.model.properties.AlertProperties;
import org.onebusaway.service_alerts.services.AlertBeanService;
import org.onebusaway.service_alerts.services.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class AlertBeanServiceImpl implements AlertBeanService {

  private AlertService _alertService;

  @Autowired
  public void setAlertService(AlertService alertService) {
    _alertService = alertService;
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
    return resolvedAlertBeans(alerts);
  }

  @Override
  public List<ResolvedAlertBean> getResolvedAlertsWithGroup(
      AlertProperties group) {
    Collection<ResolvedAlert> alerts = _alertService.getResolvedAlertsWithGroup(group);
    return resolvedAlertBeans(alerts);
  }

  @Override
  public List<ResolvedAlertBean> getResolvedAlertsForSituationConfigurationId(
      String id) {
    Collection<ResolvedAlert> alerts = _alertService.getResolvedAlertsForSituationConfigurationId(id);
    return resolvedAlertBeans(alerts);
  }

  @Override
  public ResolvedAlertBean getResolvedAlertForId(String id) {
    return getResolvedAlertAsBean(_alertService.getResolvedAlertForId(id));
  }

  @Override
  public List<SituationConfiguration> getPotentialConfigurationsWithGroup(
      AlertProperties group) {
    Collection<SituationConfiguration> configs = _alertService.getPotentialConfigurationsWithGroup(group);
    return new ArrayList<SituationConfiguration>(configs);
  }

  @Override
  public SituationConfiguration getSituationConfigurationForId(String id) {
    return _alertService.getSituationConfigurationForId(id);
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

  private List<ResolvedAlertBean> resolvedAlertBeans(
      Collection<ResolvedAlert> alerts) {
    List<ResolvedAlertBean> beans = new ArrayList<ResolvedAlertBean>();
    for (ResolvedAlert alert : alerts)
      beans.add(getResolvedAlertAsBean(alert));
    return beans;
  }

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

    List<SituationConfiguration> beans = new ArrayList<SituationConfiguration>();
    for (SituationConfiguration config : alert.getConfigurations()) {
      beans.add(config);
    }
    bean.setConfigurations(beans);

    return bean;
  }

  private void populateAlertBean(AbstractAlert alert, AbstractAlertBean bean) {
    bean.setId(alert.getId());
    bean.setGroup(alert.getGroup());
    bean.setKey(alert.getKey());
    bean.setTimeOfCreation(alert.getTimeOfCreation());
    bean.setTimeOfLastUpdate(alert.getTimeOfLastUpdate());
  }
}
