package org.onebusaway.service_alerts.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.FunctionalLibrary;
import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.services.SituationService;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedStopBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.springframework.stereotype.Component;

@Component
class SituationServiceImpl implements SituationService {

  private Map<String, SituationConfiguration> _situationsById = new HashMap<String, SituationConfiguration>();

  @Override
  public SituationConfiguration createSituation() {

    SituationConfiguration configuration = new SituationConfiguration();

    long t = System.currentTimeMillis();
    String id = Long.toString(t);
    configuration.setId(id);
    configuration.setVisible(false);

    SituationBean situation = new SituationBean();
    situation.setId(id);
    situation.setCreationTime(t);
    configuration.setSituation(situation);

    _situationsById.put(id, configuration);

    return configuration;
  }

  @Override
  public SituationConfiguration getSituationForId(String id) {
    return _situationsById.get(id);
  }

  @Override
  public void deleteSituationForId(String id) {
    _situationsById.remove(id);
  }

  @Override
  public List<SituationConfiguration> getAllSituations() {
    return new ArrayList<SituationConfiguration>(_situationsById.values());
  }

  @Override
  public SituationConfiguration updateConfigurationDetails(String id,
      SituationBean situation) {

    SituationConfiguration config = _situationsById.get(id);
    if (config == null)
      return null;

    SituationBean model = config.getSituation();
    model.setAdvice(situation.getAdvice());
    model.setDescription(situation.getDescription());
    model.setDetail(situation.getDetail());
    model.setInternal(situation.getInternal());
    model.setSummary(situation.getSummary());

    model.setEnvironmentReason(situation.getEnvironmentReason());
    model.setEquipmentReason(situation.getEquipmentReason());
    model.setMiscellaneousReason(situation.getMiscellaneousReason());
    model.setPersonnelReason(situation.getPersonnelReason());
    model.setUndefinedReason(situation.getUndefinedReason());

    return config;
  }

  @Override
  public SituationConfiguration updateVisibility(String id, boolean visible) {
    SituationConfiguration config = _situationsById.get(id);
    if (config == null)
      return null;

    if (config.isVisible() != visible) {
      config.setVisible(visible);
    }

    return config;
  }

  @Override
  public SituationConfiguration setAffectedStopForSituation(String id,
      String stopId, boolean active) {

    SituationConfiguration config = _situationsById.get(id);
    if (config == null)
      return null;

    SituationBean situation = config.getSituation();
    SituationAffectsBean affects = getAffectsForSituation(situation);

    List<SituationAffectedStopBean> stops = affects.getStops();

    if (active) {

      if (stops == null) {
        stops = new ArrayList<SituationAffectedStopBean>();
        affects.setStops(stops);
      }

      SituationAffectedStopBean match = FunctionalLibrary.filterFirst(stops,
          "stopId", stopId);

      if (match == null) {
        match = new SituationAffectedStopBean();
        match.setStopId(stopId);
        stops.add(match);
      }
    } else {
      if (stops != null) {

        List<SituationAffectedStopBean> matches = FunctionalLibrary.filter(
            stops, "stopId", stopId);
        stops.removeAll(matches);
      }
    }

    return config;
  }

  /****
   * Private Methods
   ****/

  private SituationAffectsBean getAffectsForSituation(SituationBean situation) {

    SituationAffectsBean affects = situation.getAffects();
    if (affects == null) {
      affects = new SituationAffectsBean();
      situation.setAffects(affects);
    }
    return affects;
  }

}
