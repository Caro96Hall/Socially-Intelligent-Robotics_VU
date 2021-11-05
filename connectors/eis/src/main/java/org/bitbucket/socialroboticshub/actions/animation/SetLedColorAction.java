package org.bitbucket.socialroboticshub.actions.animation;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;

public class SetLedColorAction extends RobotAction {
	public final static String NAME = "setLedColor";
	private final static String DEFAULT_DURATION = "0";

	/**
	 * @param parameters A list of 1 identifier representing a list leds of the
	 *                   robot, a list of colors the leds should get, and an
	 *                   optional numeral representing the duration of execution (in
	 *                   milliseconds).
	 */
	public SetLedColorAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		final int params = getParameters().size();
		boolean valid = (params == 2 || params == 3);
		if (valid) {
			valid &= (getParameters().get(0) instanceof ParameterList)
					&& (getParameters().get(1) instanceof ParameterList);
			if (params == 3) {
				valid &= (getParameters().get(2) instanceof Numeral);
			}
		}
		return valid;
	}

	@Override
	public String getTopic() {
		return "action_led_color";
	}

	@Override
	public String getData() {
		final String leds = EIStoString(getParameters().get(0));
		final String colors = EIStoString(getParameters().get(1));
		final String duration = (getParameters().size() == 2) ? DEFAULT_DURATION : EIStoString(getParameters().get(2));

		return (leds + ";" + colors + ";" + duration);
	}

	@Override
	public String getExpectedEvent() {
		return "LedColorStarted";
	}
}
