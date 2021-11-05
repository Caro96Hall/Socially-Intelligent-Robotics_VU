package org.bitbucket.socialroboticshub;

enum DeviceType {
	CAMERA("cam"), MICROPHONE("mic"), ROBOT("robot"), PUPPET("puppet"), SPEAKER("speaker"), GUI("gui_controller"),
	BROWSER("browser"), GOOGLE_ASSISTANT("ga"), LOGGER("logger");

	private final String name;

	DeviceType(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public static DeviceType fromString(final String string) {
		switch (string) {
		case "cam":
			return CAMERA;
		case "mic":
			return MICROPHONE;
		case "robot":
			return ROBOT;
		case "puppet":
			return PUPPET;
		case "speaker":
			return SPEAKER;
		case "gui_controller":
			return GUI;
		case "browser":
			return BROWSER;
		case "ga":
			return GOOGLE_ASSISTANT;
		case "logger":
			return LOGGER;
		default:
			return null;
		}
	}

	public static int size() {
		return 9;
	}
}
