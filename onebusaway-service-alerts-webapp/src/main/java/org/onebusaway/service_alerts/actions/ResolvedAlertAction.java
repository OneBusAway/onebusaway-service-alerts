package org.onebusaway.service_alerts.actions;

import org.onebusaway.service_alerts.model.beans.ResolvedAlertBean;
import org.onebusaway.service_alerts.services.AlertBeanService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

public class ResolvedAlertAction extends ActionSupport implements
    ModelDriven<ResolvedAlertBean> {

  private static final long serialVersionUID = 1L;

  private AlertBeanService _alertService;

  private String _id;

  private ResolvedAlertBean _model;

  @Autowired
  public void setAlertService(AlertBeanService alertService) {
    _alertService = alertService;
  }

  @RequiredStringValidator
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  @Override
  public ResolvedAlertBean getModel() {
    return _model;
  }

  @Override
  public String execute() {

    _model = _alertService.getResolvedAlertForId(_id);

    if (_model == null)
      return ERROR;

    return SUCCESS;
  }

}
