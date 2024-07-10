package com.buschmais.jqassistant.core.analysis.api.baseline;

import java.io.*;
import java.util.*;

import com.buschmais.jqassistant.core.shared.xml.JAXBHelper;

import lombok.RequiredArgsConstructor;
import org.jqassistant.schema.baseline.v2.ColumnType;
import org.jqassistant.schema.baseline.v2.JqassistantBaseline;
import org.jqassistant.schema.baseline.v2.RowType;
import org.jqassistant.schema.baseline.v2.RuleType;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@RequiredArgsConstructor
public class BaselineRepository {

    private static final JAXBHelper<JqassistantBaseline> JAXB_HELPER = new JAXBHelper<>(JqassistantBaseline.class);

    private final com.buschmais.jqassistant.core.analysis.api.configuration.Baseline configuration;

    private final File ruleDirectory;

    public Optional<Baseline> read() {
        File baselineFile = getFile();
        if (baselineFile.exists()) {
            return of(read(baselineFile));
        }
        return empty();
    }

    public void write(Baseline baseline) {
        write(baseline, getFile());
    }

    private File getFile() {
        return configuration.file()
            .map(File::new)
            .orElse(new File(ruleDirectory, "jqassistant-baseline.xml"));
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
        toBaseline(jqassistantBaseline.getConstraint(), baseline.getConstraints());
        toBaseline(jqassistantBaseline.getConcept(), baseline.getConcepts());
        return baseline;
    }

    private static void toBaseline(List<RuleType> ruleTypes, SortedMap<String, Baseline.RuleBaseline> ruleBaselines) {
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
        try {
            JAXB_HELPER.marshal(jqassistantBaseline, new FileOutputStream(baselinefile));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write baseline file " + baselinefile, e);
        }
    }

    private static JqassistantBaseline fromBaseline(Baseline baseline) {
        JqassistantBaseline jqassistantBaseline = new JqassistantBaseline();
        fromBaseline(baseline.getConstraints(), jqassistantBaseline.getConstraint());
        fromBaseline(baseline.getConcepts(), jqassistantBaseline.getConcept());
        return jqassistantBaseline;
    }

    private static void fromBaseline(SortedMap<String, Baseline.RuleBaseline> ruleBaselines, List<RuleType> ruleTypes) {
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
                ruleTypes.add(ruleType);
            }
        }
    }
}
