package org.ccci.gto.persistence.hibernate;

/**
 * Interface that marks a dialect aware of certain error codes. When you have to do a low level check of the
 * exception you are trying to handle, you can implement this in this interface, so you can encapsulate the specific
 * error codes for the specific dialects.
 */
public interface ErrorCodeAware {
    boolean isDeadlockErrorCode(int error);
}
