package zk.grails

import geb.junit4.GebReportingTest
import geb.navigator.Navigator

class ComposerTestCase extends GebReportingTest {

    protected Navigator jq(String q) {
        String s = q.replaceAll(/\#([_a-zA-Z0-9]+)/, { m, s ->
            "*[data-id=\"$s\"]"
        })
        return $(s)
    }

}
