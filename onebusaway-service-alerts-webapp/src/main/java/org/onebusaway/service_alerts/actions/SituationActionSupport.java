package org.onebusaway.service_alerts.actions;

import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.api.ResponseCodes;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.EntryWithReferencesBean;
import org.onebusaway.presentation.bundles.ResourceBundleSupport;
import org.onebusaway.presentation.bundles.service_alerts.Effects;
import org.onebusaway.presentation.bundles.service_alerts.Reasons;
import org.onebusaway.presentation.bundles.service_alerts.Sensitivity;
import org.onebusaway.presentation.bundles.service_alerts.Severity;
import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.model.SituationConfigurationV2Bean;
import org.onebusaway.service_alerts.model.beans.ResolvedAlertBean;
import org.onebusaway.service_alerts.services.AlertBeanService;
import org.onebusaway.service_alerts.services.AlertDao;
import org.onebusaway.service_alerts.services.SituationService;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedAgencyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedCallBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedStopBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedVehicleJourneyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

@Results({
    @Result(type = "redirectAction", name = "redirectToSituation", params = {
        "actionName", "situation", "id", "${id}", "parse", "true"}),
    @Result(type = "redirectAction", name = "redirectToSituations", params = {
        "actionName", "situation!list"}),
    @Result(type = "json", name = "json", params = {"root", "response"})})
public abstract class SituationActionSupport extends ActionSupport implements
    ModelDriven<SituationConfiguration> {

  private static final long serialVersionUID = 1L;

  protected TransitDataService _transitDataService;

  protected AlertDao _alertDao;

  protected SituationService _situationService;

  protected AlertBeanService _alertBeanService;

  protected String _id;

  protected SituationConfiguration _model = new SituationConfiguration();

  private ResponseBean _response;

  private List<ResolvedAlertBean> _resolvedAlerts;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setAlertDao(AlertDao alertDao) {
    _alertDao = alertDao;
  }

  @Autowired
  public void setSituationService(SituationService situationService) {
    _situationService = situationService;
  }

  @Autowired
  public void setAlertBeanService(AlertBeanService alertBeanService) {
    _alertBeanService = alertBeanService;
  }

  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  @Override
  public SituationConfiguration getModel() {
    return _model;
  }

  public ResponseBean getResponse() {
    return _response;
  }

  public List<ResolvedAlertBean> getResolvedAlerts() {
    return _resolvedAlerts;
  }

  /****
   * Methods
   ****/

  public Map<String, String> getReasonValues() {
    return ResourceBundleSupport.getLocaleMap(this, Reasons.class);
  }

  public Map<String, String> getEffectValues() {
    return ResourceBundleSupport.getLocaleMap(this, Effects.class);
  }

  public Map<String, String> getSeverityValues() {
    return ResourceBundleSupport.getLocaleMap(this, Severity.class);
  }

  public Map<String, String> getSensitivityValues() {
    return ResourceBundleSupport.getLocaleMap(this, Sensitivity.class);
  }

  /****
   * Private Methods
   ****/

  private String string(String value) {
    if (value == null || value.isEmpty() || value.equals("null"))
      return null;
    return value;
  }

  protected NaturalLanguageStringBean nls(NaturalLanguageStringBean nls) {
    if (nls == null || string(nls.getValue()) == null)
      return null;
    return nls;
  }

  protected void fillResponse() {

    if (_model == null)
      return;

    _resolvedAlerts = _alertBeanService.getResolvedAlertsForSituationConfigurationId(_model.getId());

    BeanFactoryV2 factory = new BeanFactoryV2(true);

    ServiceAlertBean situation = _model.getSituation();
    SituationAffectsBean affects = situation.getAllAffects();

    if (affects != null) {
      List<SituationAffectedAgencyBean> agencies = affects.getAgencies();
      if (agencies != null) {
        for (SituationAffectedAgencyBean agency : agencies) {
          AgencyBean agencyBean = _transitDataService.getAgency(agency.getAgencyId());
          if (agencyBean != null)
            factory.addToReferences(agencyBean);
        }
      }

      List<SituationAffectedStopBean> stops = affects.getStops();
      if (stops != null) {
        for (SituationAffectedStopBean affectedStop : stops) {
          StopBean stop = _transitDataService.getStop(affectedStop.getStopId());
          if (stop != null)
            factory.addToReferences(stop);
        }
      }

      List<SituationAffectedVehicleJourneyBean> journeys = affects.getVehicleJourneys();
      if (journeys != null) {
        for (SituationAffectedVehicleJourneyBean journey : journeys) {
          if (journey.getLineId() == null)
            continue;
          RouteBean route = _transitDataService.getRouteForId(journey.getLineId());
          if (route != null)
            factory.addToReferences(route);
          List<SituationAffectedCallBean> calls = journey.getCalls();
          if (calls != null) {
            for (SituationAffectedCallBean call : calls) {
              StopBean stop = _transitDataService.getStop(call.getStopId());
              if (stop != null)
                factory.addToReferences(stop);
            }
          }
        }
      }
    }

    SituationConfigurationV2Bean bean = new SituationConfigurationV2Bean();
    bean.setId(_model.getId());
    bean.setVisible(_model.isVisible());
    bean.setSituation(factory.getSituation(situation));

    EntryWithReferencesBean<SituationConfigurationV2Bean> entry = factory.entry(bean);
    _response = new ResponseBean(2, ResponseCodes.RESPONSE_OK, "OK", entry);
  }
}
