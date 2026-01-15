assert new File(basedir, 'target/jqassistant/jqassistant-report.xml').exists()
assert !new File(basedir, 'target/jqassistant/jqassistant-report.html').exists()
assert new File(basedir, 'target/jqassistant-report.xml').exists()
assert new File(basedir, 'target/jqassistant-report.html').exists()
