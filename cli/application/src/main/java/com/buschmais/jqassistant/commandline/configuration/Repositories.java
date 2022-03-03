package com.buschmais.jqassistant.commandline.configuration;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

@Description("The repositories for resolving plugins and their dependencies.")
public interface Repositories {

    @Description("The path to the local repository.")
    Optional<File> local();

    @Description("The remote repositories.")
    Map<String, Remote> remotes();

}
