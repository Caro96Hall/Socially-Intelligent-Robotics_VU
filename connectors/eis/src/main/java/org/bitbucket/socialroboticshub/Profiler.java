package org.bitbucket.socialroboticshub;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.bitbucket.socialroboticshub.actions.RobotAction;

public class Profiler {
	private volatile boolean enabled;
	private final String name;
	private final Map<String, Long> cache;
	private final BlockingQueue<String> queue;

	public Profiler(final boolean enabled) {
		this.enabled = enabled;
		this.name = enabled ? ("profile_" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date())) : null;
		this.cache = enabled ? new ConcurrentHashMap<>() : null;
		this.queue = enabled ? new LinkedBlockingQueue<>() : null;
		if (enabled) {
			new Thread(() -> {
				write();
			}).start();
		}
	}

	public void start(final String label) {
		if (this.enabled) {
			this.cache.put(label, System.nanoTime());
		}
	}

	public void startRobotAction(final RobotAction action) {
		final String label = action.getExpectedEvent();
		if (label != null) {
			start(label);
		}
	}

	public void end(final String label) {
		final Long start = this.enabled ? this.cache.remove(label) : null;
		if (start != null) {
			final double diff = (System.nanoTime() - start.longValue()) / 1000000.0;
			final String info = (label + ";" + String.format("%.1f", diff) + "\n");
			this.queue.add(info);
			if (label.endsWith("Started")) {
				start(label.replace("Started", "Done"));
			}
		}
	}

	public void write() {
		FileWriter writer = null;
		while (this.enabled) {
			try {
				final String next = this.queue.take();
				if (writer == null) {
					writer = new FileWriter(this.name + ".csv");
				}
				writer.write(next);
				writer.flush();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		if (writer != null) {
			try {
				writer.close();
			} catch (final Exception ignore) {
			}
		}
	}

	public void shutdown() {
		if (this.enabled) {
			this.cache.clear();
			this.queue.add("END;");
			this.enabled = false;
		}
	}
}
