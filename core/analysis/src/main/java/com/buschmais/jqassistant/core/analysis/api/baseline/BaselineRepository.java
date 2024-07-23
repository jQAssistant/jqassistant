package com.buschmais.jqassistant.core.analysis.api.baseline;

import java.io.*;
import java.util.*;

import com.buschmais.jqassistant.core.shared.xml.JAXBHelper;

import lombok.extern.slf4j.Slf4j;
import org.jqassistant.schema.baseline.v2.ColumnType;
import org.jqassistant.schema.baseline.v2.JqassistantBaseline;
import org.jqassistant.schema.baseline.v2.RowType;
import org.jqassistant.schema.baseline.v2.RuleType;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Slf4j
public class BaselineRepository {

    private static final JAXBHelper<JqassistantBaseline> JAXB_HELPER = new JAXBHelper<>(JqassistantBaseline.class);

    private final File baselineFile;

    public BaselineRepository(com.buschmais.jqassistant.core.analysis.api.configuration.Baseline configuration, File ruleDirectory) {
        this.baselineFile = configuration.file()
            .map(File::new)
            .orElse(new File(ruleDirectory, "jqassistant-baseline.xml"));
    }

    public Optional<Baseline> read() {
        if (baselineFile.exists()) {
            log.info("Reading baseline from file '{}'.", baselineFile);
            return of(read(baselineFile));
        }
        log.info("Baseline file '{}' does not exist yet.", baselineFile);
        return empty();
    }

    public void write(Baseline baseline) {
        log.info("Writing baseline to file '{}'.", baselineFile);
        write(baseline, baselineFile);
    }

    private static Baseline read(File baselineFile) {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(baselineFile))) {
            return toBaseline(JAXB_HELPER.unmarshal(inputStream));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read baseline file " + baselineFile, e);
        }
    }

    private static Baseline toBaseline(JqassistantBaseline jqassistantBaseline) {
        Baseline baseline = new Baseline();
        toRuleBaselines(jqassistantBaseline.getConstraint(), baseline.getConstraints());
        toRuleBaselines(jqassistantBaseline.getConcept(), baseline.getConcepts());
        return baseline;
    }

    private static void toRuleBaselines(List<RuleType> ruleTypes, SortedMap<String, Baseline.RuleBaseline> ruleBaselines) {
        for (RuleType ruleType : ruleTypes) {
            Baseline.RuleBaseline ruleBaseline = new Baseline.RuleBaseline();
            for (RowType rowType : ruleType.getRow()) {
                SortedMap<String, String> columns = new TreeMap<>();
                for (ColumnType columnType : rowType.getColumn()) {
                    columns.put(columnType.getName(), columnType.getValue());
                }
                ruleBaseline.getRows()
                    .put(rowType.getKey(), columns);
            }
            ruleBaselines.put(ruleType.getId(), ruleBaseline);
        }
    }

    private static void write(Baseline baseline, File baselinefile) {
        JqassistantBaseline jqassistantBaseline = fromBaseline(baseline);
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(baselinefile))) {
            JAXB_HELPER.marshal(jqassistantBaseline, outputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write baseline file " + baselinefile, e);
        }
    }

    private static JqassistantBaseline fromBaseline(Baseline baseline) {
        JqassistantBaseline jqassistantBaseline = new JqassistantBaseline();
        fromRuleBaselines(baseline.getConstraints(), jqassistantBaseline.getConstraint());
        fromRuleBaselines(baseline.getConcepts(), jqassistantBaseline.getConcept());
        return jqassistantBaseline;
    }

    private static void fromRuleBaselines(SortedMap<String, Baseline.RuleBaseline> ruleBaselines, List<RuleType> ruleTypes) {
        for (Map.Entry<String, Baseline.RuleBaseline> ruleBaselineEntry : ruleBaselines.entrySet()) {
            String ruleId = ruleBaselineEntry.getKey();
            Baseline.RuleBaseline ruleBaseline = ruleBaselineEntry.getValue();
            RuleType ruleType = new RuleType();
            ruleType.setId(ruleId);
            for (Map.Entry<String, SortedMap<String, String>> rowEntry : ruleBaseline.getRows()
                .entrySet()) {
                String rowKey = rowEntry.getKey();
                SortedMap<String, String> columns = rowEntry.getValue();
                RowType rowType = new RowType();
                rowType.setKey(rowKey);
                for (Map.Entry<String, String> columnEntry : columns.entrySet()) {
                    ColumnType columnType = new ColumnType();
                    columnType.setName(columnEntry.getKey());
                    columnType.setValue(columnEntry.getValue());
                    rowType.getColumn()
                        .add(columnType);
                }
                ruleType.getRow()
                    .add(rowType);
            }
            ruleTypes.add(ruleType);
        }
    }
}
