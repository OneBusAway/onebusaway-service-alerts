package org.onebusaway.service_alerts.actions;

import java.util.List;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.model.beans.ResolvedAlertBean;
import org.onebusaway.service_alerts.model.beans.UnresolvedAlertBean;
import org.onebusaway.service_alerts.model.properties.AlertProperties;
import org.onebusaway.service_alerts.services.AlertBeanService;
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

  private String _id;

  private String _resolvedAlertId;

  private List<String> _configurationIds;

  private UnresolvedAlertBean _model;

  private List<ResolvedAlertBean> _resolvedAlerts;

  private List<SituationConfiguration> _potentialConfigurations;

  @Autowired
  public void setAlertService(AlertBeanService alertService) {
    _alertService = alertService;
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

  public List<SituationConfiguration> getPotentialConfigurations() {
    return _potentialConfigurations;
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "id")})
  @Override
  public String execute() {

    _model = _alertService.getUnresolvedAlertForId(_id);

    if (_model == null)
      return ERROR;

    AlertProperties group = _model.getGroup();

    _resolvedAlerts = _alertService.getResolvedAlertsWithGroup(group);
    _potentialConfigurations = _alertService.getPotentialConfigurationsWithGroup(group);

    return SUCCESS;
  }

  @Validations(requiredStrings = {
      @RequiredStringValidator(fieldName = "id"),
      @RequiredStringValidator(fieldName = "resolvedAlertId")})
  public String resolveToExistingAlert() {
    _alertService.resolveAlertToExistingAlert(_id, _resolvedAlertId);
    return "redirect";
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "id")})
  public String resolveToEmptyAlert() {
    _alertService.resolveAlertToNothing(_id);
    return "redirect";
  }

  @Validations(requiredFields = {@RequiredFieldValidator(fieldName = "configurationIds")}, requiredStrings = {@RequiredStringValidator(fieldName = "id")})
  public String resolveToConfigurations() {
    _alertService.resolveAlertToExistingConfiguration(_id, _configurationIds);
    return "redirect";
  }
}
