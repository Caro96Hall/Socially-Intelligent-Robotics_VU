package org.bitbucket.socialroboticshub.actions.audiovisual;

import java.util.List;

import eis.iilang.Identifier;
import eis.iilang.Parameter;

public class PlayRawAudioAction extends PlayAudioAction {
	// playAudio instead of playRawAudio is used for backward compatibility
	public final static String NAME = "playAudio";

	/**
	 * @param parameters A list of 1 identifier, the name of the audio file
	 */
	public PlayRawAudioAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 1) && (getParameters().get(0) instanceof Identifier);
	}
}