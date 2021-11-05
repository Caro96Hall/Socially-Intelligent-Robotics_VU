package org.bitbucket.socialroboticshub.actions.animation;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;

public class SetStiffnessAction extends RobotAction {
	public final static String NAME = "setStiffness";
	private final static String DEFAULT_DURATION = "1000";

	/**
	 * @param parameters A list of 1 identifier representing a list of joint chains
	 *                   of the robot, 1 numeral representing the stiffness value
	 *                   (between 1 and 100), and an optional numeral representing
	 *                   the duration of execution (in milliseconds).
	 */
	public SetStiffnessAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		final int params = getParameters().size();
		boolean valid = (params == 2 || params == 3);
		if (valid) {
			valid &= (getParameters().get(0) instanceof ParameterList) && (getParameters().get(1) instanceof Numeral);
			if (params == 3) {
				valid &= (getParameters().get(2) instanceof Numeral);
			}
		}
		return valid;
	}

	@Override
	public String getTopic() {
		return "action_stiffness";
	}

	@Override
	public String getData() {
		final String jointChains = EIStoString(getParameters().get(0));
		final String stiffness = EIStoString(getParameters().get(1));
		final String duration = (getParameters().size() == 2) ? DEFAULT_DURATION : EIStoString(getParameters().get(2));

		return (jointChains + ";" + stiffness + ";" + duration);
	}

	@Override
	public String getExpectedEvent() {
		return "SetStiffnessStarted";
	}
}
