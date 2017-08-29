def rulesFile = new File(basedir, 'target/jqassistant/jqassistant-rules.xml')
assert rulesFile.exists()
def rulesNode = new XmlSlurper().parse(rulesFile)
def exportDefault = rulesNode.group.find { it.@id = 'export-default' }
assert exportDefault != null
assert exportDefault.includeConstraint.find { it.@refId == 'dependency:PackageCycles' } != null
assert exportDefault.includeConstraint.find { it.@refId == 'dependency:ArtifactCycles' } != null
