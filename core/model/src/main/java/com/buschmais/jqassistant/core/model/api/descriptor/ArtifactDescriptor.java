package com.buschmais.jqassistant.core.model.api.descriptor;

/**
 * Describes a Maven artifact.
 *
 * @author Herklotz
 */
public class ArtifactDescriptor extends ParentDescriptor {

	private String group;
	private String artifact;
	private String version;

	/**
	 * @return the groupIdProperty
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @param group
	 *            the groupIdProperty to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * @return the artifactIdProperty
	 */
	public String getArtifact() {
		return artifact;
	}

	/**
	 * @param artifact
	 *            the artifactIdProperty to set
	 */
	public void setArtifact(String artifact) {
		this.artifact = artifact;
	}

	/**
	 * @return the versionProperty
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the versionProperty to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

}
