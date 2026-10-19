def buildLog = new File(basedir, 'build.log')
assert buildLog.exists()

assert buildLog.getText().contains("Applying concept 'concept' with severity: 'INFO'.")
assert buildLog.getText().contains("Validating constraint 'constraint' with severity: 'MAJOR'.")
assert buildLog.getText().contains("Applying concept 'custom-concept' with severity: 'INFO'.")

assert new File(basedir, "target/custom-jqassistant-report.xml").exists()
