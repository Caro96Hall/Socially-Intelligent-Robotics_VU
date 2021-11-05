package org.bitbucket.socialroboticshub.actions.audiovisual;

import java.util.List;

import org.bitbucket.socialroboticshub.actions.RobotAction;

import eis.iilang.Identifier;
import eis.iilang.Parameter;

public class SetLanguageAction extends RobotAction {
	public final static String NAME = "setLanguage";

	/**
	 * @param parameters A list of 1 identifier: the language identifier to be used
	 *                   in DialogFlow and/or the text-to-speech.
	 */
	public SetLanguageAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 1) && (getParameters().get(0) instanceof Identifier);
	}

	@Override
	public String getTopic() {
		return "audio_language";
	}

	@Override
	public String getData() {
		return EIStoString(getParameters().get(0));
	}

	@Override
	public String getExpectedEvent() {
		return "LanguageChanged";
	}
}
