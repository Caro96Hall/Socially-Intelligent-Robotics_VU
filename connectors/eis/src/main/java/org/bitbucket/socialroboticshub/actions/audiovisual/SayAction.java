package org.bitbucket.socialroboticshub.actions.audiovisual;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Identifier;
import eis.iilang.Parameter;

public class SayAction extends RobotAction {
	public final static String NAME = "say";

	/**
	 * @param parameters A list of 1 identifier, see
	 *                   {@link http://doc.aldebaran.com/2-5/naoqi/audio/altexttospeech-tuto.html}
	 */
	public SayAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 1) && (getParameters().get(0) instanceof Identifier);
	}

	@Override
	public String getTopic() {
		return "action_" + NAME;
	}

	@Override
	public String getData() {
		return EIStoString(getParameters().get(0));
	}

	@Override
	public String getExpectedEvent() {
		return "TextStarted";
	}
}
