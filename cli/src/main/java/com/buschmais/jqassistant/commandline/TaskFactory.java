package com.buschmais.jqassistant.commandline;

import java.util.List;

public interface TaskFactory {

    /**
     * Determine the task to execute from the given name.
     *
     * @param name
     *            The name.
     * @return The task.
     */
    com.buschmais.jqassistant.commandline.Task fromName(String name) throws com.buschmais.jqassistant.commandline.CliExecutionException;

    List<com.buschmais.jqassistant.commandline.Task> getTasks();

    List<String> getTaskNames();

}
