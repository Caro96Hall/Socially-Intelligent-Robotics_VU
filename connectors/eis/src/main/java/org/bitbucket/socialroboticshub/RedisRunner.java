package org.bitbucket.socialroboticshub;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;

abstract class RedisRunner extends Thread {
	protected static final Charset UTF8 = StandardCharsets.UTF_8;
	protected final CBSRenvironment parent;
	protected final Map<DeviceType, List<String>> devices;
	private Jedis redis;

	RedisRunner(final CBSRenvironment parent, final Map<DeviceType, List<String>> devices) {
		this.parent = parent;
		this.devices = devices;
	}

	@Override
	public abstract void run();

	protected Jedis getRedis() {
		if (this.redis == null) {
			try {
				this.redis = this.parent.connect();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return this.redis;
	}

	protected boolean isRunning() {
		return (this.redis != null);
	}

	public void shutdown() {
		if (this.redis != null) {
			this.redis.close();
			this.redis = null;
		}
	}
}
