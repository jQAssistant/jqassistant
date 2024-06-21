package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.ConfigMappingInterface;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.MethodProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import static io.smallrye.config.ConfigMappingInterface.getConfigurationInterface;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK;

@Slf4j
public class ConfigurationSerializer<C extends Configuration> {

    public String toYaml(C configuration) {
        DumperOptions dumperOptions = getDumperOptions();
        Representer representer = getRepresenter(dumperOptions);
        Yaml yaml = new Yaml(representer);
        yaml.setBeanAccess(BeanAccess.PROPERTY);
        Optional<Class<?>> configInterface = getConfigInterface(configuration.getClass());
        return configInterface.map(type -> {
                Map<String, Object> configRoot = new HashMap<>();
                configRoot.put(type.getAnnotation(ConfigMapping.class)
                    .prefix(), configuration);
                return yaml.dump(configRoot);
            })
            .orElseThrow(() -> new IllegalArgumentException("Cannot serialize object without ConfigMapping: " + configuration));
    }

    private static DumperOptions getDumperOptions() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(BLOCK);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setIndent(2);
        dumperOptions.setAllowReadOnlyProperties(true);
        return dumperOptions;
    }

    private static Representer getRepresenter(DumperOptions dumperOptions) {
        PropertyUtils propertyUtils = new ConfigPropertyUtils();
        propertyUtils.setAllowReadOnlyProperties(true);
        return new ConfigRepresenter(dumperOptions, propertyUtils);
    }

    /**
     * Finds the first implemented interface annotated with {@link ConfigMapping}.
     *
     * @param configType
     *     The configType
     * @return The optional {@link ConfigMapping} annotated interface.
     */
    private static Optional<Class<?>> getConfigInterface(Class<?> configType) {
        return stream(configType.getInterfaces()).filter(i -> i.isAnnotationPresent(ConfigMapping.class))
            .findFirst();
    }

    /**
     * Determines all implemented interfaces of the given type (including inherited interfaces)
     *
     * @param type
     *     The type.
     * @return The list of implemented interfaces.
     */
    private static List<Class<?>> getImplementedInterfaces(Class<?> type) {
        List<Class<?>> result = new LinkedList<>();
        result.add(type);
        for (Class<?> anInterface : type.getInterfaces()) {
            result.addAll(getImplementedInterfaces(anInterface));
        }
        return result;
    }

    private static class ConfigRepresenter extends Representer {
        ConfigRepresenter(DumperOptions options, PropertyUtils propertyUtils) {
            super(options);
            super.setPropertyUtils(propertyUtils);
            setDefaultFlowStyle(options.getDefaultFlowStyle());
            representers.put(Optional.class, new RepresentOptional());
            RepresentString representString = new RepresentString();
            representers.put(URI.class, representString);
            representers.put(File.class, representString);
        }

        @Override
        protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
            addClassTag(javaBean.getClass(), Tag.MAP);
            return super.representJavaBean(properties, javaBean);
        }

        /**
         * Suppress rendering keys are "null" or empty optionals
         *
         * @param javaBean
         *     - the instance to be represented
         * @param property
         *     - the property of the instance
         * @param propertyValue
         *     - value to be represented
         * @param customTag
         *     - user defined Tag
         * @return
         */
        @Override
        protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
            return isNullOrEmpty(propertyValue) ? null : super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        }

        private static boolean isNullOrEmpty(Object propertyValue) {
            return propertyValue == null || (propertyValue instanceof Optional<?> && !((Optional<?>) propertyValue).isPresent());
        }

        /**
         * Unwrap optionals.
         */
        private class RepresentOptional implements Represent {
            @Override
            public Node representData(Object data) {
                return ConfigRepresenter.this.representData(((Optional<?>) data).orElse(null));
            }
        }

        /**
         * Use the string representation of the provided data.
         */
        private class RepresentString implements Represent {
            @Override
            public Node representData(Object data) {
                return ConfigRepresenter.this.representData(data.toString());
            }
        }
    }

    private static class ConfigPropertyUtils extends PropertyUtils {
        @Override
        protected Map<String, Property> getPropertiesMap(Class<?> type, BeanAccess bAccess) {
            Optional<Class<?>> configInterface = getConfigInterface(type);
            return configInterface.map(configurationInterface -> {
                    Map<String, Property> properties = new HashMap<>();
                    for (Class<?> implementedInterface : getImplementedInterfaces(configurationInterface)) {
                        ConfigMappingInterface configMappingInterface = getConfigurationInterface(implementedInterface);
                        ConfigMapping.NamingStrategy namingStrategy = configMappingInterface.getNamingStrategy();
                        for (ConfigMappingInterface.Property property : configMappingInterface.getProperties()) {
                            String propertyName = namingStrategy.apply(property.getPropertyName());
                            properties.put(propertyName, getProperty(propertyName, property.getMethod()));
                        }
                    }
                    return properties;
                })
                .orElseGet(() -> {
                    log.warn("Type '{}' does not implement an interface annotated by '{}'.", type.getName(), ConfigMapping.class.getName());
                    return emptyMap();
                });
        }

        private Property getProperty(String propertyName, Method method) {
            try {
                return new MethodProperty(new PropertyDescriptor(propertyName, method, null));
            } catch (IntrospectionException e) {
                throw new IllegalStateException("Cannot create method property for " + propertyName + " from method " + method, e);
            }
        }
    }
}
