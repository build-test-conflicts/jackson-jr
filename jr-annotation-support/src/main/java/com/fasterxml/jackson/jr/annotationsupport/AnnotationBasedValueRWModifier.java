package com.fasterxml.jackson.jr.annotationsupport;

import com.fasterxml.jackson.jr.ob.api.ReaderWriterModifier;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;
import com.fasterxml.jackson.jr.ob.impl.POJODefinition;

public class AnnotationBasedValueRWModifier
    extends ReaderWriterModifier
{
    @Override
    public POJODefinition pojoDefinitionForDeserialization(JSONReader readContext,
            Class<?> pojoType)
    {
        // use default
        return null;
    }

    @Override
    public POJODefinition pojoDefinitionForSerialization(JSONWriter writeContext,
            Class<?> pojoType) {
        // use default
        return null;
    }

}
