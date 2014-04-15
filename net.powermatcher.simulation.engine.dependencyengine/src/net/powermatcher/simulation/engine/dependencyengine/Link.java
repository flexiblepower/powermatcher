package net.powermatcher.simulation.engine.dependencyengine;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

// TODO may be more methods are called over this link
// now one method call is expected ...
public class Link implements InvocationHandler, ExecutedListener {
	private static class Call {
		Object[] args;
		Method method;

		public Call(Method method, Object[] args) {
			this.method = method;
			this.args = args;
		}
	}

	public interface ReadyListener {
		void notifyIsReady(Link link);
	}

	public static Link create(Object to, Class<?> type) {
		// TODO create specific links for AgentService and MatcherService
		// interface for speed
		Link link = new Link(to);
		link.setProxy(Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, link));
		return link;
	}

	private final AtomicReference<Call> call = new AtomicReference<Call>();

	private final List<Activity> dependencies = new CopyOnWriteArrayList<Activity>();
	private long dependencyCount = 0;

	private final AtomicLong finishedCount = new AtomicLong();

	private Object proxy;

	private ReadyListener readyListener;

	private Object to;

	public Link() {
	}

	public Link(Object to) {
		this.to = to;
	}

	/** Makes this link dependent on the given activity */
	public void setDependentOn(Activity activity) {
		dependencies.add(activity);
		dependencyCount++;
		activity.addExecutedListener(this);
	}

	public Object getProxy() {
		return proxy;
	}

	@Override
	// TODO analyze making an explicit list of methods to delay
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (this.readyListener == null || Void.TYPE.equals(method.getReturnType()) == false) {
			return method.invoke(to, args);
		}

		this.call.set(new Call(method, args));

		return null;
	}

	@Override
	public void notifyIsExecuted(Activity activity) {
		if (readyListener != null && finishedCount.incrementAndGet() == dependencyCount) {
			readyListener.notifyIsReady(this);
		}
	}

	public void release() {
		finishedCount.set(0);

		Call localCall;
		while (true) {
			localCall = call.get();
			if (call.compareAndSet(localCall, null)) {
				break;
			}
		}

		if (localCall == null) {
			return;
		}

		try {
			localCall.method.invoke(to, localCall.args);
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
	}

	private void setProxy(Object proxy) {
		this.proxy = proxy;
	}

	public void setReadyListener(ReadyListener listener) {
		if (this.readyListener != null) {
			throw new IllegalStateException("listener already associated");
		}

		this.readyListener = listener;
	}

	@Override
	public String toString() {
		if (to == null) {
			return "unnattached link";
		} else {
			return "link to " + to.getClass().getSimpleName();
		}
	}
}
