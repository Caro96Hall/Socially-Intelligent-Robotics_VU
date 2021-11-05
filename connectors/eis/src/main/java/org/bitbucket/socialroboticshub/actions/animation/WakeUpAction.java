package org.bitbucket.socialroboticshub.actions.animation;

import org.bitbucket.socialroboticshub.actions.RobotAction;

public class WakeUpAction extends RobotAction {
	public final static String NAME = "wakeUp";

	public WakeUpAction() {
		super(null);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getTopic() {
		return "action_wakeup";
	}

	@Override
	public String getData() {
		return "";
	}

	@Override
	public String getExpectedEvent() {
		return "WakeUpStarted";
	}
}
