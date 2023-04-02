package com.buschmais.jqassistant.core.runtime.impl.plugin;

import java.util.function.Function;
import java.util.function.IntUnaryOperator;

import org.apache.commons.lang3.StringUtils;
import org.jqassistant.schema.plugin.v1.JqassistantPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginIdGenerator
    implements Function<JqassistantPlugin, JqassistantPlugin> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginIdGenerator.class);

    private static final Character UNDERSCORE = '_';

    @Override
    public JqassistantPlugin apply(JqassistantPlugin plugin) {
        if (StringUtils.isBlank(plugin.getId())) {
            String name = plugin.getName().toLowerCase();
            IntUnaryOperator replacer = i -> (Character.isWhitespace(i)) ? UNDERSCORE : i;
            StringBuilder generate = new StringBuilder();

            for (int index = 0; index < name.length(); index++) {
                int updated = replacer.applyAsInt(name.charAt(index));
                int lastChar = getLastChar(generate);

                if (!(updated == UNDERSCORE && lastChar == UNDERSCORE)) {
                    generate.appendCodePoint(updated);
                }
            }

            plugin.setId(generate.toString());

            LOGGER.debug("Assigned generated plugin id '{}' to plugin named '{}'",
                         plugin.getName(), plugin.getId());
        }

        return plugin;
    }

    private int getLastChar(CharSequence input) {
        if (input != null && input.length() > 0) {
            return input.charAt(input.length() - 1);
        }

        return -1;
    }
}
