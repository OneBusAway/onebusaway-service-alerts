package org.onebusaway.king_county_metro.service_alerts.builder;

import java.io.File;

public class SnowRerouteBundleCreatorMain {

  public static void main(String[] args) throws Exception {

    if (args.length != 4) {
      printUsage();
      System.exit(-1);
    }

    SnowRerouteBundleCreator m = new SnowRerouteBundleCreator();

    m.setRerouteShapefile(new File(args[0]));
    m.setRegionsShapefile(new File(args[1]));
    m.setTransitDataServiveUrl(args[2]);
    m.setBundleOutputPath(new File(args[3]));

    m.run();
  }

  private static void printUsage() {
    System.err.println("usage: reroute_shapefile.shp regions_shapefile.shp transit_data_service_url bundle_output_path");
  }
}
