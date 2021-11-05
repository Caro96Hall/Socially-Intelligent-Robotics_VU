package org.bitbucket.socialroboticshub;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.prefs.Preferences;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.bitbucket.socialroboticshub.DetectionResultProto.DetectionResult;
import org.bitbucket.socialroboticshub.actions.RobotAction;
import org.bitbucket.socialroboticshub.actions.audiovisual.SetLanguageAction;
import org.bitbucket.socialroboticshub.actions.audiovisual.StartListeningAction;
import org.bitbucket.socialroboticshub.actions.audiovisual.StopListeningAction;
import org.bitbucket.socialroboticshub.actions.audiovisual.StopWatchingAction;

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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

public class CBSRenvironment extends EIDefaultImpl {
	private static final long serialVersionUID = 1L;
	protected Map<String, Parameter> parameters;
	protected BlockingQueue<Percept> perceptQueue;
	protected List<Percept> previousPercepts;
	protected String redisServer;
	protected String redisUser;
	protected String redisPass;
	protected String flowKey;
	protected String flowAgent;
	protected String flowLang;
	protected RedisRunner consumer;
	protected RedisRunner producer;
	protected Profiler profiler;

	@Override
	public void init(final Map<String, Parameter> parameters) throws ManagementException {
		super.init(parameters);
		this.parameters = parameters;
		this.perceptQueue = new LinkedBlockingQueue<>();
		this.previousPercepts = new ArrayList<>(0);
		setState(EnvironmentState.PAUSED);

		// get all parameters
		this.redisServer = getParameter("server", "localhost");
		final String[] userInfo = isLocal(this.redisServer) ? new String[] { "default", "changemeplease" }
				: getUserInformation();
		this.redisUser = userInfo[0];
		this.redisPass = userInfo[1];
		this.flowKey = getParameter("flowkey", "");
		if (!this.flowKey.isEmpty()) {
			try {
				this.flowKey = new String(Files.readAllBytes(Paths.get(this.flowKey)), StandardCharsets.UTF_8);
			} catch (final Exception e) {
				throw new ManagementException("Unable to read the dialogflow keyfile", e);
			}
		}
		this.flowAgent = getParameter("flowagent", "");
		this.flowLang = getParameter("flowlang", "");
		this.profiler = new Profiler(getParameter("profiling", "").equals("1"));

		final Map<DeviceType, List<String>> devices = new HashMap<>(DeviceType.size());
		for (final DeviceType type : DeviceType.values()) {
			devices.put(type, new LinkedList<>());
		}
		final Parameter devicesParam = this.parameters.get("devices");
		if (devicesParam instanceof ParameterList) {
			boolean foundOne = false;
			for (final Parameter device : (ParameterList) devicesParam) {
				if (device instanceof Identifier) {
					final String[] split = ((Identifier) device).getValue().split(":");
					if (split.length == 2) {
						final String identifier = this.redisUser + "-" + split[0];
						final DeviceType type = DeviceType.fromString(split[1]);
						if (type != null) {
							devices.get(type).add(identifier);
							foundOne = true;
						}
					}
				}
			}
			if (!foundOne) {
				throw new ManagementException("No (valid) devices given in the 'devices' init parameter");
			}
		} else {
			getDevices(devices);
		}
		if (devices.isEmpty()) {
			throw new ManagementException("No devices selected");
		}
		for (final Entry<DeviceType, List<String>> allDevices : devices.entrySet()) {
			final String deviceType = allDevices.getKey().toString();
			for (final String device : allDevices.getValue()) {
				this.perceptQueue.add(new Percept("device", new Identifier(deviceType), new Identifier(device)));
			}
		}

		// start the database connections
		this.consumer = new RedisConsumerRunner(this, devices);
		this.consumer.start();
		this.producer = new RedisProducerRunner(this, devices);
		this.producer.start();

		// start-up actions
		addAction(new StopListeningAction());
		addAction(new StopWatchingAction());
		if (!this.flowLang.isEmpty()) {
			addAction(new SetLanguageAction(Arrays.asList(new Identifier(this.flowLang))));
		}

		// we're ready; announce the entity
		setState(EnvironmentState.RUNNING);
		try {
			addEntity("robot", "robot");
		} catch (final Exception e) {
			throw new ManagementException("Unable to initialise the robot entity", e);
		}
	}

	public Profiler getProfiler() {
		return this.profiler;
	}

	private String getParameter(final String name, final String def) {
		final Parameter param = this.parameters.get(name);
		return (param instanceof Identifier) ? ((Identifier) param).getValue().trim() : def;
	}

	private void getDevices(final Map<DeviceType, List<String>> devices) throws ManagementException {
		// show the device selection dialog
		final JDialog deviceSelection = new JDialog((JDialog) null, "Select Devices", true);
		final JPanel deviceGrid = new JPanel(new GridLayout(0, 2));
		final List<JCheckBox> checkboxes = new LinkedList<>();
		try (final Jedis jedis = connect()) {
			final Set<String> deviceIds = jedis.zrangeByScore("user:" + this.redisUser,
					(Instant.now().getEpochSecond() - 60), Double.POSITIVE_INFINITY);
			for (final String deviceId : new TreeSet<>(deviceIds)) {
				final JCheckBox deviceBox = new JCheckBox(deviceId);
				checkboxes.add(deviceBox);
				deviceGrid.add(deviceBox);
				deviceGrid.add(new JLabel(""));
			}
		} catch (final Exception e) {
			throw new ManagementException("Unable to get the list of devices", e);
		}
		if (checkboxes.isEmpty()) {
			throw new ManagementException("No devices found");
		}
		final JButton select = new JButton("(De)Select All");
		select.addActionListener(e -> {
			boolean noneSelected = true;
			for (final JCheckBox checkbox : checkboxes) {
				if (checkbox.isSelected()) {
					noneSelected = false;
					break;
				}
			}
			for (final JCheckBox checkbox : checkboxes) {
				checkbox.setSelected(noneSelected);
			}
		});
		deviceGrid.add(select);
		final JButton ok = new JButton("OK");
		ok.addActionListener(e -> {
			deviceSelection.dispose();
		});
		deviceGrid.add(ok);
		deviceSelection.add(deviceGrid);
		deviceSelection.getRootPane().setDefaultButton(ok);
		deviceSelection.pack();
		deviceSelection.setVisible(true);

		// process the device selection
		boolean foundOne = false;
		for (final JCheckBox checkbox : checkboxes) {
			if (checkbox.isSelected()) {
				final String[] split = checkbox.getText().split(":");
				final String identifier = this.redisUser + "-" + split[0];
				final DeviceType type = DeviceType.fromString(split[1]);
				devices.get(type).add(identifier);
				foundOne = true;
			}
		}
		if (!foundOne) {
			throw new ManagementException("No (valid) devices selected");
		}
	}

	@Override
	public void kill() throws ManagementException {
		this.consumer.shutdown();
		this.producer.shutdown();
		this.profiler.shutdown();
		this.perceptQueue.clear();
		super.kill();
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
		final RobotAction robotAction = RobotAction.getRobotAction(action);
		return (robotAction != null && robotAction.isValid());
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
		final RobotAction robotAction = RobotAction.getRobotAction(action);
		if (robotAction != null && robotAction.isValid()) {
			if (robotAction instanceof StartListeningAction) {
				final StartListeningAction slAction = (StartListeningAction) robotAction;
				if (!slAction.getContext().isEmpty()) {
					addAction(getStubAction("dialogflow_context", slAction.getContext(), null));
				}
			}
			addAction(robotAction);
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
		this.profiler.end(event);
		this.perceptQueue.add(new Percept("event", new Identifier(event)));
	}

	/**
	 * Queues the answer (to a posed question) as a percept to be received by the
	 * agent.
	 *
	 * @param answer The answer
	 */
	public void addAnswer(final String answer) {
		try {
			this.perceptQueue.add(new Percept("answer", new Numeral(Integer.parseInt(answer))));
		} catch (final NumberFormatException e) {
			this.perceptQueue.add(new Percept("answer", new Identifier(answer)));
		}
	}

	/**
	 * Queues 'personDetected' as a percept to be received by the agent. This means
	 * one or more persons have been detected (with a certainty threshold).
	 */
	public void addDetectedPerson() {
		this.perceptQueue.add(new Percept("personDetected"));
	}

	/**
	 * Queues the detected emotion as a percept to be received by the agent.
	 */
	public void addDetectedEmotion(final String emotion) {
		this.perceptQueue.add(new Percept("emotionDetected", new Identifier(emotion)));
	}

	/**
	 * Queues the id of the face as a percept to be received by the agent.
	 *
	 * @param id The identifier for the face
	 */
	public void addRecognizedFace(final String id) {
		this.perceptQueue.add(new Percept("faceRecognized", new Identifier(id)));
	}

	/**
	 * Queues the language key as a percept to be received by the agent, and changes
	 * the language used in the intent recognition.
	 *
	 * @param lang The language key
	 */
	public void setAudioLanguage(final String lang) {
		this.perceptQueue.add(new Percept("audioLanguage", new Identifier(lang)));
	}

	/**
	 * Queues the file name of an audio recording as a percept to be received by the
	 * agent.
	 *
	 * @param filename The filename (including extension) of the recording.
	 */
	public void addAudioRecording(final String filename) {
		this.perceptQueue.add(new Percept("audioRecording", new Identifier(filename)));
	}

	/**
	 * Queues the id of a loaded audio file as a percept to be received by the
	 * agent.
	 *
	 * @param audioID The id of the loaded audio.
	 */
	public void addLoadedAudioID(final int audioID) {
		this.perceptQueue.add(new Percept("loadedAudioID", new Numeral(audioID)));
	}

	/**
	 * Queues the file name of a picture as a percept to be received by the agent.
	 *
	 * @param filename The filename (including extension) of the picture.
	 */
	public void addPicture(final String filename) {
		this.perceptQueue.add(new Percept("picture", new Identifier(filename)));
	}

	/**
	 * Queues the data coming from the memory module as a key value pair percept to
	 * be received by the agent.
	 *
	 * @param key   key of memory data packet
	 * @param value value of memory data packet
	 */
	public void addMemoryData(final String key, final String value) {
		try {
			final Number valueNumber = NumberFormat.getInstance().parse(value);
			this.perceptQueue.add(new Percept("memoryData", new Identifier(key), new Numeral(valueNumber)));
		} catch (final ParseException e) {
			this.perceptQueue.add(new Percept("memoryData", new Identifier(key), new Identifier(value)));
		}
	}

	/**
	 * Queues the data coming from a gui controller as a key value pair percept to
	 * be received by the agent.
	 *
	 * @param key   key of gui data packet
	 * @param value value of gui data packet
	 */
	public void addGuiData(final String key, final String value) {
		this.perceptQueue.add(new Percept("guiData", new Identifier(key), new Identifier(value)));
	}

	/**
	 * Queues PostureChanged events as percept to be received by the agent.
	 *
	 * @param posture new posture
	 */
	public void addPostureChanged(final String posture) {
		this.perceptQueue.add(new Percept("posture", new Identifier(posture)));
	}

	/**
	 * Queues isAwake events as percept to be received by the agent.
	 *
	 * @param isAwake '1' if awake and '0' if asleep
	 */
	public void addIsAwake(final boolean isAwake) {
		this.perceptQueue.add(new Percept("isAwake", new TruthValue(isAwake)));
	}

	/**
	 * Queues onStiffnessChanged events as percept to be received by the agent.
	 *
	 * @param stiffness value to indicate the average stiffness 0 means that average
	 *                  of stiffness is less than 0.05 1 means that average of
	 *                  stiffness is between 0.05 and 0.95 2 means that average of
	 *                  stiffness is greater 0.95
	 */
	public void addStiffness(final int stiffness) {
		this.perceptQueue.add(new Percept("stiffness", new Numeral(stiffness)));
	}

	/**
	 * Queues BatteryChargeChanged events as pecepts to be received by the agent.
	 *
	 * @param percentage battery level (0-100)
	 */
	public void addBatteryCharge(final int percentage) {
		this.perceptQueue.add(new Percept("batteryCharge", new Numeral(percentage)));
	}

	/**
	 * Queues BatteryPowerPluggedChanged events as percepts to be received by the
	 * agent.
	 *
	 * @param isCharging '1' if charging and '0' if not charging
	 */
	public void addIsCharging(final boolean isCharging) {
		this.perceptQueue.add(new Percept("isCharging", new TruthValue(isCharging)));
	}

	/**
	 * Queues HotDeviceDetected events as percepts to be received by agent.
	 *
	 * @param hotDevices body parts of the robot that are too hot.
	 */
	public void addHotDevice(final String[] hotDevices) {
		final Parameter[] parameters = new Parameter[hotDevices.length];
		for (int i = 0; i < hotDevices.length; i++) {
			parameters[i] = new Identifier(hotDevices[i]);
		}
		this.perceptQueue.add(new Percept("hotDevices", new ParameterList(parameters)));
	}

	/**
	 * Queues a motion recording as a percept to be received by the agent.
	 *
	 * @param motionRecording
	 */
	public void addMotionRecording(final String motionRecording) {
		this.perceptQueue.add(new Percept("motionRecording", new Identifier(motionRecording)));
	}

	/**
	 * Queues a text transcript as a percept to be received by the agent.
	 *
	 * @param motionRecording
	 */
	public void addTextTranscript(final String transcript) {
		this.perceptQueue.add(new Percept("transcript", new Identifier(transcript)));
	}

	/**
	 * Queues forceSessionStart as a percept to be received by the agent.
	 */
	public void forceSessionStart() {
		this.perceptQueue.add(new Percept("forceSessionStart"));
	}

	/**
	 * Queues forceSessionEnd as a percept to be received by the agent.
	 */
	public void forceSessionEnd() {
		this.perceptQueue.add(new Percept("forceSessionEnd"));
	}

	/**
	 * Queues a coronaCheckPassed percept to be received by the agent.
	 */
	public void addCoronaCheckPassed() {
		this.perceptQueue.add(new Percept("coronaCheckPassed"));
	}

	/**
	 * Queues the given action for transmission by the producer.
	 *
	 * @param action
	 */
	public void addAction(final RobotAction action) {
		((RedisProducerRunner) this.producer).queueAction(action);
	}

	public Jedis connect() throws Exception {
		final Jedis jedis = isLocal(this.redisServer)
				? new Jedis(this.redisServer, Protocol.DEFAULT_PORT, true, getLocalSSL(), null, null)
				: new Jedis(this.redisServer, Protocol.DEFAULT_PORT, true);
		jedis.auth(this.redisUser, this.redisPass);
		return jedis;
	}

	private static boolean isLocal(final String server) {
		return server.startsWith("127.") || server.startsWith("192.") || server.equals("localhost");
	}

	private static SSLSocketFactory getLocalSSL() throws Exception {
		final KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(CBSRenvironment.class.getResourceAsStream("/truststore.jks"), "changeit".toCharArray());
		final Certificate original = ((KeyStore.TrustedCertificateEntry) keyStore.getEntry("cbsr", null))
				.getTrustedCertificate();
		final TrustManager bypass = new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[] { (X509Certificate) original };
			}

			@Override
			public void checkClientTrusted(final X509Certificate[] chain, final String authType)
					throws CertificateException {
				checkServerTrusted(chain, authType);
			}

			@Override
			public void checkServerTrusted(final X509Certificate[] chain, final String authType)
					throws CertificateException {
				if (chain.length != 1 || !chain[0].equals(original)) {
					throw new CertificateException("Invalid certificate provided");
				}
			}
		};
		final SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, new TrustManager[] { bypass }, null);
		return sslContext.getSocketFactory();
	}

	//
	// STATIC HELPER FUNCTIONS
	//

	protected static String[] getUserInformation() {
		final Preferences prefs = Preferences.userRoot().node("cbsr");
		final int fieldWidth = 16;

		final JDialog dialog = new JDialog((JDialog) null, "User Information", true);
		final JPanel grid = new JPanel(new GridLayout(0, 1));
		final JPanel userPanel = new JPanel(new BorderLayout());
		final JLabel userLabel = new JLabel("Username: ");
		final JTextField userField = new JTextField(fieldWidth);
		userField.setText(prefs.get("user", ""));
		userPanel.add(userLabel, BorderLayout.WEST);
		userPanel.add(userField, BorderLayout.EAST);
		grid.add(userPanel);
		final JPanel passwordPanel = new JPanel(new BorderLayout());
		final JLabel passwordLabel = new JLabel("Password: ");
		final JPasswordField passwordField = new JPasswordField(fieldWidth);
		passwordPanel.add(passwordLabel, BorderLayout.WEST);
		passwordPanel.add(passwordField, BorderLayout.EAST);
		grid.add(passwordPanel);

		final JButton ok = new JButton("OK");
		ok.addActionListener(e -> {
			dialog.dispose();
		});
		grid.add(ok);

		dialog.add(grid);
		dialog.getRootPane().setDefaultButton(ok);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(final WindowEvent e) {
				if (userField.getText().isEmpty()) {
					userField.requestFocusInWindow();
				} else {
					passwordField.requestFocusInWindow();
				}
			}
		});
		dialog.pack();
		dialog.setVisible(true);

		final String[] result = { userField.getText(), new String(passwordField.getPassword()) };
		prefs.put("user", result[0]);
		return result;
	}

	private static RobotAction getStubAction(final String topic, final String data, final String expectedEvent) {
		return new RobotAction(null) {
			@Override
			public boolean isValid() {
				return true;
			}

			@Override
			public String getTopic() {
				return topic;
			}

			@Override
			public String getData() {
				return data;
			}

			@Override
			public String getExpectedEvent() {
				return expectedEvent;
			}
		};
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
			if (!((param instanceof Identifier) && ((Identifier) param).getValue().isEmpty())) {
				elements.add(new Function("=", new Identifier(key), param));
			}
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
