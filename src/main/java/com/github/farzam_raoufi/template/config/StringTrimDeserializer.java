package com.github.farzam_raoufi.template.config;

import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@JacksonComponent
public class StringTrimDeserializer extends ValueDeserializer<String> {

    private final boolean keepRaw;

    // Required default constructor for component scanning
    public StringTrimDeserializer() {
        this.keepRaw = false;
    }

    // Context constructor to apply state based on field analysis
    private StringTrimDeserializer(boolean keepRaw) {
        this.keepRaw = keepRaw;
    }

    // Jackson 3 natively builds contextual configurations directly via this override
    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property != null && property.getAnnotation(KeepRaw.class) != null) {
            return new StringTrimDeserializer(true);
        }
        return this;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }

        // Skip modifying fields flagged with @KeepRaw
        if (keepRaw) {
            return value;
        }

        // Standard logic for all other string fields
        return value.trim().replaceAll("\\s+", " ");
    }
}

