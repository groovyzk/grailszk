package jquery

class TextComposer extends zk.grails.Composer {

    def afterCompose = { wnd ->
        //
        // Select nothing
        //
        try {
            $('#result').val("result: " +  $('#nothing').text())
        } catch(ignored) {
            $('#result').val("result: ")
        }

        //
        // label can use both .text() and .val() to get label's value
        //
        $('#result1').val("result: " +  $('#label').text())
        $('#result2').val("result: " +  $('#label').val())

        //
        // button can get label via .text()
        // but cannot use .val()
        //
        $('#result3').val("result: " +  $('#button').text())
        try {
            $('#result4').val("result: " +  $('#button').val())
        } catch (ignored) {
            $('#result4').val("button does not have value attr")
        }
    }

}
