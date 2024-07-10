def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new groovy.xml.XmlSlurper().parse(reportFile)
def defaultGroup = jqassistantReport.group.find { it.@id = 'default' }
assert defaultGroup.constraint.find { it.@id == 'test:UndefinedDependency' }.status == "success"
assert defaultGroup.constraint.find { it.@id == 'test:RedundantDependency' }.status == "success"
