package com.buschmais.jqassistant.sonar.plugin.language;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.java.Java;

import com.buschmais.jqassistant.plugin.java.api.report.Java.JavaLanguageElement;
import com.buschmais.jqassistant.sonar.plugin.sensor.JQAssistantSensor;
import com.buschmais.jqassistant.sonar.plugin.sensor.LanguageResourceResolver;

/**
 * Implementation of a {@link com.buschmais.jqassistant.sonar.plugin.sensor.LanguageResourceResolver} for java elements.
 */
public class JavaResourceResolver implements LanguageResourceResolver, BatchExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(JQAssistantSensor.class);

	private final FileSystem fileSystem;

	public JavaResourceResolver(FileSystem moduleFileSystem) {
		this.fileSystem = moduleFileSystem;
	}

	@Override
	public String getLanguage() {
		return Java.KEY;
	}

	@Override
	public Resource resolve(Project project, String nodeType, String nodeSource, String nodeValue) {
		if (JavaLanguageElement.Type.name().equals(nodeType)) {
			final String javaFilePath = determineRelativeQualifiedJavaSourceFileName(nodeSource);
			return findMatchingResourceFile(javaFilePath);
		} else if (JavaLanguageElement.Field.name().equals(nodeType) || JavaLanguageElement.MethodInvocation.name().equals(nodeType)
				|| JavaLanguageElement.ReadField.name().equals(nodeType) || JavaLanguageElement.WriteField.name().equals(nodeType)
				|| JavaLanguageElement.MethodInvocation.name().equals(nodeType)) {
			//TODO: Using 'nodeSource' is working only on file level, can we identify a method... as resource?
			final String javaFilePath = determineRelativeQualifiedJavaSourceFileName(nodeSource);
			return findMatchingResourceFile(javaFilePath);
		} else if (JavaLanguageElement.Package.name().equals(nodeType)) {
			return findMatchingResourceDirectory(project, nodeValue.replace('.', '/'));
		}
		return null;
	}

	/**
	 * This resolver can find only resources in the current project, because only such resources are part of the 'index cache'.
	 *
	 * @return The matching resource or <code>null</code> if nothing was found and in case of multiple matches.
	 */
	private Resource findMatchingResourceFile(String javaFilePath)
	{
		//in SonarQ Java files have the prefix 'src/main/java' for Maven projects
		//we have to handle such nested project structures without specific knowledge about project structures... so use pattern matcher :-)
		Iterator<InputFile> files = fileSystem.inputFiles(fileSystem.predicates().matchesPathPattern("**/"+javaFilePath)).iterator();
		while(files.hasNext())
		{
			InputFile file = files.next();
			if(files.hasNext())
			{
				//ups, more entries?!
				LOGGER.error("Multiple matches for Java file {}, cannot handle source file for violations", javaFilePath);
				return null;
			}
			return org.sonar.api.resources.File.create(file.relativePath());
		}
		return null;
	}

	private Resource findMatchingResourceDirectory(Project project, String javaPackageDirPath)
	{
		//for packages (directories) exists no pattern matching api, so we have to check all available source directories for the package
		final ProjectFileSystem fs = project.getFileSystem();
		List<java.io.File> dirs = new ArrayList<>(2);
		dirs.addAll(fs.getSourceDirs());
		dirs.addAll(fs.getTestDirs());

		java.io.File packageDir;
		for(File dir: dirs)
		{
			packageDir = new java.io.File(dir, javaPackageDirPath);
			if(!packageDir.exists()) {
				continue;
			}
			final InputDir id = fileSystem.inputDir(packageDir);
			if(id != null) {
				return org.sonar.api.resources.Directory.fromIOFile(id.file(), project);
			}
		}
		return null;
	}

	/**
	 * Convert a given entry like <code>com/buschmais/jqassistant/examples/sonar/project/Bar.class</code> into a source file name like <code>com/buschmais/jqassistant/examples/sonar/project/Bar.java</code>.
	 */
	private String determineRelativeQualifiedJavaSourceFileName(String classFileName)
	{
		if(classFileName == null || classFileName.isEmpty()) {
			return null;
		}
		String result = classFileName;
		if(result.charAt(0) == '/') {
			result = result.substring(1);
		}
		if(result.toLowerCase(Locale.ENGLISH).endsWith(".class")) {
			result = result.substring(0, result.length() - ".class".length());
		}
		//remove nested class fragments
		int index = result.indexOf('$');
		if(index > -1){
			result = result.substring(0, index);
		}
		return result.concat(".java");
	}
}
