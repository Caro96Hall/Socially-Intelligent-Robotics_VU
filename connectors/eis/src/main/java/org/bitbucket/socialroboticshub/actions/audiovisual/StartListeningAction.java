package org.bitbucket.socialroboticshub.actions.audiovisual;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;

public class StartListeningAction extends RobotAction {
	public final static String NAME = "startListening";

	/**
	 * @param parameters A list of 1 or 2 identifier indicating the timeout after
	 *                   which to stop listening and the optional context.
	 */
	public StartListeningAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		final int params = getParameters().size();
		boolean valid = (params == 1 || params == 2);
		if (valid) {
			valid &= (getParameters().get(0) instanceof Numeral);
			if (params == 2) {
				valid &= (getParameters().get(1) instanceof Identifier);
			}
		}
		return valid;
	}

	public String getContext() {
		return (getParameters().size() == 2) ? EIStoString(getParameters().get(1)) : "";
	}

	@Override
	public String getTopic() {
		return "action_audio";
	}

	@Override
	public String getData() {
		return EIStoString(getParameters().get(0));
	}

	@Override
	public String getExpectedEvent() {
		return "ListeningStarted";
	}
}
