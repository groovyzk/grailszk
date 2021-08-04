package route

import org.junit.Test

class RouteTests extends zk.grails.ComposerTestCase {

    @Test
    void "test routing"() {
        browser.go "/zk/route/"

        waitFor { jq('#result').text() == "Ready" }

        $('#comp_3').click()
        waitFor { jq('#result').text() == "Project: 1" }

        $('#comp_4').click()
        waitFor { jq('#result').text() == "Project: 2" }

        $('#comp_5').click()
        waitFor { jq('#result').text() == "Blog: ck" }

        $('#comp_6').click()
        waitFor { jq('#result').text() == "Blog: me" }

    }

}
