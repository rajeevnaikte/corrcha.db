package com.corrcha.db.core.annotations;

/**
 * This class has two type of consistency level for auditing.
 * SINGLE - means Entity table transaction and audit table transaction are done separately.
 * BOTH - means both Entity table and audit table transactions are done together
 * @author Rajeev Naik
 * @since 2017-06-07
 */
public enum ConsistencyLevel
{
    SINGLE, BOTH;
}
