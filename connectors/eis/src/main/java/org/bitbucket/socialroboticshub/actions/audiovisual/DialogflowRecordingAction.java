package org.bitbucket.socialroboticshub.actions.audiovisual;

import org.bitbucket.socialroboticshub.actions.RobotAction;

abstract class DialogflowRecordingAction extends RobotAction {
	private final boolean enable;

	DialogflowRecordingAction(final boolean enable) {
		super(null);
		this.enable = enable;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getTopic() {
		return "dialogflow_record";
	}

	@Override
	public String getData() {
		return this.enable ? "1" : "0";
	}

	@Override
	public String getExpectedEvent() {
		return null;
	}
}
