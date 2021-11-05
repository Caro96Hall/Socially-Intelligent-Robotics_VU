package org.bitbucket.socialroboticshub.actions.animation;

import java.util.List;

import eis.iilang.Parameter;

public class SetEarColourAction extends SetColourAction {
	public final static String NAME = "setEarColour";

	/**
	 * @param parameters A list of 1 identifier: rainbow (which is a short effect),
	 *                   white, red, green, blue, yellow, magenta, or cyan.
	 */
	public SetEarColourAction(final List<Parameter> parameters) {
		super("ear", parameters);
	}
}
