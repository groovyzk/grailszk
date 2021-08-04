package route

class RouteComposer extends zk.grails.Composer {

    def routing = {

        hashtag("/project/:id") {
            $('#result').val("Project: ${params["id"]}")
        }

        hashtag("/blog/:blogId") { blogId ->
            $('#result').val("Blog: ${blogId}")
        }

    }

    def afterCompose = { wnd ->
        $('#result').val("Ready")
    }

}