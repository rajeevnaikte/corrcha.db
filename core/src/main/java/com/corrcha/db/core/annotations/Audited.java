package com.corrcha.db.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This will indicate whether the entity need to be audited or not
 * @author Rajeev Naik
 * @since 2017-06-07
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited
{
    /**
     * Optionally audit table name can be provided.
     * @return audit table name
     */
    String table() default "";

    /**
     * If audit table is in different DB space, then it can be specified here.
     * @return DB name/space/schema
     */
    String dbSpace() default "";

    /**
     * ConsistencyLevel for audit inserts
     * @return ConsistencyLevel
     */
    ConsistencyLevel consistencyLevel() default ConsistencyLevel.BOTH;
}
