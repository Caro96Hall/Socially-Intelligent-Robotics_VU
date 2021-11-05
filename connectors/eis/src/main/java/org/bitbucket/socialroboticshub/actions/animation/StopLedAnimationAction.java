package org.bitbucket.socialroboticshub.actions.animation;

import org.bitbucket.socialroboticshub.actions.RobotAction;

public class StopLedAnimationAction extends RobotAction {
	public final static String NAME = "stopLedAnimation";

	public StopLedAnimationAction() {
		super(null);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getTopic() {
		return "action_led_animation";
	}

	@Override
	public String getData() {
		return "stop";
	}

	@Override
	public String getExpectedEvent() {
		return "LedAnimationDone";
	}
}
