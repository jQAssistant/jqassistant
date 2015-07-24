assert new File(basedir, 'parent/target/jqassistant').exists()
def reportFile = new File(basedir, 'parent/target/jqassistant/jqassistant-report.xml')
assert reportFile.exists()
