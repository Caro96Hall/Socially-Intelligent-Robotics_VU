package org.bitbucket.socialroboticshub.actions.memory;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;

public class SetSession extends RobotAction {
	public final static String NAME = "setSession";

	/**
	 * @param parameters interactant id and session id.
	 */
	public SetSession(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 2) && (getParameters().get(0) instanceof Identifier)
				&& (getParameters().get(1) instanceof Numeral);
	}

	@Override
	public String getTopic() {
		return "memory_set_session";
	}

	@Override
	public String getData() {
		return EIStoString(getParameters().get(0)) + ";" + EIStoString(getParameters().get(1));
	}

	@Override
	public String getExpectedEvent() {
		return "SessionSet";
	}
}
