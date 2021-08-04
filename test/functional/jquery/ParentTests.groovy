package jquery

import org.junit.Test
import org.openqa.selenium.Keys
import org.openqa.selenium.interactions.Actions

class ParentTests extends zk.grails.ComposerTestCase {

    @Test
    void testJQueryAPIGettingParent() {
        browser.go "/zk/jquery/parent_test.zul"
        waitFor { jq('#btn').text() == "Passed" }
    }

}
