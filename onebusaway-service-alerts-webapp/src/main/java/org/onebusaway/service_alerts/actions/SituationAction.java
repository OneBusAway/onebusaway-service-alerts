package org.onebusaway.service_alerts.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.api.ResponseCodes;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.EntryWithReferencesBean;
import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.presentation.bundles.ResourceBundleSupport;
import org.onebusaway.presentation.bundles.service_alerts.Effects;
import org.onebusaway.presentation.bundles.service_alerts.Reasons;
import org.onebusaway.presentation.bundles.service_alerts.Sensitivity;
import org.onebusaway.presentation.bundles.service_alerts.Severity;
import org.onebusaway.presentation.impl.StackInterceptor.AddToStack;
import org.onebusaway.service_alerts.impl.SituationConfigSummaryComparator;
import org.onebusaway.service_alerts.impl.SituationConfigVisibilityComparator;
import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.model.SituationConfigurationV2Bean;
import org.onebusaway.service_alerts.model.beans.ResolvedAlertBean;
import org.onebusaway.service_alerts.model.properties.AlertProperties;
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
import org.onebusaway.transit_data.model.service_alerts.SituationConditionDetailsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConsequenceBean;
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
@AddToStack("consequence")
public class SituationAction extends ActionSupport implements
    ModelDriven<SituationConfiguration> {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private AlertDao _alertDao;

  private SituationService _situationService;

  private AlertBeanService _alertBeanService;

  private String _id;

  private List<String> _stopIds = Collections.emptyList();

  private String _routeId;

  private String _routeIds;

  private String _directionId;

  private String _apiKey;

  private List<SituationConfiguration> _models;

  private SituationConfiguration _model = new SituationConfiguration();

  private List<String> _groupProperty = new ArrayList<String>();

  private ResponseBean _response;

  private String _agencyId;

  private boolean _enabled;

  private ConsequenceFormBean _consequence = new ConsequenceFormBean();

  private int _index;

  private List<ResolvedAlertBean> _resolvedAlerts;

  private String _sort;

  private String _unresolvedAlertId;

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

  public void setGroupProperty(List<String> groupProperty) {
    _groupProperty = groupProperty;
  }

  public List<String> getGroupProperty() {
    return _groupProperty;
  }

  public void setAgencyId(String agencyId) {
    _agencyId = agencyId;
  }

  public String getAgencyId() {
    return _agencyId;
  }

  public void setStopId(List<String> stopIds) {
    _stopIds = stopIds;
  }

  public List<String> getStopId() {
    return _stopIds;
  }

  public void setRouteId(String routeId) {
    _routeId = routeId;
  }

  public String getRouteId() {
    return _routeId;
  }

  public void setRouteIds(String routeIds) {
    _routeIds = routeIds;
  }

  public String getRouteIds() {
    return _routeIds;
  }

  public void setDirectionId(String directionId) {
    _directionId = directionId;
  }

  public String getDirectionId() {
    return _directionId;
  }

  public void setApiKey(String apiKey) {
    _apiKey = apiKey;
  }

  public String getApiKey() {
    return _apiKey;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  public ConsequenceFormBean getConsequence() {
    return _consequence;
  }

  public void setIndex(int index) {
    _index = index;
  }

  public void setSort(String sort) {
    _sort = sort;
  }

  public String getSort() {
    return _sort;
  }

  public void setUnresolvedAlertId(String unresolvedAlertId) {
    _unresolvedAlertId = unresolvedAlertId;
  }

  @Override
  public SituationConfiguration getModel() {
    return _model;
  }

  public List<ResolvedAlertBean> getResolvedAlerts() {
    return _resolvedAlerts;
  }

  public List<SituationConfiguration> getModels() {
    return _models;
  }

  public ResponseBean getResponse() {
    return _response;
  }

  /****
   * Methods
   ****/

  public String list() {

    _models = new ArrayList<SituationConfiguration>(
        _alertDao.getConfigurations());

    if (_sort != null) {
      if ("visible".equals(_sort))
        Collections.sort(_models, SituationConfigVisibilityComparator.INSTANCE);
      else if ("summary".equals(_sort))
        Collections.sort(_models, SituationConfigSummaryComparator.INSTANCE);
    }
    return "list";
  }

  public String create() {

    AlertProperties group = new AlertProperties();
    if (!CollectionsLibrary.isEmpty(_groupProperty)) {
      for (String token : _groupProperty)
        group.putEncodedProperty(token);
    }

    /**
     * Create the situation
     */
    _model = _situationService.createSituation(group);

    /**
     * Automatically resolve an unresolved alert on creation?
     */
    if (_model != null && _unresolvedAlertId != null) {
      List<String> ids = Arrays.asList(_model.getId());
      _alertBeanService.resolveAlertToExistingConfiguration(_unresolvedAlertId,
          ids);
    }

    fillResponse();
    return "redirectToSituation";
  }

  @Override
  public String execute() {
    _model = _alertDao.getConfigurationForId(_model.getId());
    fillResponse();
    return SUCCESS;
  }

  public String json() {
    _model = _alertDao.getConfigurationForId(_model.getId());
    fillResponse();
    return "json";
  }

  public String submitDetails() {

    ServiceAlertBean situation = _model.getSituation();

    situation.setReason(string(situation.getReason()));

    _model = _situationService.updateConfigurationDetails(_model.getId(),
        situation);
    return "redirectToSituation";
  }

  public String removeKey() {

    _model = _alertDao.getConfigurationForId(_model.getId());
    if (_model == null)
      return INPUT;
    int index = 0;

    AlertProperties matchedKey = null;

    for (AlertProperties key : _model.getKeys()) {
      if (index == _index) {
        matchedKey = key;
        break;
      }
      index++;
    }

    if (matchedKey != null)
      _alertDao.removeKeyFromConfiguration(_model, matchedKey);

    return "redirectToSituation";
  }

  public String updateVisibility() {
    _model = _situationService.updateVisibility(_model.getId(),
        _model.isVisible());
    fillResponse();
    return "json";
  }

  public String delete() {
    _situationService.deleteSituationForId(_model.getId());
    return "redirectToSituations";
  }

  public String saveAll() {
    _situationService.saveAllAlerts();
    return "redirectToSituations";
  }

  public String updateAffectedAgency() {
    _model = _situationService.setAffectedAgencyForSituation(_model.getId(),
        _agencyId, _enabled);
    if (_model == null)
      return INPUT;
    fillResponse();
    return "json";
  }

  public String updateAffectedStop() {
    _model = _situationService.setAffectedStopsForSituation(_model.getId(),
        _stopIds, _enabled);
    if (_model == null)
      return INPUT;
    fillResponse();
    return "json";
  }

  public String updateAffectedVehicleJourney() {
    _model = _situationService.setAffectedVehicleJourneyForSituation(
        _model.getId(), _routeId, _directionId, _enabled);
    if (_model == null)
      return INPUT;
    fillResponse();
    return "json";
  }

  public String updateAffectedVehicleJourneyStopCall() {
    if (_stopIds.isEmpty())
      return INPUT;
    _model = _situationService.setAffectedVehicleJourneyStopCallForSituation(
        _model.getId(), _routeId, _directionId, _stopIds.get(0), _enabled);
    if (_model == null)
      return INPUT;
    fillResponse();
    return "json";
  }

  public String updateAffectedApplication() {
    _model = _situationService.setAffectedApplicationForSituation(
        _model.getId(), _apiKey, _enabled);
    if (_model == null)
      return INPUT;
    fillResponse();
    return "json";
  }

  public String addConsequence() {

    SituationConsequenceBean consequence = fillConsequence();

    _model = _situationService.addConsequenceForSituation(_model.getId(),
        consequence);
    if (_model == null)
      return INPUT;
    fillResponse();
    return "json";
  }

  public String updateConsequence() {

    SituationConsequenceBean consequence = fillConsequence();

    _model = _situationService.updateConsequenceForSituation(_model.getId(),
        _index, consequence);
    if (_model == null)
      return INPUT;
    fillResponse();
    return "json";
  }

  public String removeConsequence() {
    _model = _situationService.removeConsequenceForSituation(_model.getId(),
        _index);
    if (_model == null)
      return INPUT;
    fillResponse();
    return "json";
  }

  /****
   * 
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

  private List<NaturalLanguageStringBean> nls(NaturalLanguageStringBean nls) {
    if (nls == null || string(nls.getValue()) == null)
      return null;
    return Arrays.asList(nls);
  }

  private SituationConsequenceBean fillConsequence() {
    SituationConsequenceBean bean = new SituationConsequenceBean();
    bean.setCondition(string(_consequence.getCondition()));
    String diversionPath = string(_consequence.getDiversionPath());
    if (diversionPath != null) {
      SituationConditionDetailsBean details = new SituationConditionDetailsBean();
      EncodedPolylineBean poly = new EncodedPolylineBean();
      poly.setPoints(diversionPath);
      details.setDiversionPath(poly);
      bean.setConditionDetails(details);
    }
    return bean;
  }

  private void fillResponse() {

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
