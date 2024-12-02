import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.runtime.api.configuration.JsonSchemaGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.smallrye.config.ConfigMapping;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaGeneratorTest {

    private final JsonSchemaGenerator generator = new JsonSchemaGenerator();

    @Test
    public void generateSchema() throws IOException {
        JsonNode node = generator.generateSchema(CliJQAssistant.class);
        assertThat(node).isNotNull();
        ObjectMapper objectMapper = new ObjectMapper();
        File targetFile;
        try {
            targetFile = new File("src/main/resources/json-schema", "jqassistant-configuration-cli.schema.json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(targetFile, node);
            System.out.println("Schema saved: " + targetFile.getAbsolutePath());
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            JsonSchemaFactory bluePrintFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(bluePrintFactory).build();
            JsonSchema schema = schemaFactory.getSchema(node);
            JsonNode rootNode = mapper.readTree(targetFile);
            Set<ValidationMessage> validationMessages = schema.validate(rootNode);
            System.out.println(validationMessages);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @ConfigMapping
    private interface CliJQAssistant {
        CliConfiguration jqassistant();
    }
}
