package com.buschmais.jqassistant.core.runtime.impl.plugin;

import java.util.*;

import com.buschmais.jqassistant.core.runtime.api.plugin.PluginConfigurationReader;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;

import org.jqassistant.schema.plugin.v2.ClassListType;
import org.jqassistant.schema.plugin.v2.IdClassListType;
import org.jqassistant.schema.plugin.v2.IdClassType;
import org.jqassistant.schema.plugin.v2.JqassistantPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.unmodifiableMap;

/**
 * Scanner plugin repository implementation.
 */
public class ScannerPluginRepositoryImpl extends AbstractPluginRepository implements ScannerPluginRepository {

    private static final Logger log = LoggerFactory.getLogger(ScannerPluginRepositoryImpl.class);
    private final Set<ScannerPlugin<?, ?>> scannerPlugins = new HashSet<>();

    private final Map<String, Scope> scopes;

    /**
     * Constructor.
     */
    public ScannerPluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) {
        super(pluginConfigurationReader);
        this.scopes = unmodifiableMap(this.getScopes(plugins));
    }

    @Override
    public void initialize() {
        getScannerPlugins(plugins);
    }

    @Override
    public void destroy() {
        scannerPlugins.forEach(ScannerPlugin::destroy);
    }

    @Override
    public Set<ScannerPlugin<?, ?>> getScannerPlugins(Scan scan, ScannerContext scannerContext) {
        Map<String, Object> properties = unmodifiableMap(scan.properties());
        for (ScannerPlugin<?, ?> scannerPlugin : scannerPlugins) {
            scannerPlugin.configure(scannerContext, properties);
        }
        return scannerPlugins;
    }

    @Override
    public Scope getScope(String name) {
        return scopes.get(name.toLowerCase());
    }

    @Override
    public Map<String, Scope> getScopes() {
        return scopes;
    }

    private void getScannerPlugins(List<JqassistantPlugin> plugins) {
        for (JqassistantPlugin plugin : plugins) {
            IdClassListType scannerTypes = plugin.getScanner();
            if (scannerTypes != null) {
                for (IdClassType classType : scannerTypes.getClazz()) {
                    ScannerPlugin<?, ?> scannerPlugin = createInstance(classType.getValue());
                    if (scannerPlugin != null) {
                        scannerPlugin.initialize();
                        String id = classType.getId();
                        if (id != null) {
                            log.info("The id attribute for scanner plugins is obsolete, please remove it: {}.", id);
                        }
                        scannerPlugins.add(scannerPlugin);
                    }
                }
            }
        }
    }

    private Map<String, Scope> getScopes(List<JqassistantPlugin> plugins) {
        Map<String, Scope> scopes = new HashMap<>();
        for (JqassistantPlugin plugin : plugins) {
            ClassListType scopeTypes = plugin.getScope();
            if (scopeTypes != null) {
                for (String scopePluginName : scopeTypes.getClazz()) {
                    Class<? extends Enum<?>> type = getType(scopePluginName);
                    for (Enum<?> enumConstant : type.getEnumConstants()) {
                        Scope scope = (Scope) enumConstant;
                        String scopeName = scope.getPrefix() + ":" + scope.getName();
                        scopes.put(scopeName.toLowerCase(), scope);
                    }
                }
            }
        }
        return scopes;
    }
}
