package zk

import org.junit.Test
import geb.junit4.GebReportingTest

class DehyphenTaglibTests extends GebReportingTest {

    @Test
    void testDehyphenTagLib() {
        browser.go "/zk/dehyphen_taglib.zul"
        waitFor {
            $('#comp_1').text() == "it should be de-hyphened"
        }
    }

}