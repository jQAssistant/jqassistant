/**
 * Copyright (C) 2011 tdarby <tim.darby.uk@googlemail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.buschmais.jqassistant.mojo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.core.analysis.api.ExecutionListener;
import com.buschmais.jqassistant.core.analysis.api.ExecutionListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractExecutable;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.report.impl.CompositeReportWriter;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.report.impl.XmlReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;

/**
 * The analyze Mojo runs analysis according to the defined rules.
 */
@Mojo(name = "analyze", defaultPhase = LifecyclePhase.VERIFY)
public class AnalyzeMojo extends AbstractAnalysisAggregatorMojo {

	/**
	 * Indicates if the plugin shall fail if a constraint violation is detected.
	 */
	@Parameter(property = "jqassistant.failOnConstraintViolations", defaultValue = "true")
	protected boolean failOnConstraintViolations;

	@Override
	public void aggregate(MavenProject baseProject, Set<MavenProject> projects)
			throws MojoExecutionException, MojoFailureException {
		getLog().info("Executing analysis for '" + baseProject.getName() + "'.");
		final RuleSet ruleSet = resolveEffectiveRules(baseProject);
		InMemoryReportWriter inMemoryReportWriter = new InMemoryReportWriter();
		FileWriter xmlReportFileWriter;
		try {
			xmlReportFileWriter = new FileWriter(getXmlReportFile(baseProject));
		} catch (IOException e) {
			throw new MojoExecutionException("Cannot create XML report file.",
					e);
		}
		XmlReportWriter xmlReportWriter;
		try {
			xmlReportWriter = new XmlReportWriter(xmlReportFileWriter);
		} catch (ExecutionListenerException e) {
			throw new MojoExecutionException(
					"Cannot create XML report file writer.", e);
		}
		List<ExecutionListener> reportWriters = new LinkedList<>();
		reportWriters.add(inMemoryReportWriter);
		reportWriters.add(xmlReportWriter);
		try {
			final CompositeReportWriter reportWriter = new CompositeReportWriter(
					reportWriters);
			execute(baseProject, new StoreOperation<Void>() {
				@Override
				public Void run(Store store) throws MojoExecutionException {
					Analyzer analyzer = new AnalyzerImpl(store, reportWriter);
					try {
						analyzer.execute(ruleSet);
					} catch (AnalyzerException e) {
						throw new MojoExecutionException("Analysis failed.", e);
					}
					return null;
				}
			});
		} finally {
			IOUtils.closeQuietly(xmlReportFileWriter);
		}
		verifyConceptResults(inMemoryReportWriter);
		verifyConstraintViolations(inMemoryReportWriter);
	}

	/**
	 * Verifies the concept results returned by the {@link InMemoryReportWriter}
	 * .
	 * <p>
	 * A warning is logged for each concept which did not return a result (i.e.
	 * has not been applied).
	 * </p>
	 * 
	 * @param inMemoryReportWriter
	 *            The {@link InMemoryReportWriter}.
	 */
	private void verifyConceptResults(InMemoryReportWriter inMemoryReportWriter) {
		List<Result<Concept>> conceptResults = inMemoryReportWriter
				.getConceptResults();
		for (Result<Concept> conceptResult : conceptResults) {
			if (conceptResult.getRows().isEmpty()) {
				getLog().warn(
						"Concept '" + conceptResult.getExecutable().getId()
								+ "' returned an empty result.");
			}
		}
	}

	/**
	 * Verifies the constraint violations returned by the
	 * {@link InMemoryReportWriter}.
	 * 
	 * @param inMemoryReportWriter
	 *            The {@link InMemoryReportWriter}.
	 * @throws MojoFailureException
	 *             If constraint violations are detected.
	 */
	private void verifyConstraintViolations(
			InMemoryReportWriter inMemoryReportWriter)
			throws MojoFailureException {
		List<Result<Constraint>> constraintViolations = inMemoryReportWriter
				.getConstraintViolations();
		int violations = 0;
		for (Result<Constraint> constraintViolation : constraintViolations) {
			if (!constraintViolation.isEmpty()) {
				AbstractExecutable constraint = constraintViolation
						.getExecutable();
				getLog().error(
						constraint.getId() + ": " + constraint.getDescription());
				for (Map<String, Object> columns : constraintViolation
						.getRows()) {
					StringBuilder message = new StringBuilder();
					for (Map.Entry<String, Object> entry : columns.entrySet()) {
						if (message.length() > 0) {
							message.append(", ");
						}
						message.append(entry.getKey());
						message.append('=');
						Object value = entry.getValue();
						message.append(value instanceof FullQualifiedNameDescriptor ? ((FullQualifiedNameDescriptor) value)
								.getFullQualifiedName() : value.toString());
					}
					getLog().error("  " + message.toString());
				}
				violations++;
			}
		}
		if (failOnConstraintViolations && violations > 0) {
			throw new MojoFailureException(violations
					+ " constraints have been violated!");
		}
	}

	/**
	 * Returns the {@link File} to write the XML report to.
	 * 
	 * @return The {@link File} to write the XML report to.
	 * @throws MojoExecutionException
	 *             If the file cannot be determined.
	 */
	private File getXmlReportFile(MavenProject baseProject)
			throws MojoExecutionException {
		File selectedXmlReportFile = BaseProjectResolver.getReportFile(
				baseProject, xmlReportFile, REPORT_XML);
		selectedXmlReportFile.getParentFile().mkdirs();
		return selectedXmlReportFile;
	}
}