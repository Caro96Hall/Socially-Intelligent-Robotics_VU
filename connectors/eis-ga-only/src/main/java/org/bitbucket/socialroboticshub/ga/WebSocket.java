package org.bitbucket.socialroboticshub.ga;

import java.awt.Frame;
import java.net.URI;

import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.cloud.dialogflow.v2.QueryResult;

public class WebSocket extends WebSocketClient {
	public static final String VUserver = "socialai4.labs.vu.nl";
	private final GoogleAssistant parent;
	private final JDialog statusDialog;
	private final JTextField statusField;
	private final JsonUtils json;
	private boolean started = false;

	public WebSocket(final GoogleAssistant parent, final String projectId) throws Exception {
		super(new URI("ws://" + VUserver));
		this.parent = parent;
		this.json = new JsonUtils();
		this.statusDialog = new JDialog((Frame) null, "Connection Status");
		this.statusDialog.setAlwaysOnTop(true);
		this.statusDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.statusField = new JTextField(20);
		this.statusField.setEditable(false);
		this.statusField.setText("Connecting to " + VUserver + " ...");
		this.statusDialog.add(this.statusField);
		this.statusDialog.pack();
		this.statusDialog.setVisible(true);
		connectBlocking();
		start(projectId);
	}

	public void start(final String projectId) {
		if (this.started) {
			stop();
		}
		System.out.println("Starting with " + projectId + "...");
		send(">>>" + projectId + "|" + System.getenv("user.name"));
		this.started = true;
	}

	public void stop() {
		if (this.started) {
			System.out.println("Stopping current run...");
			this.statusDialog.dispose();
			if (isOpen()) {
				send("<<<");
			}
			this.started = false;
		}
	}

	@Override
	public void onOpen(final ServerHandshake handshakedata) {
	}

	@Override
	public void onMessage(final String message) {
		if (message.endsWith("!")) {
			System.out.println(message);
			this.statusField.setText(message);
		} else {
			try {
				this.statusDialog.dispose();
				final QueryResult queryResult = this.json.getQueryResult(message);
				final String text = this.parent.getResponse(queryResult);
				final String jsonResponse = this.json.convertResponse(text);
				send(jsonResponse);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onClose(final int code, final String reason, final boolean remote) {
		this.parent.disconnect();
		if (!reason.isEmpty()) {
			System.out.println("Closed websocket because: " + reason);
		}
	}

	@Override
	public void onError(final Exception ex) {
		ex.printStackTrace();
	}
}
