package org.bitbucket.socialroboticshub.actions.logger;

import org.bitbucket.socialroboticshub.actions.RobotAction;

public class SessionEndAction extends RobotAction {
	public final static String NAME = "sessionEnd";

	public SessionEndAction() {
		super(null);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getTopic() {
		return "session_end";
	}

	@Override
	public String getData() {
		return "";
	}

	@Override
	public String getExpectedEvent() {
		return null;
	}
}
