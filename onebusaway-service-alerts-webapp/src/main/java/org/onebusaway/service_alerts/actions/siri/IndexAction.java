package org.onebusaway.service_alerts.actions.siri;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.onebusaway.service_alerts.services.SiriService;
import org.onebusaway.siri.core.SiriServer;
import org.springframework.beans.factory.annotation.Autowired;

public class IndexAction implements ServletRequestAware, ServletResponseAware {

  private SiriService _siriService;

  private HttpServletRequest _request;

  private HttpServletResponse _response;

  @Autowired
  public void setSiriService(SiriService siriService) {
    _siriService = siriService;
  }

  @Override
  public void setServletRequest(HttpServletRequest request) {
    _request = request;
  }

  @Override
  public void setServletResponse(HttpServletResponse response) {
    _response = response;
  }

  public String execute() throws IOException {
    SiriServer server = _siriService.getServer();
    server.handleRawRequest(_request.getReader(), _response.getWriter());
    return null;
  }
}
