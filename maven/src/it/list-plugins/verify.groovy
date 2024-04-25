def buildLog = new File(basedir, 'build.log')
assert buildLog.exists()

def text = buildLog.getText();

verify(text, "jQAssistant Common Plugin", "jqa.plugin.common")
verify(text, "jQAssistant Core Analysis Plugin", "jqa.core.analysis.plugin")
verify(text, "jQAssistant Core Report Plugin", "jqa.core.report.plugin")
verify(text, "jQAssistant Java Plugin", "jqa.plugin.java")
verify(text, "jQAssistant JSON Plugin", "jqa.plugin.json")
verify(text, "jQAssistant JUnit Plugin", "jqa.plugin.junit")
verify(text, "jQAssistant Maven 3 Plugin", "jqa.plugin.maven3")
verify(text, "jQAssistant XML Plugin", "jqa.plugin.xml")
verify(text, "jQAssistant YAML 2 Plugin", "jqa.plugin.yaml2")

def verify(text, expectedName, expectedId) {
    assert text.contains(expectedName);
    assert text.contains("[" + expectedId + "]");
}
