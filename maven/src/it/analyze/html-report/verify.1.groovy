def xmlReportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert xmlReportFile.exists()

def jqassistantReport = new groovy.xml.XmlSlurper().parse(xmlReportFile)
def itGroup = jqassistantReport.group.find { it.@id == 'it' }
def itConcept = itGroup.concept.find { it.@id == 'it:Concept' }
assert  itConcept.result.rows.@count == 1

def htmlReportFile = new File(basedir, 'target/jqassistant/jqassistant-report.html')
assert htmlReportFile.exists()


