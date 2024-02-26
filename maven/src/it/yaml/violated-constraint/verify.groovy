def buildLog = new File(basedir, 'build.log')
assert buildLog.exists()

// The constraint must be validated
assert buildLog.getText().contains('[INFO] Validating constraint \'violatedConcept\' with severity: \'MAJOR\'.')

// As the code does not fulfill the constraint the build should fail.
assert buildLog.getText().contains('[INFO] BUILD FAILURE')

// The following lines should have been issued by jQA because of the
// violated constraint.
assert buildLog.getText().contains('[ERROR] --[ Constraint Violation ]-----------------------------------------')
assert buildLog.getText().contains('[ERROR] Constraint: violatedConcept')
assert buildLog.getText().contains('[ERROR] Severity: MAJOR')
assert buildLog.getText().contains('[ERROR] Number of rows: 1')
assert buildLog.getText().contains('[ERROR] foobar')
assert buildLog.getText().contains('[ERROR] c=com.buschmais.jqassistant.maven.it.yaml.violatedconstraint.AClass')

// Only one constraint should have been violated
assert buildLog.getText().contains('Failed rules detected: 0 concepts, 1 constraints')
