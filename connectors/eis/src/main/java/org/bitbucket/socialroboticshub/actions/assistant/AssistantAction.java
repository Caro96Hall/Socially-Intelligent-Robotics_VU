package org.bitbucket.socialroboticshub.actions.assistant;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Parameter;

abstract class AssistantAction extends RobotAction {
	protected AssistantAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public String getExpectedEvent() {
		return "ShownOnAssistant";
	}
}
