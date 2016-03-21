package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

/**
 * Descriptor for a person participating taking part in
 * a project as contributor or developer.
 *
 * @see MavenDeveloperDescriptor
 * @see MavenContributorDescriptor
 */
@Label("Participant")
public interface MavenProjectParticipantDescriptor extends MavenDescriptor {
    /**
     * Get the name of the participant.
     *
     * @return The name.
     */
    @Property("name")
    String getName();

    /**
     * Set the name of the participant.
     *
     * @param name The name.
     */
    void setName(String name);

    /**
     * Get the email of the participant.
     *
     * @return The email.
     */
    @Property("email")
    String getEmail();

    /**
     * Set the email of the participant.
     *
     * @param email The email.
     */
    void setEmail(String email);

    /**
     * Get the url of the participant.
     *
     * @return The url.
     */
    @Property("url")
    String getUrl();

    /**
     * Set the url of the participant.
     *
     * @param url The url.
     */
    void setUrl(String url);

    /**
     * Get the organization of the participant.
     *
     * @return The organization.
     */
    @Property("organization")
    String getOrganization();

    /**
     * Set the organization of the participant.
     *
     * @param organization The organization.
     */
    void setOrganization(String organization);

    /**
     * Get the organizationUrl of the participant.
     *
     * @return The organizationUrl.
     */
    @Property("organizationUrl")
    String getOrganizationUrl();

    /**
     * Set the organizationUrl of the participant.
     *
     * @param organizationUrl The organizationUrl.
     */
    void setOrganizationUrl(String organizationUrl);

    /**
     * Get the timezone of the developer.
     *
     * @return The timezone.
     */
    @Property("timezone")
    String getTimezone();

    /**
     * Set the timezone of the participant.
     *
     * @param timezone The timezone.
     */
    void setTimezone(String timezone);

    /**
     * Get roles of the participant.
     *
     * @return The roles.
     */
    @Relation("HAS_ROLES")
    List<MavenParticipantRoleDescriptor> getRoles();
}
