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
import org.onebusaway.presentation.bundles.service_alerts.EnvironmentReasons;
import org.onebusaway.presentation.bundles.service_alerts.EquipmentReasons;
import org.onebusaway.presentation.bundles.service_alerts.MiscellaneousReasons;
import org.onebusaway.presentation.bundles.service_alerts.PersonnelReasons;
import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.model.SituationConfigurationV2Bean;
import org.onebusaway.service_alerts.services.SituationService;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedStopBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

@Results({
    @Result(type = "redirectAction", name = "redirectToSituation", params = {
        "actionName", "situation", "id", "${id}", "parse", "true"}),
    @Result(type = "redirectAction", name = "delete", params = {
        "actionName", "situation!list"}),
    @Result(type = "json", name = "json", params = {"root", "response"})})
public class SituationAction extends ActionSupport implements
    ModelDriven<SituationConfiguration> {

  private static final long serialVersionUID = 1L;

  private SituationService _situationService;

  private String _id;

  private String _stopId;

  private List<SituationConfiguration> _models;

  private SituationConfiguration _model = new SituationConfiguration();

  private ResponseBean _response;

  private TransitDataService _transitDataService;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setSituationService(SituationService situationService) {
    _situationService = situationService;
  }

  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public void setStopId(String stopId) {
    _stopId = stopId;
  }

  public String getStopId() {
    return _stopId;
  }

  @Override
  public SituationConfiguration getModel() {
    return _model;
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
    _models = _situationService.getAllSituations();
    return "list";
  }

  public String create() {
    _model = _situationService.createSituation();
    fillResponse();
    return "redirectToSituation";
  }

  @Override
  public String execute() {
    _model = _situationService.getSituationForId(_model.getId());
    fillResponse();
    return SUCCESS;
  }

  public String json() {
    _model = _situationService.getSituationForId(_model.getId());
    fillResponse();
    return "json";
  }

  public String submitDetails() {

    SituationBean situation = _model.getSituation();

    situation.setAdvice(nls(situation.getAdvice()));
    situation.setDescription(nls(situation.getDescription()));
    situation.setDetail(nls(situation.getDetail()));
    situation.setInternal(nls(situation.getInternal()));
    situation.setSummary(nls(situation.getSummary()));

    situation.setEnvironmentReason(string(situation.getEnvironmentReason()));
    situation.setEquipmentReason(string(situation.getEquipmentReason()));
    situation.setPersonnelReason(string(situation.getPersonnelReason()));
    situation.setMiscellaneousReason(string(situation.getMiscellaneousReason()));
    situation.setUndefinedReason(string(situation.getUndefinedReason()));

    _model = _situationService.updateConfigurationDetails(_model.getId(),
        situation);
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
    return "delete";
  }

  public String addAffectedStop() {
    _model = _situationService.setAffectedStopForSituation(_model.getId(),
        _stopId, true);
    if (_model == null)
      return INPUT;
    fillResponse();
    return "json";
  }

  public String removeAffectedStop() {
    _model = _situationService.setAffectedStopForSituation(_model.getId(),
        _stopId, false);
    if (_model == null)
      return INPUT;
    fillResponse();
    return "json";
  }

  /****
   * 
   ****/

  public Map<String, String> getEnvironmentReasonValues() {
    return ResourceBundleSupport.getLocaleMap(this, EnvironmentReasons.class);
  }

  public Map<String, String> getEquipmentReasonValues() {
    return ResourceBundleSupport.getLocaleMap(this, EquipmentReasons.class);
  }

  public Map<String, String> getMiscellaneousReasonValues() {
    return ResourceBundleSupport.getLocaleMap(this, MiscellaneousReasons.class);
  }

  public Map<String, String> getPersonnelReasonValues() {
    return ResourceBundleSupport.getLocaleMap(this, PersonnelReasons.class);
  }

  /****
   * Private Methods
   ****/

  private String string(String value) {
    if (value == null || value.isEmpty() || value.equals("null"))
      return null;
    return value;
  }

  private NaturalLanguageStringBean nls(NaturalLanguageStringBean nls) {
    if (nls == null || string(nls.getValue()) == null)
      return null;
    return nls;
  }

  private void fillResponse() {

    if (_model == null)
      return;

    BeanFactoryV2 factory = new BeanFactoryV2(true);

    SituationBean situation = _model.getSituation();
    SituationAffectsBean affects = situation.getAffects();

    if (affects != null) {
      List<SituationAffectedStopBean> stops = affects.getStops();
      if (stops != null) {
        for (SituationAffectedStopBean affectedStop : stops) {
          StopBean stop = _transitDataService.getStop(affectedStop.getStopId());
          if (stop != null)
            factory.addToReferences(stop);
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
