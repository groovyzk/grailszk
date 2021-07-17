package jquery

import geb.junit4.GebReportingTest
import org.junit.Test

class WeaverTests extends GebReportingTest {

    @Test
    void testBeforeNotProceed() {
        browser.go "/zk/jquery/before_not_proceed_test.zul"
        $('#comp_3').click()
        waitFor { $('#comp_4').text() == "set by before advice" }
    }

    @Test
    void testAfter() {
        browser.go "/zk/jquery/after_test.zul"
        $('#comp_3').click()
        waitFor { $('#comp_4').text() == "this is set by handler" }
        waitFor { $('#comp_5').text() == "set by after advice"    }
    }

}
