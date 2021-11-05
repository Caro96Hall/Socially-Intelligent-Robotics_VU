package org.bitbucket.socialroboticshub.actions.animation;

import java.util.List;

import eis.iilang.Parameter;

public class TurnLeftAction extends TurnAction {
	public final static String NAME = "turnLeft";

	public TurnLeftAction(final List<Parameter> parameters) {
		super(parameters, "left");
	}
}
