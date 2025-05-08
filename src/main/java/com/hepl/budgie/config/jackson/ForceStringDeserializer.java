package com.hepl.budgie.config.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class ForceStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        if (parser.getCurrentToken() != JsonToken.VALUE_STRING) {
            return null;
        }
        return parser.getValueAsString();
    }

}
