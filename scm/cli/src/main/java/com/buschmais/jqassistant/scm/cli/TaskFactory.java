package com.buschmais.jqassistant.scm.cli;

import java.util.List;

public interface TaskFactory {

    /**
     * Determine the task to execute from the given name.
     *
     * @param name
     *            The name.
     * @return The task.
     */
    Task fromName(String name) throws CliExecutionException;

    List<Task> getTasks();

    List<String> getTaskNames();

}
