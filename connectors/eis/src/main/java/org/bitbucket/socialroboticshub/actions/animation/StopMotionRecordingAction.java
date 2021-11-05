package org.bitbucket.socialroboticshub.actions.animation;

import org.bitbucket.socialroboticshub.actions.RobotAction;

public class StopMotionRecordingAction extends RobotAction {
	public final static String NAME = "stopMotionRecording";

	public StopMotionRecordingAction() {
		super(null);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getTopic() {
		return "action_record_motion";
	}

	@Override
	public String getData() {
		return "stop";
	}

	@Override
	public String getExpectedEvent() {
		return "RecordMotionDone";
	}
}
