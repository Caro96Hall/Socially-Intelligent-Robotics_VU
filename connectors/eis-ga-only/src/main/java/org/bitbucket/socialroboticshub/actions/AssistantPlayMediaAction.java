package org.bitbucket.socialroboticshub.actions;

import java.util.List;

import eis.iilang.Identifier;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;

public class AssistantPlayMediaAction extends AssistantAction {
	public final static String NAME = "assistantPlayMedia";

	/**
	 * @param parameters A list of 3 identifiers and a (required) parameterlist: the
	 *                   text to show (and say), the name of the audio to play, the
	 *                   url to the audio (MP3 on HTTPS), and the response hints.
	 */
	public AssistantPlayMediaAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 4) && (getParameters().get(0) instanceof Identifier)
				&& (getParameters().get(1) instanceof Identifier) && (getParameters().get(2) instanceof Identifier)
				&& (getParameters().get(3) instanceof ParameterList);
	}

	@Override
	public String getTopic() {
		return "assistant_play_media";
	}

	@Override
	public String getData() {
		return EIStoString(getParameters().get(0)) + "|" + EIStoString(getParameters().get(1)) + "|"
				+ EIStoString(getParameters().get(2)) + "|" + EIStoString(getParameters().get(3));
	}
}
