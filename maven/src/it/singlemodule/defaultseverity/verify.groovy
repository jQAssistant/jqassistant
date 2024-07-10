def reportFile1 = new File(basedir, 'target/jqassistant/jqassistant-report.1.xml')
assert reportFile1.exists()
def jqassistantReport = new groovy.xml.XmlSlurper().parse(reportFile1)
def defaultGroup = jqassistantReport.group.find { it.@id = 'default' }
def constraint = defaultGroup.constraint.find { it.@id == 'severity:Blocker' }
assert constraint.status == 'success'
assert constraint.severity == 'info'
