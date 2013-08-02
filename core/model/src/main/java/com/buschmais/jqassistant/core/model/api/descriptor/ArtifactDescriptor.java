package com.buschmais.jqassistant.core.model.api.descriptor;

/**
 * Describes a Maven artifact.
 *
 * @author Herklotz
 */
public class ArtifactDescriptor extends ParentDescriptor {

	private String groupIdProperty;
	private String artifactIdProperty;
	private String versionProperty;

	/**
	 * @return the groupIdProperty
	 */
	public String getGroupIdProperty() {
		return groupIdProperty;
	}

	/**
	 * @param groupIdProperty
	 *            the groupIdProperty to set
	 */
	public void setGroupIdProperty(String groupIdProperty) {
		this.groupIdProperty = groupIdProperty;
	}

	/**
	 * @return the artifactIdProperty
	 */
	public String getArtifactIdProperty() {
		return artifactIdProperty;
	}

	/**
	 * @param artifactIdProperty
	 *            the artifactIdProperty to set
	 */
	public void setArtifactIdProperty(String artifactIdProperty) {
		this.artifactIdProperty = artifactIdProperty;
	}

	/**
	 * @return the versionProperty
	 */
	public String getVersionProperty() {
		return versionProperty;
	}

	/**
	 * @param versionProperty
	 *            the versionProperty to set
	 */
	public void setVersionProperty(String versionProperty) {
		this.versionProperty = versionProperty;
	}

}
