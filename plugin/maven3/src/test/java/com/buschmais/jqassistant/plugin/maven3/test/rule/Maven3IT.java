package com.buschmais.jqassistant.plugin.maven3.test.rule;

import static com.buschmais.jqassistant.core.analysis.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ResultMatcher.result;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDirectoryDescriptor;

public class Maven3IT extends AbstractPluginIT {

    @Test
    public void hierarchicalParentModuleRelation() throws AnalysisException {
        store.beginTransaction();
        MavenProjectDirectoryDescriptor parent = store.create(MavenProjectDirectoryDescriptor.class);
        MavenProjectDirectoryDescriptor module1 = store.create(MavenProjectDirectoryDescriptor.class);
        MavenProjectDirectoryDescriptor module2 = store.create(MavenProjectDirectoryDescriptor.class);
        parent.getModules().add(module1);
        parent.getModules().add(module2);
        module1.setParent(parent);
        store.commitTransaction();
        validateConstraint("maven3:HierarchicalParentModuleRelation");
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = reportWriter.getConstraintViolations();
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat(result, result(constraint("maven3:HierarchicalParentModuleRelation")));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(1));
        Map<String, Object> row = rows.get(0);
        MavenProjectDirectoryDescriptor invalidModule = (MavenProjectDirectoryDescriptor) row.get("InvalidModule");
        assertThat(invalidModule, equalTo(module2));
        store.commitTransaction();
    }

}
