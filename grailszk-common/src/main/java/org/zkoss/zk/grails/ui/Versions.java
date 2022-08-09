package org.zkoss.zk.grails.ui;

import org.zkoss.zk.fn.ZkFns;

public class Versions {
    private static final char ZK6_VERSION = '6';

    static void versionValidator() {
        final String zkVersion = ZkFns.getVersion();
        final char majorZkVersion = zkVersion.charAt(0);
        if (majorZkVersion <= ZK6_VERSION) {
            throw new RuntimeException(
                "ZK version " + zkVersion + " not supported."
                + " Support is only granted for versions 6 or above.");
        }
    }
}
