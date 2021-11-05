package org.bitbucket.socialroboticshub.actions.animation;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;

public class StartMotionRelayingAction extends RobotAction {
	public final static String NAME = "startMotionRelaying";
	private final static String FRAMERATE = "5";

	/**
	 * @param parameters a list of joints and or joint chains of the robot
	 *                   respectively, and 1 optional numeral representing framerate
	 *                   (how often to relay per second).
	 */
	public StartMotionRelayingAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		final int params = getParameters().size();
		boolean valid = (params == 1 || params == 2);
		if (valid) {
			valid &= (getParameters().get(0) instanceof ParameterList);
			if (params == 2) {
				valid &= (getParameters().get(1) instanceof Numeral);
			}
		}
		return valid;
	}

	@Override
	public String getTopic() {
		return "action_relay_motion";
	}

	@Override
	public String getData() {
		final String joints = EIStoString(getParameters().get(0));
		final String framerate = (getParameters().size() == 1) ? FRAMERATE : EIStoString(getParameters().get(1));

		return ("start;" + joints + ";" + framerate);
	}

	@Override
	public String getExpectedEvent() {
		return "RelayMotionStarted";
	}

}
