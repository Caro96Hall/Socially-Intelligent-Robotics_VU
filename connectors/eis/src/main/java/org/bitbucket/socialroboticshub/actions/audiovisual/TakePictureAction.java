package org.bitbucket.socialroboticshub.actions.audiovisual;

import org.bitbucket.socialroboticshub.actions.RobotAction;

public class TakePictureAction extends RobotAction {
	public final static String NAME = "takePicture";

	public TakePictureAction() {
		super(null);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getTopic() {
		return "action_take_picture";
	}

	@Override
	public String getData() {
		return "";
	}

	@Override
	public String getExpectedEvent() {
		return null;
	}
}
