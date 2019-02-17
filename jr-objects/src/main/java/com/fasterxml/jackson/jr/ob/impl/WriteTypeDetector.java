package com.fasterxml.jackson.jr.ob.impl;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.jr.ob.JSON;

/**
 * Helper object used for efficient detection of type information
 * relevant to our conversion needs when writing out Java Objects
 * as JSON.
 *<p>
 * Note that usage pattern is such that a single "root" instance is kept
 * by each {@link com.fasterxml.jackson.jr.ob.JSON} instance; and
 * an actual per-operation instance must be constructed by calling
 * {@link #perOperationInstance}: reason for this is that instances
 * use simple caching to handle the common case of repeating types
 * within JSON Arrays.
 */
public class WriteTypeDetector extends TypeDetectorBase
{
    protected final BeanPropertyWriter[] NO_PROPS_FOR_WRITE = new BeanPropertyWriter[0];

    /*
    /**********************************************************************
    /* Helper objects, serialization
    /**********************************************************************
     */

    /**
     * Mapping from classes to resolved type constants or indexes, to use
     * for serialization.
     */
    protected final ConcurrentHashMap<ClassKey, Integer> _knownSerTypes;

    protected final CopyOnWriteArrayList<BeanPropertyWriter[]> _knownWriters;

    /*
    /**********************************************************************
    /* Instance configuration
    /**********************************************************************
     */

    protected final int _features;

    protected final JSONWriter _writeContext;

    /*
    /**********************************************************************
    /* Instance state, caching
    /**********************************************************************
     */

    /**
     * Reusable lookup key; only used by per-thread instances.
     */
    private ClassKey _key;

    private Class<?> _prevClass;

    private int _prevType;

    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    /**
     * Constructor for the blueprint instance
     */
    protected WriteTypeDetector(int features)
    {
        _features = features;
        _knownSerTypes = new ConcurrentHashMap<ClassKey, Integer>(50, 0.75f, 4);
        _knownWriters = new CopyOnWriteArrayList<BeanPropertyWriter[]>();
        _writeContext = null;
    }

    protected WriteTypeDetector(WriteTypeDetector base, int features, JSONWriter w) {
        _features = features;
        _writeContext = w;
        _knownSerTypes = base._knownSerTypes;
        _knownWriters = base._knownWriters;
    }

    public final static WriteTypeDetector blueprint(int features) {
        return new WriteTypeDetector(features & CACHE_FLAGS);
    }

    public WriteTypeDetector perOperationInstance(JSONWriter w, int features) {
        return new WriteTypeDetector(this, features & CACHE_FLAGS, w);
    }

    /*
    /**********************************************************************
    /* Writer lookup methods
    /**********************************************************************
     */
    
    public BeanPropertyWriter[] getPropertyWriters(int index) {
        // for simplicity, let's allow caller to pass negative id as is
        if (index < 0) {
            index = -(index+1);
        }
        return _knownWriters.get(index);
    }

    /**
     * The main lookup method used to find type identifier for
     * given raw class; including Bean types (if allowed).
     */
    public final int findSerializationType(Class<?> raw)
    {
        if (raw == _prevClass) {
            return _prevType;
        }
        if (raw == String.class) {
            return SER_STRING;
        }
        ClassKey k = (_key == null) ? new ClassKey(raw, _features) : _key.with(raw, _features);
        int type;

        Integer I = _knownSerTypes.get(k);

        if (I == null) {
            type = _findPOJOSerializationType(raw);
            _knownSerTypes.put(new ClassKey(raw, _features), Integer.valueOf(type));
        } else {
            type = I.intValue();
        }
        _prevType = type;
        _prevClass = raw;
        return type;
    }

    protected int _findPOJOSerializationType(Class<?> raw)
    {
        int type = _findSimpleType(raw, true);
        if (type == SER_UNKNOWN) {
            if (JSON.Feature.HANDLE_JAVA_BEANS.isEnabled(_features)) {
                POJODefinition cd = resolvePOJODefinition(raw);
                BeanPropertyWriter[] props = resolveBeanForSer(raw, cd);
                // Due to concurrent access, possible that someone might have added it
                synchronized (_knownWriters) {
                    // Important: do NOT try to reuse shared instance; caller needs it
                    ClassKey k = new ClassKey(raw, _features);
                    Integer I = _knownSerTypes.get(k);
                    // if it was already concurrently added, we'll just discard this copy, return earlier
                    if (I != null) {
                        return I.intValue();
                    }
                    // otherwise add at the end, use -(index+1) as id
                    _knownWriters.add(props);
                    int typeId = -_knownWriters.size();
                    _knownSerTypes.put(k, Integer.valueOf(typeId));
                    return typeId;
                }
            }
        }
        return type;
    }

    protected BeanPropertyWriter[] resolveBeanForSer(Class<?> raw, POJODefinition classDef)
    {
        POJODefinition.Prop[] rawProps = classDef.properties();
        final int len = rawProps.length;
        List<BeanPropertyWriter> props = new ArrayList<BeanPropertyWriter>(len);
        final boolean includeReadOnly = JSON.Feature.WRITE_READONLY_BEAN_PROPERTIES.isEnabled(_features);
        final boolean forceAccess = JSON.Feature.FORCE_REFLECTION_ACCESS.isEnabled(_features);
        final boolean useFields = JSON.Feature.USE_FIELDS.isEnabled(_features);

        for (int i = 0; i < len; ++i) {
            POJODefinition.Prop rawProp = rawProps[i];
            Method m = rawProp.getter;
            if (m == null) {
                if (JSON.Feature.USE_IS_GETTERS.isEnabled(_features)) {
                    m = rawProp.isGetter;
                }
            }
            Field f = useFields ? rawProp.field : null;

            // But if neither regular nor is-getter, move on
            if ((m == null) && (f == null)) {
                continue;
            }
            // also: if setter is required to match, skip if none
            if (!includeReadOnly && !rawProp.hasSetter()) {
                continue;
            }
            Class<?> type;
            if (m != null) {
                type = m.getReturnType();
                if (forceAccess) {
                    m.setAccessible(true);
                }
            } else {
                type = f.getType();
                if (forceAccess) {
                    f.setAccessible(true);
                }
            }
            int typeId = _findSimpleType(type, true);
            props.add(new BeanPropertyWriter(typeId, rawProp.name, rawProp.field, m));
        }
        int plen = props.size();
        BeanPropertyWriter[] propArray = (plen == 0) ? NO_PROPS_FOR_WRITE
                : props.toArray(new BeanPropertyWriter[0]);
        return propArray;
    }
}