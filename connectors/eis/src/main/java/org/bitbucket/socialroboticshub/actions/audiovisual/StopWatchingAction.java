package org.bitbucket.socialroboticshub.actions.audiovisual;

import org.bitbucket.socialroboticshub.actions.RobotAction;

public class StopWatchingAction extends RobotAction {
	public final static String NAME = "stopWatching";

	public StopWatchingAction() {
		super(null);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getTopic() {
		return "action_video";
	}

	@Override
	public String getData() {
		return "-1";
	}

	@Override
	public String getExpectedEvent() {
		return "WatchingDone";
	}
}
