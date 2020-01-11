def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new XmlSlurper().parse(reportFile)
def defaultGroup = jqassistantReport.group.find { it.@id = 'default' }
assert defaultGroup.concept.find { it.@id == 'test:SpringComponentByMetaAnnotation' }.status == "success"
assert defaultGroup.concept.find { it.@id == 'test:MavenArtifactDependsOnSpringArtifacts' }.status == "success"
