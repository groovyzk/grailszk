package zk

import geb.junit4.GebReportingTest
import org.junit.Test

class TagLibTests extends GebReportingTest {

    @Test
    void test_taglib_Z_resource() {
        browser.go "/zk/taglib_tests.zul"
        waitFor {
            def src = $('#comp_1').attr('src')
            src.startsWith("http://localhost:8080/zk/ext/images/grails_logo.png")
        }
    }

}
