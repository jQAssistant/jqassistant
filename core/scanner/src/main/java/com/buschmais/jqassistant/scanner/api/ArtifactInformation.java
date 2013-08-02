/**
 *
 */
package com.buschmais.jqassistant.scanner.api;

/**
 * Contains informations about the scanned artifact.
 *
 * @author Herklotz
 */
public class ArtifactInformation {

	private String groupId;
	private String artifactId;
	private String version;

	/**
	 * Constructor.
	 *
	 * @param groupId
	 *            the groupId
	 * @param artifactId
	 *            the artifactId
	 * @param version
	 *            the version
	 */
	public ArtifactInformation(String groupId, String artifactId, String version) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}

	/**
	 * Constructor.
	 */
	public ArtifactInformation() {
	}

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId
	 *            the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the artifactId
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * @param artifactId
	 *            the artifactId to set
	 */
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

}
