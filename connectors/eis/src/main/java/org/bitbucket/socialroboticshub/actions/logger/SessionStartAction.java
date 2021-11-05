package org.bitbucket.socialroboticshub.actions.logger;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Parameter;

public class SessionStartAction extends RobotAction {
	public final static String NAME = "sessionStart";

	/**
	 * @param parameters A list of 1 parameter: some session identifier
	 */
	public SessionStartAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 1);
	}

	@Override
	public String getTopic() {
		return "session_start";
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
