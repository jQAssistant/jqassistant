package com.buschmais.jqassistant.plugin.yaml.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.yaml.api.model.YAMLFileDescriptor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;
import org.yaml.snakeyaml.serializer.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class YAMLFileScannerPlugin extends AbstractScannerPlugin<FileResource, YAMLFileDescriptor> {

    /**
     * Supported file extension for YAML file resources.
     */
    public final static String YAML_FILE_EXTENSION = ".yaml";

    @Override
    public boolean accepts(FileResource file, String path, Scope scope) throws IOException {
        return file.getFile().getName().toLowerCase().endsWith(YAML_FILE_EXTENSION);
    }

    @Override
    public YAMLFileDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        Store store = scanner.getContext().getStore();



        Yaml yaml = new Yaml(new TagOverridingConstructor());
        Representer representer = new Representer();
        DumperOptions options = new DumperOptions();

        YAMLFileDescriptor fileDescriptor = store.create(YAMLFileDescriptor.class);

        fileDescriptor.setFileName(item.getFile().getAbsolutePath());

        try (InputStream in = item.createStream()) {
            Iterable<Object> docs = yaml.loadAll(in);

            for (Object doc : docs) {
                Node node = representer.represent(doc);
                YAMLEmitter emitter = new YAMLEmitter(fileDescriptor, scanner);
                Serializer serializer = new Serializer(emitter, new Resolver(), options, null);

                serializer.open();
                serializer.serialize(node);
                serializer.close();
            }
        }

        return fileDescriptor;
    }


    class TagOverridingConstructor extends Constructor {
        private List<Tag> SUPPORTED_TAGS =
             Arrays.asList(Tag.YAML, Tag.MERGE,
                           Tag.SET, Tag.PAIRS, Tag.OMAP,
                           Tag.BINARY, Tag.INT, Tag.FLOAT,
//                           Tag.TIMESTAMP,
                           Tag.BOOL, Tag.NULL,
                           Tag.STR, Tag.SEQ, Tag.MAP);

        @Override
        protected Object constructObject(Node node) {
            Tag tag = node.getTag();

            if (!SUPPORTED_TAGS.contains(tag)) {
                node.setTag(Tag.STR);
            }

            return super.constructObject(node);
        }


    }
}
