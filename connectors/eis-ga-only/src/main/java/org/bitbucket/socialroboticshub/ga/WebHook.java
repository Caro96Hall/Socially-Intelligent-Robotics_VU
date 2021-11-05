package org.bitbucket.socialroboticshub.ga;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.cloud.dialogflow.v2.QueryResult;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

final class WebHook extends NanoHTTPD {
	private static final String MIME_JSON = "application/json";
	private static final int serverPort = 8004;
	private final GoogleAssistant parent;
	private final Process tunnel;
	private final JsonUtils json;

	WebHook(final GoogleAssistant parent, final String subdomain) throws Exception {
		super(serverPort);
		this.parent = parent;
		this.tunnel = startTunnel(subdomain);
		this.json = new JsonUtils();
		setTempFileManagerFactory();
		start(0);
	}

	private static Process startTunnel(final String subdomain) throws Exception {
		final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("win");
		final String localtunnel = isWindows ? "lt.cmd" : "lt";
		final String[] command = { localtunnel, "--host", "https://loca.lt", //
				"--port", Integer.toString(serverPort), "--subdomain", subdomain };
		System.out.println(Arrays.asList(command));
		final ProcessBuilder b = new ProcessBuilder(command).inheritIO();
		return b.start();
	}

	@Override
	public void stop() {
		super.stop();
		this.tunnel.destroy();
	}

	@Override
	public Response serve(final IHTTPSession session) {
		System.out.println("Received " + session.getMethod() + " to '" + session.getUri() + "' ");
		try {
			final Map<String, String> map = new LinkedHashMap<>();
			session.parseBody(map);
			if (map.containsKey("postData")) {
				final QueryResult queryResult = this.json.getQueryResult(map.get("postData"));
				final String text = this.parent.getResponse(queryResult);
				final String jsonResponse = this.json.convertResponse(text);
				return newFixedLengthResponse(Status.OK, MIME_JSON, jsonResponse);
			} else {
				return newFixedLengthResponse(Status.BAD_REQUEST, MIME_PLAINTEXT, "No POST data found");
			}
		} catch (final Exception e) {
			e.printStackTrace();
			return newFixedLengthResponse(Status.INTERNAL_ERROR, MIME_PLAINTEXT, e.getMessage());
		}
	}

	private void setTempFileManagerFactory() {
		setTempFileManagerFactory(() -> new TempFileManager() { // set deleteOnExit
			private final File tmpdir = new File(System.getProperty("java.io.tmpdir"));
			private final List<TempFile> tempFiles = new LinkedList<>();

			@Override
			public void clear() {
				for (final TempFile file : this.tempFiles) {
					try {
						file.delete();
					} catch (final Exception ignore) {
					}
				}
				this.tempFiles.clear();
			}

			@Override
			public TempFile createTempFile(final String filename_hint) throws Exception {
				final DefaultTempFile tempFile = new DefaultTempFile(this.tmpdir);
				new File(tempFile.getName()).deleteOnExit();
				this.tempFiles.add(tempFile);
				return tempFile;
			}
		});
	}
}
