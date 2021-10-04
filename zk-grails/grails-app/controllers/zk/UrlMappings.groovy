package zk

class UrlMappings {
    static excludes = ["/static/*"]

    static mappings = {
        "/"(view: "/index")
        "500"(view: "/error")
        "404"(view: "/notFound")
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }
    }
}
