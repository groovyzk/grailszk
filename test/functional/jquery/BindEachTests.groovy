package jquery

import org.junit.Test
import org.openqa.selenium.Keys
import org.openqa.selenium.interactions.Actions

class BindEachTests extends zk.grails.ComposerTestCase {

    @Test
    void testBindEach() {
        browser.go "/zk/jquery/bind_each_test.zul"
        waitFor { jq('#result').text() == "3" }
    }

}
