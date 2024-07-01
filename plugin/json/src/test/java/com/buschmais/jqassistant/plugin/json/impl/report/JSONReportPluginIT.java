package com.buschmais.jqassistant.plugin.json.impl.report;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.WARNING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class JSONReportPluginIT extends AbstractPluginIT {

    private static final File JSON_REPORT_DIRECTORY = new File("target/jqassistant/report/json/");

    @Test
    void singleColum() throws RuleException, IOException, JSONException {
        verify("json-it:JsonReportWithSingleColumn", "json-it_JsonReportWithSingleColumn.json", "[\"a\", \"b\"]");
    }

    @Test
    void multipleColums() throws RuleException, IOException, JSONException {
        verify("json-it:JsonReportWithMultipleColumns", "json-it_JsonReportWithMultipleColumns.json",
            "[{ key: \"key1\", value: \"value1\" }, { key: \"key2\", value: \"value2\" }]");
    }

    @Test
    void object() throws RuleException, IOException, JSONException {
        verify("json-it:JsonReportObject", "json-it_JsonReportObject.json",
            "[{ scalar: \"Scalar\", array: [ \"Array Item\"], object: { scalar: \"Embedded Object\"} }]");

    }

    @Test
    void emptyResult() throws RuleException {
        Result<Concept> result = applyConcept("json-it:EmptyResult");
        assertThat(result.getStatus()).isEqualTo(WARNING);
        assertThat(result.getRows()).isEmpty();
        assertThat(new File(JSON_REPORT_DIRECTORY, "json-it_EmptyResult.json")).doesNotExist();
    }

    private void verify(String conceptId, String expectedFileName, String expectedJsonContent) throws IOException, JSONException, RuleException {
        Result<Concept> result = applyConcept(conceptId);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        File reportFile = new File(JSON_REPORT_DIRECTORY, expectedFileName);
        assertThat(reportFile).exists();
        String json = readFileToString(reportFile, UTF_8);
        assertEquals(expectedJsonContent, json, true);
    }

}
