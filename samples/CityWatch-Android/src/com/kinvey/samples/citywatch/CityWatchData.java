/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.samples.citywatch;

public class CityWatchData {

	public enum Category {
		FIRE("Fire"), Flood("Flood"), EMERGENCY("Emergency"), HEALTH("Health"), INFRASTRUCTURE(
				"Infrastructure"), OBSTRUCTION("Obstruction"), OTHER("Other"), WEATHER(
				"Weather"), WILDLIFE("Wildlife");

		private String displayName;

		private Category(String disp) {
			this.displayName = disp;
		}

		public String getDisplayName() {
			return this.displayName;
		}
	}

	public enum Severity {
		HIGH("High"), MEDIUM("Medium"), LOW("Low"), NONE("None");

		private String displayName;

		private Severity(String disp) {
			this.displayName = disp;
		}

		public String getDisplayName() {
			return this.displayName;
		}
	}

	public enum Risk {
		HIGH("High"), MEDIUM("Medium"), LOW("Low"), NONE("None");

		private String displayName;

		private Risk(String disp) {
			this.displayName = disp;
		}

		public String getDisplayName() {
			return this.displayName;
		}
	}

}

