package org.onebusaway.service_alerts.actions;

import java.util.List;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.service_alerts.model.RouteAndRegionRef;
import org.onebusaway.service_alerts.model.beans.AlertConfigurationBean;
import org.onebusaway.service_alerts.model.beans.ResolvedAlertBean;
import org.onebusaway.service_alerts.model.beans.UnresolvedAlertBean;
import org.onebusaway.service_alerts.services.AlertBeanService;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;

@Results({@Result(type = "redirectAction", name = "redirect", params = {
    "actionName", "resolved-alert", "id", "${id}", "parse", "true"})})
public class UnresolvedAlertAction extends ActionSupport implements
    ModelDriven<UnresolvedAlertBean> {

  private static final long serialVersionUID = 1L;

  private AlertBeanService _alertService;

  private TransitDataService _transitDataService;

  private String _id;

  private String _resolvedAlertId;

  private List<String> _configurationIds;

  private UnresolvedAlertBean _model;

  private List<ResolvedAlertBean> _resolvedAlerts;

  private List<AlertConfigurationBean> _potentialConfigurations;

  private StopsForRouteBean _stopsForRoute;

  @Autowired
  public void setAlertService(AlertBeanService alertService) {
    _alertService = alertService;
  }

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public void setResolvedAlertId(String resolvedAlertId) {
    _resolvedAlertId = resolvedAlertId;
  }

  public String getResolvedAlertId() {
    return _resolvedAlertId;
  }

  public void setConfigurationIds(List<String> configurationIds) {
    _configurationIds = configurationIds;
  }

  public List<String> getConfigurationIds() {
    return _configurationIds;
  }

  @Override
  public UnresolvedAlertBean getModel() {
    return _model;
  }

  public List<ResolvedAlertBean> getResolvedAlerts() {
    return _resolvedAlerts;
  }

  public List<AlertConfigurationBean> getPotentialConfigurations() {
    return _potentialConfigurations;
  }

  public StopsForRouteBean getStopsForRoute() {
    return _stopsForRoute;
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "id")})
  @Override
  public String execute() {

    _model = _alertService.getUnresolvedAlertForId(_id);

    if (_model == null)
      return ERROR;

    RouteAndRegionRef rar = _model.getAsRouteAndRegion();

    _resolvedAlerts = _alertService.getResolvedAlertsWithRouteAndRegion(rar);
    _potentialConfigurations = _alertService.getPotentialConfigurationsWithRouteAndRegion(rar);
    _stopsForRoute = _transitDataService.getStopsForRoute(rar.getRouteId());

    return SUCCESS;
  }

  @Validations(requiredStrings = {
      @RequiredStringValidator(fieldName = "id"),
      @RequiredStringValidator(fieldName = "resolvedAlertId")})
  public String resolveToExistingAlert() {
    _alertService.resolveAlertToExistingAlert(_id, _resolvedAlertId);
    return "redirect";
  }

  @Validations(requiredFields = {@RequiredFieldValidator(fieldName = "configurationIds")}, requiredStrings = {@RequiredStringValidator(fieldName = "id")})
  public String resolveToConfigurations() {
    _alertService.resolveAlertToExistingConfiguration(_id, _configurationIds);
    return "redirect";
  }
}
