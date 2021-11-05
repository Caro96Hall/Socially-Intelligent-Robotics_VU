package org.bitbucket.socialroboticshub.actions.audiovisual;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Numeral;
import eis.iilang.Parameter;

public class StartWatchingAction extends RobotAction {
	public final static String NAME = "startWatching";

	/**
	 * @param parameters A list of 1 identifier indicating the timeout after which
	 *                   to stop watching.
	 */
	public StartWatchingAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 1) && (getParameters().get(0) instanceof Numeral);
	}

	@Override
	public String getTopic() {
		return "action_video";
	}

	@Override
	public String getData() {
		return EIStoString(getParameters().get(0));
	}

	@Override
	public String getExpectedEvent() {
		return "WatchingStarted";
	}
}
