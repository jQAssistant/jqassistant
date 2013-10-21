package com.buschmais.jqassistant.core.report.impl;

import com.buschmais.jqassistant.core.analysis.api.ExecutionListener;
import com.buschmais.jqassistant.core.analysis.api.ExecutionListenerException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractExecutable;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;

/**
 * A {@link com.buschmais.jqassistant.core.analysis.api.ExecutionListener}
 * implementation which delegates all method calls to the
 * {@link com.buschmais.jqassistant.core.analysis.api.ExecutionListener}s
 * provided by the {@link Iterable} constructor argument.
 */
public class CompositeReportWriter implements ExecutionListener {

	private static interface DelegateOperation {
		void run(ExecutionListener reportWriter) throws ExecutionListenerException;
	}

	private Iterable<ExecutionListener> reportWriters;

	public CompositeReportWriter(Iterable<ExecutionListener> reportWriters) {
		this.reportWriters = reportWriters;
	}

	@Override
	public void begin() throws ExecutionListenerException {
		run(new DelegateOperation() {
			@Override
			public void run(ExecutionListener reportWriter) throws ExecutionListenerException {
				reportWriter.begin();
			}
		});
	}

	@Override
	public void end() throws ExecutionListenerException {
		run(new DelegateOperation() {
			@Override
			public void run(ExecutionListener reportWriter) throws ExecutionListenerException {
				reportWriter.end();
			}
		});
	}

	@Override
	public void beginConcept(final Concept concept) throws ExecutionListenerException {
		run(new DelegateOperation() {
			@Override
			public void run(ExecutionListener reportWriter) throws ExecutionListenerException {
				reportWriter.beginConcept(concept);
			}
		});
	}

	@Override
	public void endConcept() throws ExecutionListenerException {
		run(new DelegateOperation() {
			@Override
			public void run(ExecutionListener reportWriter) throws ExecutionListenerException {
				reportWriter.endConcept();
			}
		});
	}

	@Override
	public void beginGroup(final Group group) throws ExecutionListenerException {
		run(new DelegateOperation() {
			@Override
			public void run(ExecutionListener reportWriter) throws ExecutionListenerException {
				reportWriter.beginGroup(group);
			}
		});
	}

	@Override
	public void endGroup() throws ExecutionListenerException {
		run(new DelegateOperation() {
			@Override
			public void run(ExecutionListener reportWriter) throws ExecutionListenerException {
				reportWriter.endGroup();
			}
		});
	}

	@Override
	public void beginConstraint(final Constraint constraint) throws ExecutionListenerException {
		run(new DelegateOperation() {
			@Override
			public void run(ExecutionListener reportWriter) throws ExecutionListenerException {
				reportWriter.beginConstraint(constraint);
			}
		});
	}

	@Override
	public void endConstraint() throws ExecutionListenerException {
		run(new DelegateOperation() {
			@Override
			public void run(ExecutionListener reportWriter) throws ExecutionListenerException {
				reportWriter.endConstraint();
			}
		});
	}

	@Override
	public void setResult(final Result<? extends AbstractExecutable> result) throws ExecutionListenerException {
		run(new DelegateOperation() {
			@Override
			public void run(ExecutionListener reportWriter) throws ExecutionListenerException {
				reportWriter.setResult(result);
			}
		});
	}

	private void run(DelegateOperation operation) throws ExecutionListenerException {
		for (ExecutionListener reportWriter : reportWriters) {
			operation.run(reportWriter);
		}
	}
}
