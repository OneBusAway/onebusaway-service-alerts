package org.onebusaway.service_alerts.actions;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.service_alerts.model.beans.AlertConfigurationBean;
import org.onebusaway.service_alerts.services.AlertBeanService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;

@Results({@Result(type = "json", name = "json", params = {
    "root", "model", "contentType", "text/javascript"})})
public class AlertConfigurationAction extends ActionSupport implements
    ModelDriven<AlertConfigurationBean> {

  private static final long serialVersionUID = 1L;

  private AlertBeanService _alertService;

  private String _id;

  private AlertConfigurationBean _model;

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

  @Override
  public AlertConfigurationBean getModel() {
    return _model;
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "id")})
  @Override
  public String execute() {

    _model = _alertService.getAlertConfigurationForId(_id);

    if (_model == null)
      return ERROR;

    return SUCCESS;
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "id")})
  public String json() {

    String result = execute();
    if (!SUCCESS.equals(result))
      return result;

    return "json";
  }
}
