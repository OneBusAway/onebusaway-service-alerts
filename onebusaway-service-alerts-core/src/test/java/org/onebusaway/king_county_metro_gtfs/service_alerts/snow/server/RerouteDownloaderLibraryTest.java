package org.onebusaway.king_county_metro_gtfs.service_alerts.snow.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.service_alerts.impl.AlertDownloaderLibrary;
import org.onebusaway.service_alerts.model.AlertDescription;
import org.onebusaway.service_alerts.model.RouteAndRegionRef;

public class RerouteDownloaderLibraryTest {

  @Test
  public void test() throws Exception {

    File dir = new File("/Users/bdferris/Desktop/Weather");
    File[] files = dir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith("html");
      }
    });

    AlertDownloaderLibrary library = new AlertDownloaderLibrary();

    Map<RouteAndRegionRef, Map<String, AlertDescription>> reroutesByRefAndDescription = new FactoryMap<RouteAndRegionRef, Map<String, AlertDescription>>(
        new HashMap<String, AlertDescription>());

    for (File file : files) {

      System.out.println(file.getName());

      Map<RouteAndRegionRef, Map<String, AlertDescription>> newReroutesByRefAndDescription = new FactoryMap<RouteAndRegionRef, Map<String, AlertDescription>>(
          new HashMap<String, AlertDescription>());

      BufferedReader reader = new BufferedReader(new FileReader(file));
      List<AlertDescription> records = library.parseRecords(reader);
      for (AlertDescription record : records) {
        RouteAndRegionRef ref = record.getRef();
        Map<String, AlertDescription> reroutesByDescription = newReroutesByRefAndDescription.get(ref);
        AlertDescription existing = reroutesByDescription.put(
            record.getDescription(), record);
        if (existing != null && ! existing.allPropertiesAreEqual(record)) {
          System.err.println("overlapping reroutes: from=" + existing);
          System.err.println("overlapping reroutes:   to=" + record);
        }

        record.getDescription();
      }

      Set<RouteAndRegionRef> refs = new HashSet<RouteAndRegionRef>();
      refs.addAll(reroutesByRefAndDescription.keySet());
      refs.addAll(newReroutesByRefAndDescription.keySet());
      List<RouteAndRegionRef> orderedRefs = new ArrayList<RouteAndRegionRef>(refs);
      Collections.sort(orderedRefs);
      for (RouteAndRegionRef ref : orderedRefs) {
        System.out.println("ref=" + ref);
        Map<String, AlertDescription> from = reroutesByRefAndDescription.get(ref);
        Map<String, AlertDescription> to = newReroutesByRefAndDescription.get(ref);
        Set<String> descs = new HashSet<String>();
        descs.addAll(from.keySet());
        descs.addAll(to.keySet());
        List<String> orderedDescs = new ArrayList<String>(descs);
        Collections.sort(orderedDescs);
        for (String desc : orderedDescs) {
          AlertDescription fromReroute = from.get(desc);
          AlertDescription toReroute = to.get(desc);
          if (fromReroute == null && toReroute != null) {
            System.out.println("   ADD: " + desc);
          } else if (fromReroute != null && toReroute == null) {
            System.out.println("   DEL: " + desc);
          } else if (fromReroute != null && toReroute != null) {
            //System.out.println("  SAME: " + desc);
          } else {
            System.out.println("  unexpected: " + desc);
          }
        }
      }

      reroutesByRefAndDescription = newReroutesByRefAndDescription;
    }
  }
}
