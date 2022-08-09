package org.zkoss.zk.grails

import grails.util.Holders



//
// Helper class to get the Url Mapping in the ZKPageFilter and GrailszkGrailsPlugin
//
@SuppressWarnings("deprecation")
class ZkConfigHelper {

    //
    // Get the extensions configuration using the ConfigurationHolder
    // Return String[]{"zul"} if not configured
    //
    static ArrayList<String> getSupportExtensions() {
        return Holders.config.getProperty("grails.zk.extensions", List, ["zul"])
    }

    //
    // Issue #146 - Support for skip zscript auto wiring for better performance.
    // Default to false to maintan backward compatibility.
    //
    static boolean skipZscriptWiring() {
        return Holders.config.getProperty("grails.zk.skipZscriptWiring", Boolean, false)
    }
}
