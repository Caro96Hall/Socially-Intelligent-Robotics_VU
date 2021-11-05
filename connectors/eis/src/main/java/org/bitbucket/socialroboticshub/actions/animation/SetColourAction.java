package org.bitbucket.socialroboticshub.actions.animation;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Identifier;
import eis.iilang.Parameter;

abstract class SetColourAction extends RobotAction {
	protected final String part;

	SetColourAction(final String part, final List<Parameter> parameters) {
		super(parameters);
		this.part = part;
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 1) && (getParameters().get(0) instanceof Identifier);
	}

	@Override
	public String getTopic() {
		return "action_" + this.part + "colour";
	}

	@Override
	public String getData() {
		return EIStoString(getParameters().get(0));
	}

	@Override
	public String getExpectedEvent() {
		final String part = this.part.substring(0, 1).toUpperCase() + this.part.substring(1);
		return (part + "ColourStarted");
	}
}
