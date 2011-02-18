package org.onebusaway.service_alerts.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.collections.FunctionalLibrary;
import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.services.SiriService;
import org.onebusaway.service_alerts.services.SituationService;
import org.onebusaway.siri.core.SiriServer;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedAgencyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedStopBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.org.siri.siri.AffectedStopPointStructure;
import uk.org.siri.siri.AffectsScopeStructure;
import uk.org.siri.siri.AffectsScopeStructure.Operators;
import uk.org.siri.siri.AffectsScopeStructure.StopPoints;
import uk.org.siri.siri.AffectedOperatorStructure;
import uk.org.siri.siri.DefaultedTextStructure;
import uk.org.siri.siri.EntryQualifierStructure;
import uk.org.siri.siri.EnvironmentReasonEnumeration;
import uk.org.siri.siri.EquipmentReasonEnumeration;
import uk.org.siri.siri.MiscellaneousReasonEnumeration;
import uk.org.siri.siri.OperatorRefStructure;
import uk.org.siri.siri.PersonnelReasonEnumeration;
import uk.org.siri.siri.PtSituationElementStructure;
import uk.org.siri.siri.ServiceDelivery;
import uk.org.siri.siri.SituationExchangeDeliveryStructure;
import uk.org.siri.siri.SituationExchangeDeliveryStructure.Situations;
import uk.org.siri.siri.SituationVersion;
import uk.org.siri.siri.StopPointRefStructure;
import uk.org.siri.siri.WorkflowStatusEnumeration;

@Component
class SituationServiceImpl implements SituationService {

  private SiriService _sirivService;

  private Map<String, SituationConfiguration> _situationsById = new HashMap<String, SituationConfiguration>();

  @Autowired
  public void setSiriService(SiriService siriService) {
    _sirivService = siriService;
  }

  @Override
  public SituationConfiguration createSituation() {

    SituationConfiguration configuration = new SituationConfiguration();

    long t = System.currentTimeMillis();
    String id = Long.toString(t);
    configuration.setId(id);
    configuration.setLastUpdate(t);
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

    handleUpdate(config);

    return config;
  }

  @Override
  public SituationConfiguration updateVisibility(String id, boolean visible) {
    SituationConfiguration config = _situationsById.get(id);
    if (config == null)
      return null;

    if (config.isVisible() != visible) {
      config.setVisible(visible);
      handleUpdate(config, !visible);
    }

    return config;
  }

  @Override
  public SituationConfiguration setAffectedAgencyForSituation(String id,
      String agencyId, boolean active) {

    SituationConfiguration config = _situationsById.get(id);
    if (config == null)
      return null;

    SituationBean situation = config.getSituation();
    SituationAffectsBean affects = getAffectsForSituation(situation);

    List<SituationAffectedAgencyBean> agencies = affects.getAgencies();

    if (active) {

      if (agencies == null) {
        agencies = new ArrayList<SituationAffectedAgencyBean>();
        affects.setAgencies(agencies);
      }

      SituationAffectedAgencyBean match = FunctionalLibrary.filterFirst(
          agencies, "agencyId", agencyId);

      if (match == null) {
        match = new SituationAffectedAgencyBean();
        match.setAgencyId(agencyId);
        agencies.add(match);
        handleUpdate(config);
      }
    } else {
      if (agencies != null) {

        List<SituationAffectedAgencyBean> matches = FunctionalLibrary.filter(
            agencies, "agencyId", agencyId);

        if (agencies.removeAll(matches))
          handleUpdate(config);
      }
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
        handleUpdate(config);
      }
    } else {
      if (stops != null) {

        List<SituationAffectedStopBean> matches = FunctionalLibrary.filter(
            stops, "stopId", stopId);

        if (stops.removeAll(matches))
          handleUpdate(config);
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

  private void handleUpdate(SituationConfiguration config) {
    handleUpdate(config, config.isVisible());
  }

  private void handleUpdate(SituationConfiguration config,
      boolean previousVisibility) {

    config.setLastUpdate(System.currentTimeMillis());

    /**
     * If we're visible or we were visible and now we're not, publish
     */
    if (config.isVisible() || previousVisibility) {
      PtSituationElementStructure ptSituation = constructSiriSituation(config);
      publishPtSituation(ptSituation);
    }
  }

  private PtSituationElementStructure constructSiriSituation(
      SituationConfiguration config) {

    SituationBean situation = config.getSituation();

    PtSituationElementStructure ptSituation = new PtSituationElementStructure();

    ptSituation.setCreationTime(new Date(situation.getCreationTime()));

    WorkflowStatusEnumeration progress = config.isVisible()
        ? WorkflowStatusEnumeration.OPEN : WorkflowStatusEnumeration.CLOSED;
    ptSituation.setProgress(progress);

    EntryQualifierStructure situationId = new EntryQualifierStructure();
    situationId.setValue(situation.getId());
    ptSituation.setSituationNumber(situationId);

    long lastUpdate = config.getLastUpdate();
    SituationVersion version = new SituationVersion();
    version.setValue(BigInteger.valueOf(lastUpdate));
    ptSituation.setVersion(version);
    ptSituation.setVersionedAtTime(new Date(lastUpdate));

    ptSituation.setAdvice(text(situation.getAdvice()));
    ptSituation.setDescription(text(situation.getDescription()));
    ptSituation.setDetail(text(situation.getDetail()));
    ptSituation.setInternal(text(situation.getInternal()));
    ptSituation.setSummary(text(situation.getSummary()));

    String envReason = situation.getEnvironmentReason();
    if (envReason != null)
      ptSituation.setEnvironmentReason(EnvironmentReasonEnumeration.fromValue(envReason));

    String equipReason = situation.getEquipmentReason();
    if (equipReason != null)
      ptSituation.setEquipmentReason(EquipmentReasonEnumeration.fromValue(equipReason));

    String miscReason = situation.getMiscellaneousReason();
    if (miscReason != null)
      ptSituation.setMiscellaneousReason(MiscellaneousReasonEnumeration.fromValue(miscReason));

    String personReason = situation.getPersonnelReason();
    if (personReason != null)
      ptSituation.setPersonnelReason(PersonnelReasonEnumeration.fromValue(personReason));

    ptSituation.setUndefinedReason(situation.getUndefinedReason());

    SituationAffectsBean affects = situation.getAffects();

    if (affects != null) {
      AffectsScopeStructure sAffects = new AffectsScopeStructure();
      ptSituation.setAffects(sAffects);
      
      /**
       * Affected Agencies?
       */
      if( !CollectionsLibrary.isEmpty(affects.getAgencies())) {
        Operators operators = new Operators();
        sAffects.setOperators(operators);
        
        for( SituationAffectedAgencyBean affectedAgency : affects.getAgencies() ) {
          AffectedOperatorStructure sAffectedOperator = new AffectedOperatorStructure();
          OperatorRefStructure operatorRef = new OperatorRefStructure();
          operatorRef.setValue(affectedAgency.getAgencyId());
          sAffectedOperator.setOperatorRef(operatorRef);
          operators.getAffectedOperator().add(sAffectedOperator);
        }
      }

      /**
       * Affected Stops?
       */
      if (!CollectionsLibrary.isEmpty(affects.getStops())) {

        StopPoints stopPoints = new StopPoints();
        sAffects.setStopPoints(stopPoints);

        for (SituationAffectedStopBean affectedStop : affects.getStops()) {
          AffectedStopPointStructure sAffectedStopPoint = new AffectedStopPointStructure();
          StopPointRefStructure stopPointRef = new StopPointRefStructure();
          stopPointRef.setValue(affectedStop.getStopId());
          sAffectedStopPoint.setStopPointRef(stopPointRef);
          stopPoints.getAffectedStopPoint().add(sAffectedStopPoint);
        }
      }
    }

    return ptSituation;
  }

  private void publishPtSituation(PtSituationElementStructure ptSituation) {
    Situations situations = new Situations();
    situations.getPtSituationElement().add(ptSituation);

    SituationExchangeDeliveryStructure situationExchangeDelivery = new SituationExchangeDeliveryStructure();
    situationExchangeDelivery.setSituations(situations);

    ServiceDelivery serviceDelivery = new ServiceDelivery();
    List<SituationExchangeDeliveryStructure> situationExchangeDeliveries = serviceDelivery.getSituationExchangeDelivery();
    situationExchangeDeliveries.add(situationExchangeDelivery);

    SiriServer server = _sirivService.getServer();
    server.publish(serviceDelivery);
  }

  private DefaultedTextStructure text(NaturalLanguageStringBean nls) {

    if (nls == null)
      return null;

    DefaultedTextStructure text = new DefaultedTextStructure();
    text.setLang(nls.getLang());
    text.setValue(nls.getValue());
    return text;
  }
}
