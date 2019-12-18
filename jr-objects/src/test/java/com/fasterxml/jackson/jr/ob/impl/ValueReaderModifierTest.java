package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.TestBase;
import com.fasterxml.jackson.jr.ob.api.ReaderWriterModifier;
import com.fasterxml.jackson.jr.ob.api.ValueReader;

public class ValueReaderModifierTest extends TestBase
{
    static class RWModifier extends ReaderWriterModifier {
        private final Class<?> _target;
        private final ValueReader _reader;

        public RWModifier(Class<?> target, ValueReader r) {
            _target = target;
            _reader = r;
        }
        
        @Override
        public ValueReader modifyValueReader(JSONReader readContext,
                Class<?> type, ValueReader defaultReader) {
            if (type == _target) {
                return _reader;
            }
            return defaultReader;
        }
    }

    static class NameLowerCasingReader extends ValueReader
    {
        private final ValueReader _origReader;

        public NameLowerCasingReader(ValueReader orig) {
            super(NameBean.class);
            _origReader = orig;
        }

        @Override
        public Object read(JSONReader reader, JsonParser p) throws IOException {
            NameBean nb = (NameBean) _origReader.read(reader, p);
            return new NameBean(nb.getFirst().toLowerCase(),
                    nb.getLast().toLowerCase());
        }
    }

    static class LowerCasingReaderModifier extends ReaderWriterModifier {
        public LowerCasingReaderModifier() { }
        
        @Override
        public ValueReader modifyValueReader(JSONReader readContext,
                Class<?> type, ValueReader defaultReader) {
            if (type == NameBean.class) {
                return new NameLowerCasingReader(defaultReader);
            }
            return defaultReader;
        }
    }
    
    /*
    /**********************************************************************
    /* Tests for wholesale replacement of `ValueReader`
    /**********************************************************************
     */
    
    public void testStringReaderReplacement() throws Exception
    {
        final ReaderWriterModifier mod = new RWModifier(String.class,
                new ValueReader(String.class) {
            @Override
            public Object read(JSONReader reader, JsonParser p) throws IOException {
                return p.getText().toUpperCase();
            };
        });
        String result = JSON.std.with(mod).beanFrom(String.class, quote("foobar"));
        assertEquals("FOOBAR", result);
    }

    public void testPOJOReaderReplacement() throws Exception
    {
        final ReaderWriterModifier mod = new RWModifier(NameBean.class,
                new ValueReader(NameBean.class) {
            @Override
            public Object read(JSONReader reader, JsonParser p) throws IOException {
                Map<?, Object> map = reader.readMap();
                return new NameBean(String.valueOf(map.get("first")).toUpperCase(),
                        String.valueOf(map.get("last")).toUpperCase());
            };
        });
        NameBean result = JSON.std.with(mod).beanFrom(NameBean.class,
                aposToQuotes("{'first':'foo', 'last':'bar'}"));
        assertEquals("FOO", result.getFirst());
        assertEquals("BAR", result.getLast());
    }

    public void testPOJOReaderDelegation() throws Exception
    {
        NameBean result = JSON.std.with(new LowerCasingReaderModifier())
                .beanFrom(NameBean.class,
                        aposToQuotes("{'first':'Foo', 'last':'Bar'}"));
        assertEquals("foo", result.getFirst());
        assertEquals("bar", result.getLast());
    }
}