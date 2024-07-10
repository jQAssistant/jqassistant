def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new groovy.xml.XmlSlurper().parse(reportFile)
def itGroup = jqassistantReport.group.find { it.@id = 'it' }
assert itGroup.concept.find { it.@id == 'it:ExpectedFiles' }.status == "success"

