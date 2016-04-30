package com.buschmais.jqassistant.plugin.javaee6.impl.scanner;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.javaee6.api.model.*;

import com.sun.java.xml.ns.javaee.*;

public class XmlDescriptorHelper {

    private XmlDescriptorHelper() {
    }

    /**
     * Create a description descriptor.
     *
     * @param descriptionType
     *            The XML description type.
     * @param store
     *            The store.
     * @return The description descriptor.
     */
    public static DescriptionDescriptor createDescription(DescriptionType descriptionType, Store store) {
        DescriptionDescriptor descriptionDescriptor = store.create(DescriptionDescriptor.class);
        descriptionDescriptor.setLang(descriptionType.getLang());
        descriptionDescriptor.setValue(descriptionType.getValue());
        return descriptionDescriptor;
    }

    /**
     * Create an icon descriptor.
     *
     * @param iconType
     *            The XML icon type.
     * @param store
     *            The store
     * @return The icon descriptor.
     */
    public static IconDescriptor createIcon(IconType iconType, Store store) {
        IconDescriptor iconDescriptor = store.create(IconDescriptor.class);
        iconDescriptor.setLang(iconType.getLang());
        PathType largeIcon = iconType.getLargeIcon();
        if (largeIcon != null) {
            iconDescriptor.setLargeIcon(largeIcon.getValue());
        }
        PathType smallIcon = iconType.getSmallIcon();
        if (smallIcon != null) {
            iconDescriptor.setSmallIcon(smallIcon.getValue());
        }
        return iconDescriptor;
    }

    /**
     * Create a display name descriptor.
     *
     * @param displayNameType
     *            The XML display name type.
     * @param store
     *            The store.
     * @return The display name descriptor.
     */
    public static DisplayNameDescriptor createDisplayName(DisplayNameType displayNameType, Store store) {
        DisplayNameDescriptor displayNameDescriptor = store.create(DisplayNameDescriptor.class);
        displayNameDescriptor.setLang(displayNameType.getLang());
        displayNameDescriptor.setValue(displayNameType.getValue());
        return displayNameDescriptor;
    }

    public static SecurityRoleDescriptor createSecurityRole(SecurityRoleType securityRoleType, Store store) {
        SecurityRoleDescriptor securityRoleDescriptor = store.create(SecurityRoleDescriptor.class);
        for (DescriptionType descriptionType : securityRoleType.getDescription()) {
            securityRoleDescriptor.getDescriptions().add(XmlDescriptorHelper.createDescription(descriptionType, store));
        }
        securityRoleDescriptor.setRoleName(createRoleName(securityRoleType.getRoleName(), store));
        return securityRoleDescriptor;
    }

    public static RoleNameDescriptor createRoleName(RoleNameType roleNameType, Store store) {
        RoleNameDescriptor roleNameDescriptor = store.create(RoleNameDescriptor.class);
        roleNameDescriptor.setName(roleNameType.getValue());
        return roleNameDescriptor;
    }

}
