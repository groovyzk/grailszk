package jquery

import org.junit.Test

class TextTests extends zk.grails.ComposerTestCase {

    @Test
    void "test get text from non-existed component"() {
        browser.go "/zk/jquery/text_test.zul"
        waitFor {
            jq('#result').text() == "result:"
        }
    }

    @Test
    void "test get text from label"() {
        browser.go "/zk/jquery/text_test.zul"

        waitFor { jq('#result1').text() == "result: label" }
        waitFor { jq('#result2').text() == "result: label" }

        waitFor { jq('#result3').text() == "result: Button" }
        waitFor { jq('#result4').text() == "button does not have value attr" }
    }

}
