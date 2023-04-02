package com.buschmais.jqassistant.commandline.task;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginInfo;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * @author Oliver B. Fischer, Freiheitsgrade Consulting
 */
public class ListPluginsTask extends AbstractTask {

    @Override
    protected void addTaskOptions(List<Option> options) {
    }

    @Override
    public void run(CliConfiguration configuration, Options options) {
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
