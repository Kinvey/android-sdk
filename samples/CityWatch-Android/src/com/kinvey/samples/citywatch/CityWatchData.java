/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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

