package jquery

import org.junit.Test
import org.openqa.selenium.Keys
import org.openqa.selenium.interactions.Actions

class BindEventTests extends zk.grails.ComposerTestCase {

    @Test
    void testBindOnClick() {
        browser.go "/zk/jquery/bind_event_test.zul"
        waitFor { jq('#button').text() == "Click me" }
        jq('#button').click()
        waitFor { jq('#result1').text() == 'clicked' }
    }

    @Test
    void testBindOnRightClick() {
        browser.go "/zk/jquery/bind_event_test.zul"
        waitFor { jq('#button').text() == "Click me" }
        new Actions(driver).contextClick(jq('#button').firstElement()).perform()
        waitFor { jq('#result1').text() == 'right clicked' }
    }

    @Test
    void testBindOnDoubleClick() {
        browser.go "/zk/jquery/bind_event_test.zul"
        waitFor { jq('#button').text() == "Click me" }
        new Actions(driver).doubleClick(jq('#button').firstElement()).perform()
        waitFor { jq('#result1').text() == 'double clicked' }
    }

    @Test
    void testBindOnOK() {
        browser.go "/zk/jquery/bind_event_test.zul"
        waitFor { jq('#text').value() == "Data" }
        jq('#text') << "\n"
        waitFor { jq('#result2').text() == 'enter pressed' }
    }

    @Test
    void testBindOnCancel() {
        browser.go "/zk/jquery/bind_event_test.zul"
        waitFor { jq('#text').value() == "Data" }
        jq('#text') << Keys.ESCAPE
        waitFor { jq('#result2').text() == 'ESC pressed' }
    }

    @Test
    void testBindOnCtrlKey() {
        browser.go "/zk/jquery/bind_event_test.zul"
        waitFor { jq('#text').value() == "Data" }
        jq('#text') << Keys.chord(Keys.CONTROL, Keys.F3)
        waitFor { jq('#result2').text() == 'Ctrl+F3 pressed' }
    }

    @Test
    void testBindOnFocus() {
        browser.go "/zk/jquery/bind_event_test.zul"
        waitFor { jq('#text').value() == "Data" }
        jq('#text').click()
        waitFor { jq('#result2').text() == 'focused' }
    }

    @Test
    void testBindOnBlur() {
        browser.go "/zk/jquery/bind_event_test.zul"
        waitFor { jq('#text').value() == "Data" }
        jq('#text').click()
        jq('#text') << Keys.TAB
        waitFor { jq('#result2').text() == 'blurred' }
    }

    @Test
    void testBindOnSelectForCombobox() {
        browser.go "/zk/jquery/bind_event_test.zul"
        waitFor { jq('#combo > input').value() == "Empty" }
        jq('#combo > .z-combobox-button').click()
        jq('.z-comboitem')[0].click()
        waitFor { jq('#result3').text() == 'item 1 selected' }
    }

}
