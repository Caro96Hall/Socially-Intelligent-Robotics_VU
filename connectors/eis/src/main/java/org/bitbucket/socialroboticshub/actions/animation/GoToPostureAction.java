package org.bitbucket.socialroboticshub.actions.animation;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;

public class GoToPostureAction extends RobotAction {
	public final static String NAME = "goToPosture";
	private final static String DEFAULT_SPEED = "100";

	/**
	 * @param parameters A list of 1 identifier and an optional numeral representing
	 *                   the target posture to go to and the movement speed (value
	 *                   between 1 and 100).
	 */
	public GoToPostureAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		final int params = getParameters().size();
		boolean valid = (params == 1 || params == 2);
		if (valid) {
			valid &= (getParameters().get(0) instanceof Identifier);
			if (params == 2) {
				valid &= (getParameters().get(1) instanceof Numeral);
			}
		}
		return valid;
	}

	@Override
	public String getTopic() {
		return "action_posture";
	}

	@Override
	public String getData() {
		final String posture = EIStoString(getParameters().get(0));
		final String speed = (getParameters().size() == 1) ? DEFAULT_SPEED : EIStoString(getParameters().get(1));

		return (posture + ";" + speed);
	}

	@Override
	public String getExpectedEvent() {
		return "GoToPostureStarted";
	}
}