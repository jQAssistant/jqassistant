def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def report = new groovy.xml.XmlSlurper().parse(reportFile)
assert report.group.constraint.find { it.@id == "it:MavenModules" } != null
assert report.group.constraint.find { it.@id == "it:MavenModulesWithModel" } != null
