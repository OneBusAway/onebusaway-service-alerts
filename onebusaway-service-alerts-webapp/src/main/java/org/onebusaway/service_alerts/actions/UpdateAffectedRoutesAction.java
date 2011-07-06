package org.onebusaway.service_alerts.actions;

import java.io.IOException;
import java.util.List;

public class UpdateAffectedRoutesAction extends SituationActionSupport {

  private static final long serialVersionUID = 1L;

  private List<String> _routeIds;

  private boolean _enabled;

  public void setRouteIds(List<String> routeIds) {
    _routeIds = routeIds;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  /****
   * Methods
   ****/

  @Override
  public String execute() throws IOException {

    _model = _situationService.setAffectedVehicleJourneysForSituation(
        _model.getId(), _routeIds, _enabled);

    if (_model == null)
      return INPUT;
    fillResponse();
    return "json";
  }
}
