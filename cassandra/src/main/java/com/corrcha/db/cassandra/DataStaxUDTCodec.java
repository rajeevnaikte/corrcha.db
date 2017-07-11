package com.corrcha.db.cassandra;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UserType;
import com.corrcha.db.core.AnnotationParser;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * @author Rajeev Naik
 * @since 2017-06-13
 */
class DataStaxUDTCodec<T> extends TypeCodec.AbstractUDTCodec<T>
{
    private final UserType cqlUserType;
    private final Class<T> udtClass;
    private final Map<String, DataStaxEntityProperty> propertyMap;

    DataStaxUDTCodec(UserType cqlUserType, Class<T> udtClass, Map<String, DataStaxEntityProperty> propertyMap)
    {
        super(cqlUserType, udtClass);
        this.cqlUserType = cqlUserType;
        this.udtClass = udtClass;
        this.propertyMap = propertyMap;
    }

    @Override protected T newInstance()
    {
        return AnnotationParser.getInstance(this.udtClass);
    }

    @Override protected ByteBuffer serializeField(T source, String fieldName, ProtocolVersion protocolVersion)
    {
        return (ByteBuffer) get(fieldName,
                (property, codec) -> codec.serialize(property.getValue(source), protocolVersion), () -> null);
    }

    @Override protected T deserializeAndSetField(ByteBuffer input, T target, String fieldName, ProtocolVersion protocolVersion)
    {
        return (T) get(fieldName, (property, codec) ->
        {
            property.setValue(target, codec.deserialize(input, protocolVersion));
            return target;
        }, () -> target);
    }

    @Override protected String formatField(T source, String fieldName)
    {
        return (String) get(fieldName, (property, codec) -> codec.format(property.getValue(source)), () -> null);
    }

    @Override protected T parseAndSetField(String input, T target, String fieldName)
    {
        return (T) get(fieldName, (property, codec) ->
        {
            property.setValue(target, codec.parse(input));
            return target;
        }, () -> target);
    }

    private Object get(String fieldName, BiFunction<DataStaxEntityProperty, TypeCodec<Object>, Object> function, Supplier<Object> nullCheck)
    {
        DataStaxEntityProperty property = propertyMap.get(fieldName);
        if (property == null)
            return nullCheck.get();
        TypeCodec<Object> codec = property.getCustomCodec();
        if (codec == null)
            codec = CodecRegistry.DEFAULT_INSTANCE.codecFor(cqlUserType.getFieldType(property.getColumnName()), property.getJavaType());
        return function.apply(property, codec);
    }
}
