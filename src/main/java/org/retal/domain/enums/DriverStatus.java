package org.retal.domain.enums;

public enum DriverStatus {
	RESTING, 
	ON_SHIFT {
		@Override
		public String toString() {
			return "ON SHIFT";
		}
	}, 
	DRIVING;
}
