package com.buschmais.jqassistant.core.report.impl;

import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;

/**
 * A {@link ReportPlugin}
 * implementation collection the concept results and constraint violations
 * in-memory.
 */
@Deprecated
@ToBeRemovedInVersion(major = 1, minor = 5)
public class InMemoryReportWriter extends InMemoryReportPlugin {

    public InMemoryReportWriter(ReportPlugin delegate) {
        super(delegate);
    }
}
