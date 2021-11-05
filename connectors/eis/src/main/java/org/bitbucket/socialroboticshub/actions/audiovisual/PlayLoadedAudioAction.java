package org.bitbucket.socialroboticshub.actions.audiovisual;

import java.util.List;

import eis.iilang.Numeral;
import eis.iilang.Parameter;

public class PlayLoadedAudioAction extends PlayAudioAction {
	public final static String NAME = "playLoadedAudio";

	/**
	 * @param parameters A list of 1 identifier, the numeral identifier of the
	 *                   pre-loaded audio
	 */
	public PlayLoadedAudioAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		return (getParameters().size() == 1) && (getParameters().get(0) instanceof Numeral);
	}
}
