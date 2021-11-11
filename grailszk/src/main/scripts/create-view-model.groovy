import grails.codegen.model.Model

description("Creates a new ViewModel") {
    usage "grails create-view-model [VIEW MODEL NAME]"
    argument name: "View Model Name", description: "The name of the view model to create"
    flag name: "force", description: "Whether to overwrite existing files"
}

String ARTIFACT_NAME = "ViewModel"
def name = args[0] - ARTIFACT_NAME + ARTIFACT_NAME
def overwrite = flag("force") as boolean

Model _model = model(name)

render(
    template: template("artifacts/${ARTIFACT_NAME}.groovy"),
    destination: file("grails-app/viewmodels/${_model.packagePath}/${_model.className}.groovy"),
    model: _model,
    overwrite: overwrite
)

createUnitTest(_model.fullName, overwrite ? "--force" : "")
