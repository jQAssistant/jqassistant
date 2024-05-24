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

    @Description("The mirrors to use for remote repositories.")
    Map<String, Mirror> mirrors();

    String LOCAL = "local";

    @Description("The path to the local repository.")
    Optional<File> local();

    @Description("The remote repositories.")
    Map<String, Remote> remotes();

    String IGNORE_TRANSITIVE_REPOSITORIES = "ignore-transitive-repositories";

    @Description("If true (default), ignore any repositories specified by transitive dependencies.")
    Optional<Boolean> ignoreTransitiveRepositories();

}
