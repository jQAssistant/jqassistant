package com.buschmais.jqassistant.commandline.configuration;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;

@Description("The repositories for resolving plugins and their dependencies.")
@ConfigMapping(prefix = Repositories.PREFIX)
public interface Repositories {

    String PREFIX = "jqassistant.repositories";
    String LOCAL = "local";

    @Description("The path to the local repository.")
    Optional<File> local();

    @Description("The remote repositories.")
    Map<String, Remote> remotes();

}
