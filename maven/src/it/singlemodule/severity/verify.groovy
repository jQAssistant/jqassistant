def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.4.xml')
assert reportFile.exists()
def jqassistantReport = new XmlSlurper().parse(reportFile)
def defaultGroup = jqassistantReport.group.find { it.@id = 'default' }
assert defaultGroup.concept.find { it.@id == 'severity:Major' }.status == 'failure'
assert defaultGroup.constraint.find { it.@id == 'severity:Blocker' }.status == 'failure'
