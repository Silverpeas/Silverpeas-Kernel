package org.silverpeas.kernel.exception;

import org.silverpeas.kernel.SilverpeasRuntimeException;

/**
 * Exception thrown when an object or an operation execution doesn't match the expectations of
 * the invoker. An example of expectation can be the type a bean should satisfy or the state in
 * which an object should be.
 * @author mmoquillon
 */
public class ExpectationViolationException extends SilverpeasRuntimeException {
  public ExpectationViolationException(String message) {
    super(message);
  }

  public ExpectationViolationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExpectationViolationException(Throwable cause) {
    super(cause);
  }
}
  