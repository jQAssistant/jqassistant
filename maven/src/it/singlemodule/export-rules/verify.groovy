def rulesFile = new File(basedir, 'target/jqassistant/jqassistant-rules.xml')
assert rulesFile.exists()
def rulesNode = new XmlSlurper().parse(rulesFile)
assert rulesNode.concept.size() == 8
assert rulesNode.constraint.size() == 2
assert rulesNode.group.size() == 1
