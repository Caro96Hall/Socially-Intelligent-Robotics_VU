package org.bitbucket.socialroboticshub.actions;

import java.util.List;

import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import eis.iilang.TruthValue;

public abstract class AssistantAction {
	private final List<Parameter> parameters;

	protected AssistantAction(final List<Parameter> parameters) {
		this.parameters = parameters;
	}

	protected List<Parameter> getParameters() {
		return this.parameters;
	}

	public abstract boolean isValid();

	public abstract String getTopic();

	public abstract String getData();

	public static AssistantAction getAction(final Action action) {
		final List<Parameter> parameters = action.getParameters();
		switch (action.getName()) {
		case AssistantShowAction.NAME:
			return new AssistantShowAction(parameters);
		case AssistantShowCardAction.NAME:
			return new AssistantShowCardAction(parameters);
		case AssistantPlayMediaAction.NAME:
			return new AssistantPlayMediaAction(parameters);
		default:
			return null;
		}
	}

	protected static String EIStoString(final Parameter param) {
		if (param instanceof Identifier) {
			return ((Identifier) param).getValue();
		} else if (param instanceof Numeral) {
			return String.valueOf(((Numeral) param).getValue());
		} else if (param instanceof TruthValue) {
			return ((TruthValue) param).getBooleanValue() ? "1" : "0";
		} else if (param instanceof ParameterList) {
			final ParameterList list = (ParameterList) param;
			final int size = list.size();
			final StringBuilder result = new StringBuilder("[");
			for (int i = 0; i < size; i++) {
				final Parameter item = list.get(i);
				String subresult = EIStoString(item);
				if (item instanceof Identifier) {
					subresult = '"' + subresult + '"';
				}
				result.append(subresult);
				if (i < (size - 1)) {
					result.append(",");
				}
			}
			return result.append("]").toString();
		} else {
			throw new IllegalArgumentException("Unknown parameter type '" + param.getClass() + "'.");
		}
	}
}
