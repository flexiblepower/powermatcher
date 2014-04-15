package net.powermatcher.simulator.prototype.dependencyengine;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Activity implements Link.ReadyListener {
	private List<Link> dependencies = new CopyOnWriteArrayList<Link>();

	private List<ExecutedListener> executedListeners = new CopyOnWriteArrayList<ExecutedListener>();

	private ReadyListener readyListener;
	private AtomicLong readyCount = new AtomicLong();

	private Object object;
	private Runnable runnable;

	private long next = Long.MAX_VALUE;
	private long period;

	private String name;

	public Activity(String name, Object object) {
		this.name = name;
		this.object = object;
	}

	public void addDependency(Link link) {
		dependencies.add(link);
		link.setReadyListener(this);
	}

	public void addExecutedListener(ExecutedListener listener) {
		executedListeners.add(listener);
	}

	public void setReadyListener(ReadyListener listener) {
		if (this.readyListener != null) {
			throw new IllegalStateException();
		}

		this.readyListener = listener;
	}

	@Override
	public void notifyIsReady(Link link) {
		if (readyListener != null && readyCount.incrementAndGet() == dependencies.size()) {
			readyListener.notifyIsReady(this);
		}
	}

	public void scheduleAtFixedRate(Date date, long period, TimeUnit unit) {
		if (object instanceof Runnable == false) {
			throw new IllegalStateException("Can only schedule instances of " + Runnable.class.getName());
		}

		this.runnable = (Runnable) object;
		this.next = date.getTime();
		this.period = unit.toMillis(period);
	}

	boolean isWithoutDependencies() {
		return dependencies.size() == 0;
	}

	void execute(long timestamp) {
		for (Link link : dependencies) {
			link.release();
		}

		if (next <= timestamp) {
			runnable.run();
			next += period;
		}

		readyCount.set(0);

		for (ExecutedListener executedListener : executedListeners) {
			executedListener.notifyIsExecuted(this);
		}
	}

	@Override
	public String toString() {
		return name;
	}

	public interface ReadyListener {
		void notifyIsReady(Activity activity);
	}
}
