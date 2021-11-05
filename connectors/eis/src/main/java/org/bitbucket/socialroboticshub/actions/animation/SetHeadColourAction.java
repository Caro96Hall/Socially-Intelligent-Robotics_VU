package org.bitbucket.socialroboticshub.actions.animation;

import java.util.List;

import eis.iilang.Parameter;

public class SetHeadColourAction extends SetColourAction {
	public final static String NAME = "setHeadColour";

	/**
	 * @param parameters A list of 1 identifier: rainbow (which is a short effect),
	 *                   white, red, green, blue, yellow, magenta, or cyan.
	 */
	public SetHeadColourAction(final List<Parameter> parameters) {
		super("head", parameters);
	}
}
