def reportFile = new File(basedir, 'target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
def jqassistantReport = new groovy.xml.XmlSlurper().parse(reportFile)
def defaultGroup = jqassistantReport.group.find{ it.@id = 'default' }
def simpleResult =  defaultGroup.concept.find { it.@id == 'test:SimpleParameter' }.result
def mapResult = defaultGroup.concept.find { it.@id == 'test:MapParameter' }.result

assert simpleResult.rows.@count == 1
assert simpleResult.rows.row[0].column[0].value == "value0"
assert mapResult.rows.@count == 1
assert mapResult.rows.row[0].column[0].value == "value1"
assert mapResult.rows.row[0].column[1].value == "value2"

