package org.bitbucket.socialroboticshub.actions.audiovisual;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;

public class SetSpeechParamAction extends RobotAction {
	public final static String NAME = "setSpeechParam";

	/**
	 * @param parameters A list of 2 identifiers: a (string) key and a corresponding
	 *                   (float) value.
	 */
	public SetSpeechParamAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 2) && (getParameters().get(0) instanceof Identifier)
				&& (getParameters().get(1) instanceof Numeral);
	}

	@Override
	public String getTopic() {
		return "action_speech_param";
	}

	@Override
	public String getData() {
		return EIStoString(getParameters().get(0)) + ";" + EIStoString(getParameters().get(1));
	}

	@Override
	public String getExpectedEvent() {
		return "SetSpeechParamDone";
	}
}
