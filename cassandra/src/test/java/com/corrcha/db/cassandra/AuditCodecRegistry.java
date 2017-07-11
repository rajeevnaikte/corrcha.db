package com.corrcha.db.cassandra;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.extras.codecs.enums.EnumOrdinalCodec;

public class AuditCodecRegistry
{
    public static final void init()
    {
        CodecRegistry.DEFAULT_INSTANCE
                .register(new EnumOrdinalCodec<Gender>(Gender.class))
                .register(new EnumOrdinalCodec<DocumentType>(DocumentType.class));
    }
}
