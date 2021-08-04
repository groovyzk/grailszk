package jquery

class BindEventComposer extends zk.grails.Composer {

    def afterCompose = { c ->
        //
        // Mouse Events
        //
        $('#button').on('click', {
            $('#result1').val('clicked')
        })

        $('#button').on('rightClick', {
            $('#result1').val('right clicked')
        })

        $('#button').on('doubleClick', {
            $('#result1').val('double clicked')
        })

        //
        // Keyboard Events
        //
        $('#text').on('ok', {
            $('#result2').val('enter pressed')
        })

        $('#text').on('cancel', {
            $('#result2').val('ESC pressed')
        })

        $('#text').on('ctrlKey', {
            $('#result2').val('Ctrl+F3 pressed')
        })

        //
        // Input Events
        //
        $('#text').on('focus',{
            $('#result2').val('focused')
        })

        $('#text').on('blur',{
            $('#result2').val('blurred')
        })

        /* Haven't tested yet
        $('#text').on('change',{
            $('#result2').val('change')
        })

        $('#text').on('changing',{
            $('#result2').val('changing')
        })

        $('#text').on('selection',{
            $('#result2').val('selection')
        })
        */

        //
        //
        //
        $('#combo').on('select',{
            $('#result3').val("item " + $(it).text() + " selected")
        })

        /* Haven't tested yet
        $('#combo').on('open',{

        })

        $('#combo').on('close',{

        })
        */

    }

}
