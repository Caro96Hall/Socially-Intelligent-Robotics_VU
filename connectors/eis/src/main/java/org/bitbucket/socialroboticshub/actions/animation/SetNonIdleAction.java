package org.bitbucket.socialroboticshub.actions.animation;

import org.bitbucket.socialroboticshub.actions.RobotAction;

public class SetNonIdleAction extends RobotAction {
	public final static String NAME = "setNonIdle";

	public SetNonIdleAction() {
		super(null);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getTopic() {
		return "action_idle";
	}

	@Override
	public String getData() {
		return "";
	}

	@Override
	public String getExpectedEvent() {
		return "SetNonIdle";
	}
}
