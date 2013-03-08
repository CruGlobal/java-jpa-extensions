package org.ccci.gto.persistence;

import java.util.Set;

/**
 * Interface that marks a dialect aware of certain error codes. When you have to
 * do a low level check of the exception you are trying to handle, you can
 * implement this in this interface, so you can encapsulate the specific error
 * codes for the specific dialects.
 * 
 * @author Jelle Victoor
 * @version 05-jul-2011
 */
public interface ErrorCodeAware {
    public Set<Integer> getDeadlockErrorCodes();
}
