package com.buschmais.jqassistant.commandline.task;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.plugin.api.PluginInfo;

/**
 * @author Oliver B. Fischer, Freiheitsgrade Consulting
 */
public class ListPluginsTask extends AbstractTask {

    @Override
    public void run(CliConfiguration configuration) {
        Comparator<PluginInfo> comparator = PluginInfo.NAME_COMPARATOR;

        List<PluginInfo> sortedInfos = pluginRepository.getPluginOverview()
            .stream()
            .sorted(comparator)
            .collect(Collectors.toList());

        sortedInfos.forEach(info -> {
            CharSequence name = info.getName();
            CharSequence id = info.getId();
            String output = String.format("%s (%s)", name, id);
            System.out.println(output);
        });
    }
}
