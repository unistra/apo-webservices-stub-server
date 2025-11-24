package fr.unistra.dnum.apogee.ws.export.cli;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.*;
import fr.unistra.dnum.apogee.ws.server.stub.domaine.Person;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

class ExportJsonSchemaCLI {
    public static void main(String... args) throws IOException {
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
        configBuilder.forFields()
                .withTargetTypeOverridesResolver(ExportJsonSchemaCLI::treatTableauAsList);
        SchemaGeneratorConfig config = configBuilder.build();
        SchemaGenerator generator = new SchemaGenerator(config);
        JsonNode jsonSchema = generator.generateSchema(Person[].class);
        Files.writeString(
                Path.of("src/main/resources/persons.schema.json"),
                jsonSchema.toPrettyString()
        );
    }

    private static List<ResolvedType> treatTableauAsList(FieldScope scope) {
        ResolvedType type = scope.getType();
        if (type != null
                && type.getErasedType().getSimpleName().startsWith("Tableau")
                && type.getMemberMethods().size() == 1
                && type.getMemberMethods().getFirst().getRawMember().getParameterCount() == 0
        ) {
            ResolvedType itemType = scope.getContext().resolve(
                    type.getMemberMethods().getFirst().getRawMember().getGenericReturnType()
            );
            return Collections.singletonList(itemType);
        }
        return null;
    }

}