package org.onebusaway.service_alerts.impl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.service_alerts.services.SiriService;
import org.onebusaway.siri.core.SiriServer;
import org.springframework.stereotype.Component;

@Component
class SiriServiceWebappImpl implements SiriService {

  private SiriServer _server;

  private String _serverUrl;

  public void setServerUrl(String serverUrl) {
    _serverUrl = serverUrl;
  }

  @PostConstruct
  public void start() {
    _server = new SiriServer();

    if (_serverUrl != null) {
      _server.setUrl(_serverUrl);
    }

    _server.start();
  }

  @PreDestroy
  public void stop() {
    if (_server != null)
      _server.stop();
  }

  @Override
  public SiriServer getServer() {
    return _server;
  }
}
