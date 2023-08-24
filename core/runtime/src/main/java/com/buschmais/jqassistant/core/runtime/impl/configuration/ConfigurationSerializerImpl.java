package com.buschmais.jqassistant.core.runtime.impl.configuration;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationSerializer;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.ConfigMappingInterface;
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

public class ConfigurationSerializerImpl<C> implements ConfigurationSerializer<C> {

    @Override
    public String toYaml(C configuration) {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(BLOCK);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setIndent(2);
        dumperOptions.setAllowReadOnlyProperties(true);
        PropertyUtils propertyUtils = new ConfigPropertyUtils();
        propertyUtils.setAllowReadOnlyProperties(true);
        Representer representer = new ConfigRepresenter(dumperOptions);
        representer.setPropertyUtils(propertyUtils);
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

    private static Optional<Class<?>> getConfigInterface(Class<?> type) {
        return stream(type.getInterfaces()).filter(i -> i.isAnnotationPresent(ConfigMapping.class))
            .findFirst();
    }

    private static List<Class<?>> getImplementedInterfaces(Class<?> type) {
        List<Class<?>> result = new LinkedList<>();
        result.add(type);
        for (Class<?> anInterface : type.getInterfaces()) {
            result.addAll(getImplementedInterfaces(anInterface));
        }
        return result;
    }

    private static class ConfigRepresenter extends Representer {
        ConfigRepresenter(DumperOptions options) {
            super(options);
            representers.put(Optional.class, new RepresentOptional());
        }

        @Override
        protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
            addClassTag(javaBean.getClass(), Tag.MAP);
            return super.representJavaBean(properties, javaBean);
        }

        @Override
        protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
            return propertyValue == null || (propertyValue instanceof Optional<?> && !((Optional<?>) propertyValue).isPresent()) ?
                null :
                super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        }

        private class RepresentOptional implements Represent {
            @Override
            public Node representData(Object data) {
                Optional<?> opt = (Optional<?>) data;
                return ConfigRepresenter.this.representData(opt.orElse(null));
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
                        ConfigMappingInterface.NamingStrategy namingStrategy = configMappingInterface.getNamingStrategy();
                        for (ConfigMappingInterface.Property property : configMappingInterface.getProperties()) {
                            String propertyName = namingStrategy.apply(property.getPropertyName());
                            properties.put(propertyName, getProperty(propertyName, property.getMethod()));
                        }
                    }
                    return properties;
                })
                .orElse(emptyMap());
        }

        private Property getProperty(String propertyName, Method method) {
            try {
                return new MethodProperty(new PropertyDescriptor(propertyName, method, null));
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
