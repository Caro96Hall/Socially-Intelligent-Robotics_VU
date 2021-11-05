package org.bitbucket.socialroboticshub.actions.animation;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;

public class StartLedAnimationAction extends RobotAction {
	public final static String NAME = "startLedAnimation";

	/**
	 * @param parameters a led group name (eyes, chest, feet, all), an animation
	 *                   type (rotate, blink, alternate), a list of colors, and a
	 *                   speed setting (millisecond).
	 */
	public StartLedAnimationAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return getParameters().size() == 4 && (getParameters().get(0) instanceof Identifier)
				&& (getParameters().get(1) instanceof Identifier) && (getParameters().get(2) instanceof ParameterList)
				&& (getParameters().get(3) instanceof Numeral);
	}

	@Override
	public String getTopic() {
		return "action_led_animation";
	}

	@Override
	public String getData() {
		final String ledGroup = EIStoString(getParameters().get(0));
		final String animType = EIStoString(getParameters().get(1));
		final String colors = EIStoString(getParameters().get(2));
		final String speed = EIStoString(getParameters().get(3));

		return ("start;" + ledGroup + ";" + animType + ";" + colors + ";" + speed);
	}

	@Override
	public String getExpectedEvent() {
		return "LedAnimationStarted";
	}
}
