package org.bitbucket.socialroboticshub.actions.animation;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Identifier;
import eis.iilang.Parameter;

abstract class BreathingAction extends RobotAction {
	protected final String enable;

	/**
	 *
	 * @param parameters A list of 1 optional identifier, the body part that should
	 *                   be affected by the breathing behavior
	 * @param enable     boolean that signals to enable or disable the breathing
	 *                   behavior
	 */
	BreathingAction(final List<Parameter> parameters, final boolean enable) {
		super(parameters);
		this.enable = enable ? "1" : "0";
	}

	@Override
	public boolean isValid() {
		return getParameters().isEmpty()
				|| (getParameters().size() == 1 && getParameters().get(0) instanceof Identifier);
	}

	@Override
	public String getTopic() {
		return "action_set_breathing";
	}

	@Override
	public String getData() {
		return getParameters().isEmpty() ? ("Body;" + this.enable)
				: (EIStoString(getParameters().get(0)) + ";" + this.enable);
	}

	@Override
	public String getExpectedEvent() {
		return this.enable.equals("1") ? "BreathingEnabled" : "BreathingDisabled";
	}
}
