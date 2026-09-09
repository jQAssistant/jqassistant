def reportFile = new File(basedir, 'project/target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new groovy.xml.XmlSlurper().parse(reportFile)
def itGroup = jqassistantReport.group.find { it.@id = 'it' }
assert itGroup.concept.find { it.@id == 'it:Concept1' }.status == 'success'
assert itGroup.concept.find { it.@id == 'it:Concept2' }.status == 'success'
