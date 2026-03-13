def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new groovy.xml.XmlSlurper().parse(reportFile)
def itGroup = jqassistantReport.group.find { it.@id = 'it' }

assert itGroup.constraint.find { it.@id == 'it:ExcludedConstraint' } == ""

def constraint = itGroup.constraint.find { it.@id == 'it:Constraint' }
assert constraint.status == 'warning'
assert constraint.severity == 'minor'

