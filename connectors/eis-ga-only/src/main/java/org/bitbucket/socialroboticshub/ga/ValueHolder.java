package org.bitbucket.socialroboticshub.ga;

public class ValueHolder {
	private volatile String theValue;

	public synchronized void clear() {
		this.theValue = null;
	}

	public synchronized void set(final String value) {
		this.theValue = value;
		notifyAll();
	}

	public synchronized String get() throws Exception {
		while (this.theValue == null) {
			wait();
		}
		final String returned = this.theValue;
		this.theValue = null;
		return returned;
	}
}
