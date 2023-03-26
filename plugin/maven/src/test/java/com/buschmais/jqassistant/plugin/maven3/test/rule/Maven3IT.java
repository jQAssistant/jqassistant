package com.buschmais.jqassistant.plugin.maven3.test.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDirectoryDescriptor;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.test.matcher.ResultMatcher.result;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class Maven3IT extends AbstractPluginIT {

    @Test
    void hierarchicalParentModuleRelation() throws Exception {
        store.beginTransaction();
        MavenProjectDirectoryDescriptor parent = store.create(MavenProjectDirectoryDescriptor.class);
        MavenProjectDirectoryDescriptor module1 = store.create(MavenProjectDirectoryDescriptor.class);
        MavenProjectDirectoryDescriptor module2 = store.create(MavenProjectDirectoryDescriptor.class);
        parent.getModules().add(module1);
        parent.getModules().add(module2);
        module1.setParent(parent);
        store.commitTransaction();
        assertThat(validateConstraint("maven3:HierarchicalParentModuleRelation").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportPlugin.getConstraintResults().values());
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat(result, result(constraint("maven3:HierarchicalParentModuleRelation")));
        List<Row> rows = result.getRows();
        assertThat(rows.size(), equalTo(1));
        Map<String, Column<?>> row = rows.get(0).getColumns();
        Column invalidModuleColumn = row.get("InvalidModule");
        assertThat((MavenProjectDirectoryDescriptor) invalidModuleColumn.getValue(), equalTo(module2));
        store.commitTransaction();
    }

}
