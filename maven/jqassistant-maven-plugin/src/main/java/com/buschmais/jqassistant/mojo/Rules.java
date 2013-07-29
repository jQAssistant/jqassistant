package com.buschmais.jqassistant.mojo;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dirk.mahler
 * Date: 24.06.13
 * Time: 18:25
 * To change this template use File | Settings | File Templates.
 */
public class Rules {

    /**
     * URLs pointing to rule descriptors.
     *
     * @parameter
     */
    protected List<URL> urls;

    /**
     * The directory to scan for rules.
     *
     * @parameter expression="${jqassistant.rules.rulesDirectory}"
     */
    protected File rulesDirectory;

    public List<URL> getUrls() {
        return urls;
    }

    public void setUrls(List<URL> urls) {
        this.urls = urls;
    }

    public File getRulesDirectory() {
        return rulesDirectory;
    }

    public void setRulesDirectory(File rulesDirectory) {
        this.rulesDirectory = rulesDirectory;
    }
}
