import grails.codegen.model.Model

description("Creates a new Facade") {
    usage "grails create-facade [FACADE NAME]"
    argument name: "Facade Name", description: """\
The name of the facade to create.
Usually it takes the same name as the corresponding model"""
    flag name: "force", description: "Whether to overwrite existing files"
}

String ARTIFACT_NAME = "Facade"
def name = args[0] - ARTIFACT_NAME + ARTIFACT_NAME
def overwrite = flag("force") as boolean

Model _model = model(name)

render(
    template: template("artifacts/${ARTIFACT_NAME}.groovy"),
    destination: file("grails-app/facade/${_model.packagePath}/${_model.className}.groovy"),
    model: _model.asMap() + [
        domainName: _model.className - ARTIFACT_NAME,
    ],
    overwrite: overwrite
)

createUnitTest(_model.fullName, overwrite ? "--force" : "")
