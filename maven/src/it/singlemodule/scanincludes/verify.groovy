def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new groovy.xml.XmlSlurper().parse(reportFile)
def defaultGroup = jqassistantReport.group.find { it.@id = 'default' }
assert defaultGroup.concept.find { it.@id == 'scanInclude:PropertiesFile' }.status == "success"
assert defaultGroup.concept.find { it.@id == 'scanInclude:PropertiesUrl' }.status == "success"
