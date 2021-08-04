package zk

import org.junit.Test

class Issue287Tests extends zk.grails.ComposerTestCase {

    @Test
    void "test for issue 287"() {
        browser.go "/zk/issue_287.zul"
        waitFor {
            def str = $('dl[class="error-details"] > dd').eq(2).text().trim()
            str == "At class zk.Issue287Composer#afterCompose = { | -> }. Please change type of the argument from: [class org.zkoss.zul.Button] to: [class org.zkoss.zul.Window]."
        }
    }
}
