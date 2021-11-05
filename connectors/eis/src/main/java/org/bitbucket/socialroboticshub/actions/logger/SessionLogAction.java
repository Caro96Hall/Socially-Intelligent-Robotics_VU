package org.bitbucket.socialroboticshub.actions.logger;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Parameter;

public class SessionLogAction extends RobotAction {
	public final static String NAME = "sessionLog";

	/**
	 * @param parameters A list of 1 parameter: a message to log to the active
	 *                   session
	 */
	public SessionLogAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 1);
	}

	@Override
	public String getTopic() {
		return "session_log";
	}

	@Override
	public String getData() {
		return EIStoString(getParameters().get(0));
	}

	@Override
	public String getExpectedEvent() {
		return null;
	}
}
