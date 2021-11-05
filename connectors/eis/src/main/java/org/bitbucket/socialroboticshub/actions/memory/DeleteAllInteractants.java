package org.bitbucket.socialroboticshub.actions.memory;

import org.bitbucket.socialroboticshub.actions.RobotAction;

public class DeleteAllInteractants extends RobotAction {
	public final static String NAME = "deleteAllInteractants";

	public DeleteAllInteractants() {
		super(null);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getTopic() {
		return "memory_delete_all_interactants";
	}

	@Override
	public String getData() {
		return "";
	}

	@Override
	public String getExpectedEvent() {
		return "AllInteractantsDeleted";
	}
}
