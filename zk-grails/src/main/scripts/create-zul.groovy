import grails.codegen.model.Model

description("Creates a new zul page") {
    usage "grails create-zul [ZUL PAGE NAME] [--template=template_vm.zul]"
    argument name: "zul page name", description: "The name of the zul to create"
    flag name: "template", description: "The path of the zul template relative to templates/artifacts/"
    flag name: "no-composer", description: "Doesn't create composer along zul file"
    flag name: "force", description: "Whether to overwrite existing files"
}

String zulTemplate = argsMap.template ?: "template.zul"
def name = args[0] - ".zul"
def overwrite = flag("force") as boolean

Model _model = model(name)

render(
    template: template("artifacts/zul/${zulTemplate}"),
    destination: file("grails-app/zul/${_model.packagePath}/${_model.propertyName}.zul"),
    model: _model,
    overwrite: overwrite
)

if (!(flag("no-composer") as boolean)) {
    createComposer(_model.fullName, overwrite ? "--force" : "")
}
