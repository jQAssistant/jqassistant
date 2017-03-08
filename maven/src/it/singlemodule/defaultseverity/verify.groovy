assert new File(basedir, 'target/jqassistant/jqassistant-report.xml').exists()
def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new XmlSlurper().parse(reportFile)
def defaultGroup = jqassistantReport.group.find { it.@id = 'default' }
def constraint = defaultGroup.constraint.find { it.@id == 'severity:Blocker' }
assert constraint.status == 'failure'
assert constraint.severity == 'info'
