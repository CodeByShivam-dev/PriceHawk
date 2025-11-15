package com.pricehawk.exception;

/**
 * Custom exception for invalid user queries.
 */
public class InvalidQueryException extends RuntimeException
{
    public InvalidQueryException(String message)
    {
        super(message);
    }
}
