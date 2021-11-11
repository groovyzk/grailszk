description("Creates a new zul page and a new correspondent ViewModel") {
    usage "grails create-zul-view-model [ZUL/View NAME]"
    argument name: "zul page name", description: "The name of the zul to create"
    flag name: "force", description: "Whether to overwrite existing files"
}

def name = args[0]
def overwrite = flag('force') as boolean

createZul(name, "--no-composer", "--template=template_vm.zul", overwrite ? " --force" : "")
createViewModel(name, overwrite ? " --force" : "")
