def reportFile1 = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile1.exists()
def jqassistantReport = new groovy.xml.XmlSlurper().parse(reportFile1)
def defaultGroup = jqassistantReport.group.find { it.@id = 'it' }
def concept = defaultGroup.concept.find { it.@id == 'it:ProjectContainsApplicationXML' }
assert concept.status == 'success'
