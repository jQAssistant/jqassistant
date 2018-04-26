def reportFile = new File(basedir, 'target/surefire-reports/TEST-jQAssistant-default.xml')
assert reportFile.exists()
def testsuiteNode = new XmlSlurper().parse(reportFile)
assert testsuiteNode.testcase.size() > 0
