package zk

class UrlMappings {

    static mappings = {
        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
