def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()

def xmlSlurper = new groovy.xml.XmlSlurper()

def jqassistantReport = xmlSlurper.parse(reportFile)
def itGroup = jqassistantReport.group.find { it.@id = 'it' }
def includedConstraint = itGroup.constraint.find { it.@id == 'it:IncludedConstraint' }
assert includedConstraint.status == 'success'

def baselineFile = new File(basedir, 'jqassistant/jqassistant-baseline.xml')
assert baselineFile.exists()
def jqassistantBaseline = xmlSlurper.parse(baselineFile)

def jqassistantBaselineConstraints = jqassistantBaseline.constraint
assert jqassistantBaselineConstraints.size() == 1
def includedConstraintBaseline = jqassistantBaselineConstraints.find { it.@id == 'it:IncludedConstraint' }
assert includedConstraintBaseline.row.size() == 1

def jqassistantBaselineConcepts = jqassistantBaseline.concept
assert jqassistantBaselineConcepts.size() == 1
def includedConceptBaseline = jqassistantBaselineConcepts.find { it.@id == 'it:IncludedConcept' }
assert includedConceptBaseline.row.size() == 1
