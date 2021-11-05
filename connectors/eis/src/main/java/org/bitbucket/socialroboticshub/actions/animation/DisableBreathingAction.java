package org.bitbucket.socialroboticshub.actions.animation;

import java.util.List;

import eis.iilang.Parameter;

public class DisableBreathingAction extends BreathingAction {
	public final static String NAME = "disableBreathing";

	/**
	 *
	 * @param parameters A list of 1 optional identifier, the body part that should
	 *                   be affected by the breathing behavior
	 */
	public DisableBreathingAction(final List<Parameter> parameters) {
		super(parameters, false);
	}
}
