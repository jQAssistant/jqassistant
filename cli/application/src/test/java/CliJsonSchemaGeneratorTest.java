import java.io.File;
import java.io.IOException;
import java.util.Set;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.runtime.api.configuration.JsonSchemaGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CliJsonSchemaGeneratorTest {

    private final JsonSchemaGenerator generator = new JsonSchemaGenerator();

    @Test
    public void generateSchema() throws IOException {
        JsonNode node = generator.generateSchema(CliConfiguration.class, "target/generated-resources/schema/jqassistant-configuration-cli.schema.json");
        File file = new File("target/generated-resources/schema/jqassistant-configuration-cli.schema.json");
        assertThat(node).isNotNull();
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            JsonSchemaFactory bluePrintFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(bluePrintFactory)
                .build();
            JsonSchema schema = schemaFactory.getSchema(node);
            JsonNode rootNode = mapper.readTree(file);
            Set<ValidationMessage> validationMessages = schema.validate(rootNode);
            if (!validationMessages.isEmpty()) {
                System.out.println(validationMessages);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
