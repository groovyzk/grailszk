package org.zkoss.zk.grails

import grails.util.Holders



//
// Helper class to get the Url Mapping in the ZKPageFilter and ZkGrailsPlugin
//
@SuppressWarnings("deprecation")
class ZkConfigHelper {

    //
    // Get the extensions configuration using the ConfigurationHolder
    // Return String[]{"zul"} if not configured
    //
    static ArrayList<String> getSupportExtensions() {
//        def exts = ConfigurationHolder.config?.grails.zk.extensions
        def exts = Holders.config.grails.zk?.extensions
        if(exts) {
            return exts
        } else {
            return ["zul"]
        }
    }

    //
    // Issue #146 - Support for skip zscript auto wiring for better performance.
    // Default to false to maintan backward compatibility.
    //
    static boolean skipZscriptWiring() {
        def skipZscriptWiring = Holders.config.grails.zk?.skipZscriptWiring

        if(skipZscriptWiring != null) {
            return skipZscriptWiring
        }

        return false
    }
}
