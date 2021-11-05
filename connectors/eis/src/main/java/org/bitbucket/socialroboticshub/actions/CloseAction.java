package org.bitbucket.socialroboticshub.actions;

public class CloseAction extends RobotAction {
	/**
	 * A special action that signals the producer to shut down
	 */
	public CloseAction() {
		super(null);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getTopic() {
		return null;
	}

	@Override
	public String getData() {
		return null;
	}

	@Override
	public String getExpectedEvent() {
		return null;
	}
}
