package com.fasterxml.jackson.jr.annotationsupport;

import java.lang.reflect.*;
import java.util.*;

import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;
import com.fasterxml.jackson.jr.ob.impl.POJODefinition;

/**
 *
 * @since 2.11
 */
public class AnnotationBasedIntrospector
{
    public AnnotationBasedIntrospector() { }

    public POJODefinition pojoDefinitionForDeserialization(JSONReader r, Class<?> pojoType) {
        return null;
//        return _construct(pojoType);
    }

    public POJODefinition pojoDefinitionForSerialization(JSONWriter w, Class<?> pojoType) {
        return null;
//        return _construct(pojoType);
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */
/*
    private POJODefinition _construct(Class<?> beanType)
    {
        Map<String,PropBuilder> propsByName = new TreeMap<String,PropBuilder>();
        _introspect(beanType, propsByName);

        Constructor<?> defaultCtor = null;
        Constructor<?> stringCtor = null;
        Constructor<?> longCtor = null;

        for (Constructor<?> ctor : beanType.getDeclaredConstructors()) {
            Class<?>[] argTypes = ctor.getParameterTypes();
            if (argTypes.length == 0) {
                defaultCtor = ctor;
            } else if (argTypes.length == 1) {
                Class<?> argType = argTypes[0];
                if (argType == String.class) {
                    stringCtor = ctor;
                } else if (argType == Long.class || argType == Long.TYPE) {
                    longCtor = ctor;
                } else {
                    continue;
                }
            } else {
                continue;
            }
        }
        final int len = propsByName.size();
        Prop[] props;
        if (len == 0) {
            props = NO_PROPS;
        } else {
            props = new Prop[len];
            int i = 0;
            for (PropBuilder builder : propsByName.values()) {
                props[i++] = builder.build();
            }
        }
        return new POJODefinition(beanType, props, defaultCtor, stringCtor, longCtor);
    }

    private static void _introspect(Class<?> currType, Map<String,PropBuilder> props)
    {
        if (currType == null || currType == Object.class) {
            return;
        }
        // First, check base type
        _introspect(currType.getSuperclass(), props);

        // then public fields (since 2.8); may or may not be ultimately included
        // but at this point still possible
        for (Field f : currType.getDeclaredFields()) {
            if (!Modifier.isPublic(f.getModifiers())
                    || f.isEnumConstant() || f.isSynthetic()) {
                continue;
            }
            _propFrom(props, f.getName()).withField(f);
        }

        // then get methods from within this class
        for (Method m : currType.getDeclaredMethods()) {
            final int flags = m.getModifiers();
            // 13-Jun-2015, tatu: Skip synthetic, bridge methods altogether, for now
            //    at least (add more complex handling only if absolutely necessary)
            if (Modifier.isStatic(flags)
                    || m.isSynthetic() || m.isBridge()) {
                continue;
            }
            Class<?> argTypes[] = m.getParameterTypes();
            if (argTypes.length == 0) { // getter?
                // getters must be public to be used
                if (!Modifier.isPublic(flags)) {
                    continue;
                }
                
                Class<?> resultType = m.getReturnType();
                if (resultType == Void.class) {
                    continue;
                }
                String name = m.getName();
                if (name.startsWith("get")) {
                    if (name.length() > 3) {
                        name = decap(name.substring(3));
                        _propFrom(props, name).withGetter(m);
                    }
                } else if (name.startsWith("is")) {
                    if (name.length() > 2) {
                        // May or may not be used, but collect for now all the same:
                        name = decap(name.substring(2));
                        _propFrom(props, name).withIsGetter(m);
                    }
                }
            } else if (argTypes.length == 1) { // setter?
                // Non-public setters are fine if we can force access, don't yet check
                // let's also not bother about return type; setters that return value are fine
                String name = m.getName();
                if (!name.startsWith("set") || name.length() == 3) {
                    continue;
                }
                name = decap(name.substring(3));
                _propFrom(props, name).withSetter(m);
            }
        }
    }

    private static PropBuilder _propFrom(Map<String,PropBuilder> props, String name) {
        PropBuilder prop = props.get(name);
        if (prop == null) {
            prop = Prop.builder(name);
            props.put(name, prop);
        }
        return prop;
    }
    
    private static String decap(String name) {
        char c = name.charAt(0);
        char lowerC = Character.toLowerCase(c);

        if (c != lowerC) {
            // First: do NOT lower case if more than one leading upper case letters:
            if ((name.length() == 1)
                    || !Character.isUpperCase(name.charAt(1))) {
                char chars[] = name.toCharArray();
                chars[0] = lowerC;
                return new String(chars);
            }
        }
        return name;
    }
*/
}
