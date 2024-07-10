def rulesFile = new File(basedir, 'target/jqassistant/jqassistant-rules.xml')
assert rulesFile.exists()
def rulesNode = new groovy.xml.XmlSlurper().parse(rulesFile)
def exportDefault = rulesNode.group.find { it.@id = 'export-default' }
assert exportDefault != null
assert exportDefault.includeConstraint.find { it.@refId == 'java:AvoidCyclicPackageDependencies' } != null
assert exportDefault.includeConstraint.find { it.@refId == 'java:AvoidCyclicArtifactDependencies' } != null
