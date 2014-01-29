package com.buschmais.jqassistant.core.scanner.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.FileScanner;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;

/**
 * Implementation of the {@link FileScanner}.
 */
public class FileScannerImpl implements FileScanner {

	private abstract class AbstractIterable<E> implements Iterable<Descriptor> {

		protected abstract boolean hasNextElement();

		protected abstract E nextElement();

		protected abstract boolean isDirectory(E element);

		protected abstract String getName(E element);

		protected abstract InputStream openInputStream(String name, E element) throws IOException;

		protected abstract void close() throws IOException;

		@Override
		public Iterator<Descriptor> iterator() {
			return new Iterator<Descriptor>() {

				private Descriptor next = null;

				@Override
				public boolean hasNext() {
					try {
						while (next == null && hasNextElement()) {
							E element = nextElement();
							for (FileScannerPlugin plugin : plugins) {
								String name = getName(element);
								boolean isDirectory = isDirectory(element);
								if (plugin.matches(name, isDirectory)) {
									if (LOGGER.isInfoEnabled()) {
										LOGGER.info("Scanning '{}'", name);
									}
                                    next = doScan(element, plugin, name, isDirectory);
                                }
							}
						}
						if (next != null) {
							return true;
						}
						close();
						return false;
					} catch (IOException e) {
						throw new IllegalStateException("Cannot iterate over elements.", e);
					}
				}

                private Descriptor doScan(E element, FileScannerPlugin plugin, String name, boolean directory) throws IOException {
                    try {
                        if (directory) {
                            return plugin.scanDirectory(store, name);
                        } else {
                            BufferedInputStream inputStream = new BufferedInputStream(openInputStream(name, element));
                            StreamSource streamSource = new StreamSource(inputStream, name);
                            Descriptor descriptor = plugin.scanFile(store, streamSource);
                            inputStream.close();
                            return descriptor;
                        }
                    } catch(Exception e) {
                        throw new IOException("Error scanning "+name,e);
                    }
                }

                @Override
				public Descriptor next() {
					if (hasNext()) {
						Descriptor result = next;
						next = null;
						return result;
					}
					throw new NoSuchElementException("No more results.");
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("Cannot remove element.");
				}
			};
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(FileScannerImpl.class);

	private final Store store;
	private final Collection<FileScannerPlugin<?>> plugins;

	/**
	 * Constructor.
	 * 
	 * @param plugins
	 *            The
	 *            {@link com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin}
	 *            s to use for scanning.
	 */
	public FileScannerImpl(Store store, Collection<FileScannerPlugin<?>> plugins) {
		this.store = store;
		this.plugins = plugins;
	}

	@Override
	public Iterable<Descriptor> scanArchive(File archive) throws IOException {
		if (!archive.exists()) {
			throw new IOException("Archive '" + archive.getAbsolutePath() + "' not found.");
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Scanning archive '{}'.", archive.getAbsolutePath());
		}
		final ZipFile zipFile = new ZipFile(archive);
		final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
		return new AbstractIterable<ZipEntry>() {
			@Override
			protected boolean hasNextElement() {
				return zipEntries.hasMoreElements();
			}

			@Override
			protected ZipEntry nextElement() {
				return zipEntries.nextElement();
			}

			@Override
			protected boolean isDirectory(ZipEntry element) {
				return element.isDirectory();
			}

			@Override
			protected String getName(ZipEntry element) {
				return element.getName();
			}

			@Override
			protected InputStream openInputStream(String fileName, ZipEntry element) throws IOException {
				return zipFile.getInputStream(element);
			}

			@Override
			protected void close() throws IOException {
				zipFile.close();
			}
		};
	}

	@Override
	public Iterable<Descriptor> scanDirectory(File directory) throws IOException {
		final List<File> files = new ArrayList<>();
		new DirectoryWalker<File>() {

			@Override
			protected boolean handleDirectory(File directory, int depth, Collection<File> results) throws IOException {
				results.add(directory);
				return true;
			}

			@Override
			protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
				results.add(file);
			}

			public void scan(File directory) throws IOException {
				super.walk(directory, files);
			}
		}.scan(directory);
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Scanning directory '{}' [{} files].", directory.getAbsolutePath(), files.size());
		}
		return scanFiles(directory, files);
	}

	@Override
	public Iterable<Descriptor> scanFiles(File directory, List<File> files) {
		final URI directoryURI = directory.toURI();
		final Iterator<File> iterator = files.iterator();
		return new AbstractIterable<File>() {
			@Override
			protected boolean hasNextElement() {
				return iterator.hasNext();
			}

			@Override
			protected File nextElement() {
				return iterator.next();
			}

			@Override
			protected boolean isDirectory(File element) {
				return element.isDirectory();
			}

			@Override
			protected String getName(File element) {
				String name = directoryURI.relativize(element.toURI()).toString();
				if (element.isDirectory()) {
					if (!StringUtils.isEmpty(name)) {
						return name.substring(0, name.length() - 1);
					}
					return name;
				} else {
					return name;
				}
			}

			@Override
			protected InputStream openInputStream(String fileName, File element) throws IOException {
				return new FileInputStream(element);
			}

			@Override
			protected void close() throws IOException {
			}
		};
	}

	@Override
	public Iterable<Descriptor> scanClasses(final Class<?>... classes) throws IOException {
		return new AbstractIterable<Class<?>>() {
			int index = 0;

			@Override
			protected boolean hasNextElement() {
				return index < classes.length;
			}

			@Override
			protected Class<?> nextElement() {
				return classes[index++];
			}

			@Override
			protected boolean isDirectory(Class<?> element) {
				return false;
			}

			@Override
			protected String getName(Class<?> element) {
				return "/" + element.getName().replace('.', '/') + ".class";
			}

			@Override
			protected InputStream openInputStream(String fileName, Class<?> element) throws IOException {
				return element.getResourceAsStream(fileName);
			}

			@Override
			protected void close() throws IOException {
			}
		};
	}

	@Override
	public Iterable<Descriptor> scanURLs(final URL... urls) throws IOException {
		return new AbstractIterable<URL>() {
			int index = 0;

			@Override
			protected boolean hasNextElement() {
				return index < urls.length;
			}

			@Override
			protected URL nextElement() {
				return urls[index++];
			}

			@Override
			protected boolean isDirectory(URL element) {
				return false;
			}

			@Override
			protected String getName(URL element) {
				return element.getPath();
			}

			@Override
			protected InputStream openInputStream(String fileName, URL element) throws IOException {
				return element.openStream();
			}

			@Override
			protected void close() throws IOException {
			}
		};
	}
}
