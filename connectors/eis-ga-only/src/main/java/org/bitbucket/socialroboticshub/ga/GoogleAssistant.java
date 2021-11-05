package org.bitbucket.socialroboticshub.ga;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bitbucket.socialroboticshub.CBSRenvironment;
import org.bitbucket.socialroboticshub.DetectionResultProto.DetectionResult;

import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.protobuf.Value;

import eis.iilang.EnvironmentState;

public final class GoogleAssistant {
	private static final long timeout = 5000L; // 5s
	private final CBSRenvironment parent;
	private final WebHook webHook;
	private final WebSocket webSocket;
	private final ValueHolder response;

	public GoogleAssistant(final CBSRenvironment parent, final String subdomain, final String flowagent)
			throws Exception {
		this.parent = parent;
		this.webHook = subdomain.isEmpty() ? null : new WebHook(this, subdomain);
		this.webSocket = flowagent.isEmpty() ? null : new WebSocket(this, flowagent);
		if (this.webHook == null && this.webSocket == null) {
			throw new Exception("Missing either the loca.lt subdomain or the Dialogflow ProjectID");
		}
		this.response = new ValueHolder();
	}

	public void disconnect() {
		if (this.webHook != null) {
			this.webHook.stop();
		}
		if (this.webSocket != null) {
			this.webSocket.stop();
			this.webSocket.close();
		}
		if (this.parent.getState() != EnvironmentState.KILLED) {
			try {
				this.parent.kill();
			} catch (final Exception ignore) {
			}
		}
	}

	public void setResponse(final String channel, final String message) {
		this.response.set(channel + ":" + message);
	}

	public String getResponse(final QueryResult queryResult) throws Exception {
		this.response.clear();
		if (processRecognisedIntent(queryResult)) {
			final Timer timer = new Timer(true);
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					System.out.println("Timed out whilst waiting for response...");
					GoogleAssistant.this.response.set("");
				}
			}, timeout); // cancel waiting when Google gives up
			final String response = this.response.get(); // blocking call
			timer.cancel();
			this.parent.addEvent("ShownOnAssistant");
			return response;
		} else {
			return "";
		}
	}

	private boolean processRecognisedIntent(final QueryResult intent) {
		final DetectionResult.Builder result = DetectionResult.newBuilder();
		result.setSource("webhook");

		final int confidence = (int) (intent.getIntentDetectionConfidence() * 100);
		if (confidence > 0) { // an intent was detected
			final String name = intent.getIntent().getDisplayName();
			System.out.format("DETECTED INTENT: '%s' (confidence %d%%)\n", name, confidence);
			result.setIntent(intent.getAction());
			final Map<String, Value> parameters = intent.getParameters().getFieldsMap();
			result.putAllParameters(parameters);
			if (!parameters.isEmpty()) {
				System.out.println(parameters);
			}
			result.setConfidence(confidence);
		}

		final String queryText = intent.getQueryText();
		if (!queryText.isEmpty()) { // the final recognised text (if any)
			System.out.format("RECOGNISED TEXT: '%s'\n", queryText);
			result.setText(queryText);
		}

		if (confidence > 0 || !queryText.isEmpty()) {
			this.parent.addIntent(result.build());
			return true;
		} else {
			return false;
		}
	}
}
