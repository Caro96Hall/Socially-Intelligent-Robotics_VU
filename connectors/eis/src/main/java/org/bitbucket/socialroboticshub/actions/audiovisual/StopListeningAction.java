package org.bitbucket.socialroboticshub.actions.audiovisual;

import org.bitbucket.socialroboticshub.actions.RobotAction;

public class StopListeningAction extends RobotAction {
	public final static String NAME = "stopListening";

	public StopListeningAction() {
		super(null);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getTopic() {
		return "action_audio";
	}

	@Override
	public String getData() {
		return "-1";
	}

	@Override
	public String getExpectedEvent() {
		return "ListeningDone";
	}
}
