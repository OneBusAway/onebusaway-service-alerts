package org.onebusaway.service_alerts.impl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.service_alerts.services.SiriService;
import org.onebusaway.siri.core.SiriCoreModule;
import org.onebusaway.siri.core.SiriServer;
import org.onebusaway.siri.core.guice.LifecycleService;
import org.springframework.stereotype.Component;

import com.google.inject.Guice;
import com.google.inject.Injector;

@Component
class SiriServiceWebappImpl implements SiriService {

  private SiriServer _server;

  private String _serverUrl;

  private LifecycleService _lifecycleService;

  public void setServerUrl(String serverUrl) {
    _serverUrl = serverUrl;
  }

  @PostConstruct
  public void start() {

    Injector injector = Guice.createInjector(SiriCoreModule.getModules());
    _server = injector.getInstance(SiriServer.class);

    if (_serverUrl != null) {
      _server.setUrl(_serverUrl);
    }

    _lifecycleService = injector.getInstance(LifecycleService.class);
    _lifecycleService.start();
  }

  @PreDestroy
  public void stop() {
    _lifecycleService.stop();
  }

  @Override
  public SiriServer getServer() {
    return _server;
  }
}
