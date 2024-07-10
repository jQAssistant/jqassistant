def buildLog = new File(basedir, 'build.log')
assert buildLog.exists()

assert buildLog.getText().contains('[WARNING] Rule \'it:DeprecatedConcept\' is deprecated: This is a deprecated concept')
assert buildLog.getText().contains('[WARNING] Rule \'it:DeprecatedConstraint\' is deprecated: This is a deprecated constraint')
