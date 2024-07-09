def buildLog = new File(basedir, 'build.log')
assert buildLog.exists()

assert buildLog.getText().contains('[INFO] Applying concept \'oneConcept\' with severity: \'MINOR\'.')

