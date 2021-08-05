//
// This script is executed by Grails after plugin was installed to project.
// This script is a Gant script so you can use all special variables provided
// by Gant (such as 'baseDir' which points on project base dir). You can
// use 'ant' to access a global instance of AntBuilder
//
// For example you can create directory under project tree:
//
//    ant.mkdir(dir:"${basedir}/grails-app/jobs")
//

def installedFile = "${zkPluginDir}/.installed"
try {
    if(/*new File(installedFile).exists() ==*/ false) {
        ant.get(
            src:"http://j.mp/aaYauK",
            dest: installedFile,
            ignoreerrors: true
        )
    }
}catch(e){ /* do nothing */ }

//
// Copy zk.xml, if not exist
//
def targetFile = "${basedir}/web-app/WEB-INF/zk.xml"

if(! (new File(targetFile).exists())) {
    ant.copy(file:"${zkPluginDir}/scripts/zk.xml",
             todir:"${basedir}/web-app/WEB-INF/",
             overwrite: true)
}

//
// Copy ZK's logos - always overwrite
//
["grails_logo.png","zkpowered_l.png", "zkpowered_s.png"].each { f ->
  ant.copy(file:"${zkPluginDir}/web-app/ext/images/${f}",
           todir:"${basedir}/web-app/ext/images/",
           overwrite: true
  )
}

//
// issue #326 - removed ZkUrlMappings out of the conf
// and copy ZkUrlMappings.groovy only upon installation
//
if(new File("${basedir}/grails-app/conf/ZkUrlMappings.groovy").exists() == false) {
    ant.copy(file: "${zkPluginDir}/src/templates/ZkUrlMappings.groovy",
             todir:"${basedir}/grails-app/conf")
}
