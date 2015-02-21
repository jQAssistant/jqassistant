def rulesFile = new File(basedir, 'target/jqassistant/jqassistant-rules.xml')
assert rulesFile.exists()
def rulesNode = new XmlSlurper().parse(rulesFile)
assert rulesNode.group.size() == 2 // "default", "junit4:Default"
assert rulesNode.constraint.size() >= 2
assert rulesNode.concept.size() > 0
