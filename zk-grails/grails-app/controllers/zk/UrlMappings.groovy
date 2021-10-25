package zk

class UrlMappings {
    static excludes = ["/static/*"]

    static mappings = {
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
            }
        }
    }
}
