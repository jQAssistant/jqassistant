package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 * Simple FileRessource for Maven artifacts.
 * 
 * @author pherklotz
 */
public class ArtifactFileResource implements FileResource {

	private final File file;
	private FileInputStream fis;

	/**
	 * Constructs a new object.
	 * 
	 * @param file
	 *            the file
	 */
	public ArtifactFileResource(File file) {
		this.file = file;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		if (fis != null) {
			fis.close();
			fis = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream createStream() throws IOException {
		if (fis != null) {
			close();
		}
		fis = new FileInputStream(getFile());

		return fis;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getFile() throws IOException {
		return this.file;
	}

}
