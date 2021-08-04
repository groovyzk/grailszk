package jquery

import geb.junit4.GebReportingTest
import org.junit.Test

class PrototypeTests extends GebReportingTest {

    @Test
    void testAddMethodViaPrototype() {
        browser.go "/zk/jquery/prototype_test.zul"
        waitFor {
            $('#comp_2-real').value() == "Empty"
        }
        assert $('span[class="z-comboitem-text"]').size() == 2 // First and Second
    }
}
