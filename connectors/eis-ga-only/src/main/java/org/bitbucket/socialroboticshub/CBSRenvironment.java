package org.bitbucket.socialroboticshub;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bitbucket.socialroboticshub.DetectionResultProto.DetectionResult;
import org.bitbucket.socialroboticshub.actions.AssistantAction;
import org.bitbucket.socialroboticshub.ga.GoogleAssistant;

import com.google.protobuf.Value;

import eis.EIDefaultImpl;
import eis.PerceptUpdate;
import eis.exceptions.ActException;
import eis.exceptions.ManagementException;
import eis.iilang.Action;
import eis.iilang.EnvironmentState;
import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import eis.iilang.Percept;
import eis.iilang.TruthValue;

public class CBSRenvironment extends EIDefaultImpl {
	private static final long serialVersionUID = 1L;
	protected Map<String, Parameter> parameters;
	protected BlockingQueue<Percept> perceptQueue;
	protected List<Percept> previousPercepts;
	protected GoogleAssistant ga;

	@Override
	public void init(final Map<String, Parameter> parameters) throws ManagementException {
		super.init(parameters);
		this.parameters = parameters;
		this.perceptQueue = new LinkedBlockingQueue<>();
		this.previousPercepts = new ArrayList<>(0);
		setState(EnvironmentState.PAUSED);

		try {
			// initialise the embedded GoogleAssistant client
			this.ga = new GoogleAssistant(this, getParameter("subdomain"), getParameter("flowagent"));
			// we're ready (if the GA didn't kill us); announce the entity
			if (getState() != EnvironmentState.KILLED) {
				setState(EnvironmentState.RUNNING);
				addEntity("robot", "robot");
			}
		} catch (final Exception e) {
			throw new ManagementException("Unable to initialise the robot entity", e);
		}
	}

	private String getParameter(final String name) {
		final Parameter param = this.parameters.get(name);
		return (param instanceof Identifier) ? ((Identifier) param).getValue().trim() : "";
	}

	@Override
	public void kill() throws ManagementException {
		super.kill();
		this.ga.disconnect();
		this.perceptQueue.clear();
	}

	@Override
	public PerceptUpdate getPerceptsForEntity(final String entity) {
		final List<Percept> percepts = new LinkedList<>();
		this.perceptQueue.drainTo(percepts);

		final List<Percept> addList = new ArrayList<>(percepts);
		addList.removeAll(this.previousPercepts);
		final List<Percept> delList = new ArrayList<>(this.previousPercepts);
		delList.removeAll(percepts);

		this.previousPercepts = percepts;
		return new PerceptUpdate(addList, delList);
	}

	@Override
	protected boolean isSupportedByEnvironment(final Action action) {
		final AssistantAction assistantAction = AssistantAction.getAction(action);
		return (assistantAction != null && assistantAction.isValid());
	}

	@Override
	protected boolean isSupportedByType(final Action action, final String type) {
		return isSupportedByEnvironment(action);
	}

	@Override
	protected boolean isSupportedByEntity(final Action action, final String entity) {
		return isSupportedByEnvironment(action);
	}

	@Override
	public void performEntityAction(final Action action, final String entity) throws ActException {
		final AssistantAction assistantAction = AssistantAction.getAction(action);
		if (assistantAction != null && assistantAction.isValid()) {
			this.ga.setResponse(assistantAction.getTopic(), assistantAction.getData());
		} else {
			throw new ActException(ActException.FAILURE, "Not able to perform " + action.toProlog());
		}
	}

	/**
	 * Queues the intent information as a percept to be received by the agent.
	 *
	 * @param result The detectionresult protobuf
	 */
	public void addIntent(final DetectionResult result) {
		final Identifier intent = new Identifier(result.getIntent());
		final Parameter params = toEIS(result.getParametersMap());
		final Numeral confidence = new Numeral(result.getConfidence());
		final Identifier text = new Identifier(result.getText());
		final Identifier source = new Identifier(result.getSource());
		this.perceptQueue.add(new Percept("intent", intent, params, confidence, text, source));
	}

	/**
	 * Queues the robot event information as a percept to be received by the agent.
	 *
	 * @param event The event name
	 */
	public void addEvent(final String event) {
		this.perceptQueue.add(new Percept("event", new Identifier(event)));
	}

	private static Parameter toEIS(final Value value) {
		switch (value.getKindCase()) {
		case STRING_VALUE:
			return new Identifier(value.getStringValue());
		case NUMBER_VALUE:
			return new Numeral(value.getNumberValue());
		case BOOL_VALUE:
			return new TruthValue(value.getBoolValue());
		case STRUCT_VALUE:
			return toEIS(value.getStructValue().getFieldsMap());
		case LIST_VALUE:
			return toEIS(value.getListValue().getValuesList());
		default:
			throw new IllegalArgumentException("Unsupported value: " + value);
		}
	}

	private static Parameter toEIS(final Map<String, Value> struct) {
		final List<Parameter> elements = new ArrayList<>(struct.size());
		for (final String key : struct.keySet()) {
			final Parameter param = toEIS(struct.get(key));
			elements.add(new Function("=", new Identifier(key), param));
		}
		return new ParameterList(elements);
	}

	private static Parameter toEIS(final List<Value> valueList) {
		final List<Parameter> elements = new ArrayList<>(valueList.size());
		for (final Value value : valueList) {
			final Parameter param = toEIS(value);
			elements.add(param);
		}
		return new ParameterList(elements);
	}
}
