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
 *
 * Note: Detection is performed on the actual runtime class of each model instance
 * (not on the statically-loaded Model.class) because jQAssistant scanner plugins run
 * in their own classloader where the bundled maven-model dependency may differ from
 * the Maven runtime version.
 *
 * The helper traverses the {@code getDelegate()} chain to reach the Maven 4 API model
 * object, which may be wrapped by jQAssistant's {@code EffectiveModel} and/or
 * Maven 4's compat layer.
 */
final class Maven4ModelHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Maven4ModelHelper.class);

    private Maven4ModelHelper() {
    }

    /**
     * Traverse the {@code getDelegate()} chain on a model object to reach the
     * Maven 4 API model. This handles multiple wrapper layers:
     * <ol>
     *   <li>jQAssistant's {@code EffectiveModel.getDelegate()} → Maven 4 compat Model</li>
     *   <li>Maven 4 compat {@code Model.getDelegate()} → Maven 4 API Model</li>
     * </ol>
     *
     * @param object the starting model object
     * @param maxDepth maximum number of delegate levels to traverse
     * @return the deepest delegate object, or null if no {@code getDelegate()} is available
     */
    static Object resolveApiDelegate(Object object, int maxDepth) {
        Object current = object;
        for (int i = 0; i < maxDepth; i++) {
            try {
                Method method = current.getClass().getMethod("getDelegate");
                Object delegate = method.invoke(current);
                if (delegate == null) {
                    return null;
                }
                current = delegate;
            } catch (NoSuchMethodException e) {
                // No more delegate levels — current is the deepest
                break;
            } catch (Exception e) {
                LOGGER.debug("Failed to invoke getDelegate() at depth {}.", i, e);
                return null;
            }
        }
        // If we never called getDelegate(), no Maven 4 API is available
        return current == object ? null : current;
    }

    /**
     * Get the root flag from a Maven 4.1.0 model.
     *
     * @param model the Maven model
     * @return the root flag, or null if Maven 4 is not available
     */
    static Boolean isRoot(Model model) {
        Object apiModel = resolveApiDelegate(model, 3);
        if (apiModel == null) {
            return null;
        }
        try {
            Object result = invokeMethod(apiModel, "isRoot");
            return (Boolean) result;
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
        Object apiModel = resolveApiDelegate(model, 3);
        if (apiModel == null) {
            return Collections.emptyList();
        }
        try {
            return (List<String>) invokeMethod(apiModel, "getSubprojects");
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
        Object apiModel = resolveApiDelegate(activation, 3);
        if (apiModel == null) {
            return null;
        }
        try {
            return (String) invokeMethod(apiModel, "getCondition");
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
        Object apiModel = resolveApiDelegate(pluginExecution, 3);
        if (apiModel == null) {
            return null;
        }
        try {
            Method method = apiModel.getClass().getMethod("getPriority");
            return (Integer) method.invoke(apiModel);
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
        Object apiModel = resolveApiDelegate(build, 3);
        if (apiModel == null) {
            return Collections.emptyList();
        }
        try {
            Object sources = invokeMethod(apiModel, "getSources");
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
