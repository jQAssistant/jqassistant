package com.buschmais.jqassistant.plugin.tycho.impl.scanner;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.io.resources.PlexusIoFileResourceCollection;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;
import org.codehaus.plexus.util.AbstractScanner;
import org.eclipse.tycho.core.TychoConstants;
import org.eclipse.tycho.core.facade.BuildProperties;
import org.eclipse.tycho.core.osgitools.project.EclipsePluginProject;

import com.buschmais.jqassistant.core.scanner.api.FileScanner;
import com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.store.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.AbstractMavenProjectScannerPlugin;

/**
 * Implementation of a {@link ProjectScannerPlugin} for tycho projects
 */
public class TychoProjectScannerPlugin extends AbstractMavenProjectScannerPlugin {

    private List<File> getPdeFiles() throws IOException {
        MavenProject project = getProject();
        Object value = project.getContextValue(TychoConstants.CTX_ECLIPSE_PLUGIN_PROJECT);
        final List<File> pdeFiles = new ArrayList<>();
        if (value instanceof EclipsePluginProject) {
            EclipsePluginProject pdeProject = (EclipsePluginProject) value;
            Iterator<PlexusIoResource> iterator = getPDEBinaries(project, pdeProject);
            if (iterator.hasNext()) {
                do {
                    PlexusIoResource resource = iterator.next();
                    File file = new File(resource.getURL().getFile());
                    if (file.exists() && !file.isDirectory()) {
                        pdeFiles.add(file);
                    }
                } while (iterator.hasNext());
            }
        }
        return pdeFiles;
    }

    @Override
    public void scan(FileScanner fileScanner) throws IOException {
        Store store = getStore();
        MavenProject project = getProject();
        store.beginTransaction();
        ArtifactDescriptor artifact = getArtifact(false);
        for (FileDescriptor fileDescriptor : fileScanner.scanFiles(project.getBasedir(), getPdeFiles())) {
            artifact.getContains().add(fileDescriptor);
        }
        store.commitTransaction();
    }

    private Iterator<PlexusIoResource> getPDEBinaries(MavenProject project, EclipsePluginProject pdeProject) throws IOException {
        BuildProperties buildProperties = pdeProject.getBuildProperties();
        return getResourceFileCollection(project.getBasedir(), buildProperties.getBinIncludes(), buildProperties.getBinExcludes()).getResources();
    }

    /**
     * @return a {@link PlexusIoFileResourceCollection} with the given includes
     *         and excludes and the configured default excludes. An empty list
     *         of includes leads to an empty collection.
     */
    protected PlexusIoFileResourceCollection getResourceFileCollection(File basedir, List<String> includes, List<String> excludes) {
        PlexusIoFileResourceCollection collection = new PlexusIoFileResourceCollection();
        collection.setBaseDir(basedir);
        if (includes.isEmpty()) {
            collection.setIncludes(new String[] { "" });
        } else {
            collection.setIncludes(includes.toArray(new String[includes.size()]));
        }
        Set<String> allExcludes = new LinkedHashSet<String>();
        if (excludes != null) {
            allExcludes.addAll(excludes);
        }
        allExcludes.addAll(Arrays.asList(AbstractScanner.DEFAULTEXCLUDES));
        collection.setExcludes(allExcludes.toArray(new String[allExcludes.size()]));
        return collection;
    }
}
