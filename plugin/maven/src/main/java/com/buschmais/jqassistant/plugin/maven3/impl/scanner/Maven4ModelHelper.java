package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Activation;
import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Model;
import org.apache.maven.model.PluginExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reflection-based helper to access Maven 4.x API methods when available.
 *
 * Maven 4.1.0 introduces new POM elements (root, subprojects, condition, priority, sources)
 * which are only available in the Maven 4 compat model classes (requires Java 17+).
 * Since jQAssistant targets Java 11, this helper uses reflection to access these methods
 * at runtime when running under Maven 4, and gracefully returns null/empty when not available.
 */
final class Maven4ModelHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Maven4ModelHelper.class);

    private static final boolean MAVEN4_AVAILABLE;

    static {
        boolean available = false;
        try {
            Model.class.getMethod("getDelegate");
            available = true;
            LOGGER.debug("Maven 4 compat API detected.");
        } catch (NoSuchMethodException e) {
            LOGGER.debug("Maven 4 compat API not available, Maven 4.x specific elements will be skipped.");
        }
        MAVEN4_AVAILABLE = available;
    }

    private Maven4ModelHelper() {
    }

    /**
     * @return true if the Maven 4 compat API is available at runtime.
     */
    static boolean isMaven4Available() {
        return MAVEN4_AVAILABLE;
    }

    /**
     * Get the root flag from a Maven 4.1.0 model.
     *
     * @param model the Maven model
     * @return the root flag, or null if Maven 4 is not available
     */
    static Boolean isRoot(Model model) {
        if (!MAVEN4_AVAILABLE) {
            return null;
        }
        try {
            Object delegate = invokeMethod(model, "getDelegate");
            return (Boolean) invokeMethod(delegate, "isRoot");
        } catch (Exception e) {
            LOGGER.debug("Failed to get root flag from Maven 4 model.", e);
            return null;
        }
    }

    /**
     * Get subprojects from a Maven 4.1.0 model.
     *
     * @param model the Maven model
     * @return the list of subprojects, or empty list if Maven 4 is not available
     */
    @SuppressWarnings("unchecked")
    static List<String> getSubprojects(Model model) {
        if (!MAVEN4_AVAILABLE) {
            return Collections.emptyList();
        }
        try {
            Object delegate = invokeMethod(model, "getDelegate");
            return (List<String>) invokeMethod(delegate, "getSubprojects");
        } catch (Exception e) {
            LOGGER.debug("Failed to get subprojects from Maven 4 model.", e);
            return Collections.emptyList();
        }
    }

    /**
     * Get the condition string from a Maven 4.1.0 profile activation.
     *
     * @param activation the Maven activation
     * @return the condition string, or null if Maven 4 is not available
     */
    static String getCondition(Activation activation) {
        if (!MAVEN4_AVAILABLE) {
            return null;
        }
        try {
            Object delegate = invokeMethod(activation, "getDelegate");
            return (String) invokeMethod(delegate, "getCondition");
        } catch (Exception e) {
            LOGGER.debug("Failed to get condition from Maven 4 activation.", e);
            return null;
        }
    }

    /**
     * Get the priority from a Maven 4.1.0 plugin execution.
     *
     * @param pluginExecution the Maven plugin execution
     * @return the priority, or null if Maven 4 is not available
     */
    static Integer getPriority(PluginExecution pluginExecution) {
        if (!MAVEN4_AVAILABLE) {
            return null;
        }
        try {
            // Priority is available directly on the compat PluginExecution in Maven 4
            Method method = pluginExecution.getClass().getMethod("getPriority");
            return (Integer) method.invoke(pluginExecution);
        } catch (Exception e) {
            LOGGER.debug("Failed to get priority from Maven 4 plugin execution.", e);
            return null;
        }
    }

    /**
     * Get sources from a Maven 4.1.0 build.
     * Each source is an API model object with properties: glob, directory, enabled.
     *
     * @param build the Maven build
     * @return the list of source objects (API model), or empty list if Maven 4 is not available
     */
    static List<?> getSources(BuildBase build) {
        if (!MAVEN4_AVAILABLE) {
            return Collections.emptyList();
        }
        try {
            Object delegate = invokeMethod(build, "getDelegate");
            Object sources = invokeMethod(delegate, "getSources");
            return sources instanceof List ? (List<?>) sources : Collections.emptyList();
        } catch (Exception e) {
            LOGGER.debug("Failed to get sources from Maven 4 build.", e);
            return Collections.emptyList();
        }
    }

    /**
     * Extract a string property from a source object via reflection.
     *
     * @param source the source object
     * @param methodName the getter method name
     * @return the string value, or null
     */
    static String getSourceStringProperty(Object source, String methodName) {
        try {
            Object result = invokeMethod(source, methodName);
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            LOGGER.debug("Failed to get {} from Maven 4 source.", methodName, e);
            return null;
        }
    }

    /**
     * Extract a boolean property from a source object via reflection.
     *
     * @param source the source object
     * @param methodName the getter method name
     * @return the boolean value, or null
     */
    static Boolean getSourceBooleanProperty(Object source, String methodName) {
        try {
            Object result = invokeMethod(source, methodName);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
            return result != null ? Boolean.valueOf(result.toString()) : null;
        } catch (Exception e) {
            LOGGER.debug("Failed to get {} from Maven 4 source.", methodName, e);
            return null;
        }
    }

    private static Object invokeMethod(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }
}
