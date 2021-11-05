package org.bitbucket.socialroboticshub.actions.audiovisual;

import java.util.List;

import eis.iilang.Parameter;

public class SayAnimatedAction extends SayAction {
	public final static String NAME = "sayAnimated";

	/**
	 * @param parameters A list of 1 identifier, see
	 *                   {@link http://doc.aldebaran.com/2-5/naoqi/audio/alanimatedspeech.html}
	 */
	public SayAnimatedAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public String getTopic() {
		return super.getTopic() + "_animated";
	}
}
