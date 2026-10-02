def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new groovy.xml.XmlSlurper().parse(reportFile)
def defaultGroup = jqassistantReport.group.find { it.@id = 'it' }
assert defaultGroup.concept.find { it.@id == 'it:ProjectHasSourceDirectory' }.status == "success"
assert defaultGroup.concept.find { it.@id == 'it:ProjectHasTestSourceDirectory' }.status == "success"
assert defaultGroup.concept.find { it.@id == 'it:TypeHasSourceFile' }.status == "success"
