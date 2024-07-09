def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new XmlSlurper().parse(reportFile)
def defaultGroup = jqassistantReport.group.find{ it.@id = 'default' }
assert defaultGroup.concept.find { it.@id == 'java:TypeAssignableFrom' }.status == "success"
assert new File(basedir, 'target/site/jqassistant.html').exists()
assert !new File(basedir, 'module1/target/site/jqassistant.html').exists()
assert !new File(basedir, 'module2/target/site/jqassistant.html').exists()

