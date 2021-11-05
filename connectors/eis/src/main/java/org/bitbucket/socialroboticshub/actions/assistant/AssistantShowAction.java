package org.bitbucket.socialroboticshub.actions.assistant;

import java.util.ArrayList;
import java.util.List;

import eis.iilang.Identifier;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;

public class AssistantShowAction extends AssistantAction {
	public final static String NAME = "assistantShow";

	/**
	 * @param parameters A list of 1 identifier and optionally a parameterlist: the
	 *                   text to show (and say) and the response hints.
	 */
	public AssistantShowAction(final List<Parameter> parameters) {
		super(parameters);
	}

	@Override
	public boolean isValid() {
		final int params = getParameters().size();
		boolean valid = (params == 1 || params == 2);
		if (valid) {
			valid &= (getParameters().get(0) instanceof Identifier);
			if (params == 2) {
				valid &= (getParameters().get(1) instanceof ParameterList);
			}
		}
		return valid;
	}

	@Override
	public String getTopic() {
		return "assistant_show";
	}

	@Override
	public String getData() {
		final ParameterList list = (getParameters().size() == 2) ? (ParameterList) getParameters().get(1)
				: new ParameterList(new ArrayList<>(0));
		return EIStoString(getParameters().get(0)) + "|" + EIStoString(list);
	}
}
