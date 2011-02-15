package org.onebusaway.service_alerts.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.service_alerts.model.AlertDescription;
import org.onebusaway.service_alerts.services.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AlertDownloaderTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(AlertDownloaderTask.class);

  private AlertDownloaderLibrary _library = new AlertDownloaderLibrary();

  private URL _url;

  private AlertService _controller;

  private ScheduledExecutorService _executor;

  private int _refreshPeriod = 60;

  public void setUrl(URL url) {
    _url = url;
  }

  @Autowired
  public void setController(AlertService controller) {
    _controller = controller;
  }

  public void setRefreshPeriod(int refreshPeriod) {
    _refreshPeriod = refreshPeriod;
  }

  @PostConstruct
  public void start() {
    _executor = Executors.newSingleThreadScheduledExecutor();
    _executor.scheduleAtFixedRate(this, 0, _refreshPeriod, TimeUnit.SECONDS);
  }

  @PreDestroy
  public void stop() {
    _executor.shutdown();
  }

  public void run() {

    try {

      BufferedReader reader = new BufferedReader(new InputStreamReader(
          _url.openStream()));

      List<AlertDescription> activeAlerts = _library.parseRecords(reader);

      _controller.setActiveAlerts(activeAlerts);

    } catch (Exception ex) {
      _log.error("error downloading reroute information", ex);
    }
  }
}
