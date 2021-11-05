package org.bitbucket.socialroboticshub.actions.animation;

import org.bitbucket.socialroboticshub.actions.RobotAction;

public class StopMotionRelayingAction extends RobotAction {
	public final static String NAME = "stopMotionRelaying";

	public StopMotionRelayingAction() {
		super(null);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getTopic() {
		return "action_relay_motion";
	}

	@Override
	public String getData() {
		return "stop";
	}

	@Override
	public String getExpectedEvent() {
		return "RelayMotionDone";
	}
}
