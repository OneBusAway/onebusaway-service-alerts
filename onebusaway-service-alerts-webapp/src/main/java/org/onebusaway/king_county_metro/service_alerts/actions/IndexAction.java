package org.onebusaway.king_county_metro.service_alerts.actions;

import java.util.Collections;
import java.util.List;

import org.onebusaway.king_county_metro.service_alerts.impl.AlertBeanComparator;
import org.onebusaway.king_county_metro.service_alerts.model.beans.ResolvedAlertBean;
import org.onebusaway.king_county_metro.service_alerts.model.beans.UnresolvedAlertBean;
import org.onebusaway.king_county_metro.service_alerts.services.AlertBeanService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

public class IndexAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private AlertBeanService _alertService;

  private List<UnresolvedAlertBean> _unresolvedAlerts;

  private List<ResolvedAlertBean> _resolvedAlerts;

  @Autowired
  public void setAlertBeanService(AlertBeanService alertService) {
    _alertService = alertService;
  }

  public List<UnresolvedAlertBean> getUnresolvedAlerts() {
    return _unresolvedAlerts;
  }

  public List<ResolvedAlertBean> getResolvedAlerts() {
    return _resolvedAlerts;
  }

  @Override
  public String execute() {

    _unresolvedAlerts = _alertService.getUnresolvedAlerts();
    _resolvedAlerts = _alertService.getResolvedAlerts();

    Collections.sort(_unresolvedAlerts, new AlertBeanComparator());
    Collections.sort(_resolvedAlerts, new AlertBeanComparator());

    return SUCCESS;
  }
}
