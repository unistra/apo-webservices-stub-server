package fr.unistra.dnum.apogee.ws.server.stub.presentation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Objects;

public class PresentationUtils {
    private PresentationUtils() { /*_*/ }

    private static final ObjectWriter prettyPrinter = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .writer();

    public static String toPrettyString(Object obj) {
        try {
            return prettyPrinter.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return Objects.toString(obj);
        }
    }

}
