def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new XmlSlurper().parse(reportFile)
def defaultGroup = jqassistantReport.group.find { it.@id = 'default' }
assert defaultGroup.concept.find { it.@id == 'test:SpringComponentByMetaAnnotation' }.status == "success"
assert defaultGroup.concept.find { it.@id == 'test:MavenArtifactDependsOnSpringArtifacts' }.status == "success"

def buildLog = new File(basedir, 'build.log')
assert buildLog.exists()
def log = buildLog.getText();
assert log.findAll(".*Entering .*/springframework/spring-core/").size() == 1
