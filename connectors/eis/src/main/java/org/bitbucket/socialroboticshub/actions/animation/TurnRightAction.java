package org.bitbucket.socialroboticshub.actions.animation;

import java.util.List;

import eis.iilang.Parameter;

public class TurnRightAction extends TurnAction {
	public final static String NAME = "turnRight";

	public TurnRightAction(final List<Parameter> parameters) {
		super(parameters, "right");
	}
}
