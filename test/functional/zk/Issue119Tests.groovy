package zk

import org.junit.Test
import geb.junit4.GebReportingTest

class Issue119Tests extends GebReportingTest {

    @Test
    void test_taglib_Z_resource() {
        browser.go"/zk/issue_119.zul"
        waitFor {
            $('#comp_1').attr('src').startsWith('http://localhost:8080/zk/static/images/grails_logo.png')
        }
    }

}
