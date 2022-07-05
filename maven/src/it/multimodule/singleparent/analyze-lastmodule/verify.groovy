def reportFile = new File(basedir, 'build.log')
boolean foundModule1Violation = false
boolean foundModule2Violation = false
boolean foundModule3Violation = false
boolean foundModule4Violation = false
reportFile.eachLine {
    if (!foundModule1Violation) {
        foundModule1Violation = isViolationLogLineForModule('module1', it)
    }
    if (!foundModule2Violation) {
        foundModule2Violation = isViolationLogLineForModule('module2', it)
    }
    if (!foundModule3Violation) {
        foundModule3Violation = isViolationLogLineForModule('module3', it)
    }
    if (!foundModule4Violation) {
        foundModule4Violation = isViolationLogLineForModule('module4', it)
    }
}
assert !foundModule1Violation // Skipped - should not analyze yet
assert !foundModule2Violation // Skipped - should not analyze yet
assert !foundModule3Violation // Not the last module - should not analyze yet
assert foundModule4Violation // Should finally analyze and find the violation

static boolean isViolationLogLineForModule(String moduleName, String line) {
    line ==~ /^\[ERROR\] Failed to execute goal com.buschmais.jqassistant:jqassistant-maven-plugin:.* on project jqassistant-maven-plugin.integration.multimodule.singleparent.analyze-lastmodule.$moduleName: Failed rules detected:.*$/
}
