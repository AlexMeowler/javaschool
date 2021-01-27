package org.retal.logiweb.domain.enums;

/**
 * Enumeration for representing cargo status.
 * @author Alexander Retivov
 *
 */
public enum DriverStatus {
  RESTING, ON_SHIFT {
    @Override
    public String toString() {
      return super.toString().replace("_", " ");
    }
  },
  DRIVING, LOADING_AND_UNLOADING_CARGO {
    @Override
    public String toString() {
      return super.toString().replace("_", " ");
    }
  };
}
