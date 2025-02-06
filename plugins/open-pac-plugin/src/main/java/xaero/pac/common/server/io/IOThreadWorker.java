package xaero.pac.common.server.io;

import xaero.pac.common.server.io.exception.IOThreadWorkerException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class IOThreadWorker implements Runnable {

	private final ConcurrentLinkedQueue<Runnable> taskQueue;
	private boolean running;
	private Thread thread;
	private Throwable crashThrowable;

	public IOThreadWorker() {
		super();
		this.taskQueue = new ConcurrentLinkedQueue<Runnable>();
		this.running = true;
	}

	public void begin() {
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		synchronized(this) {
			while(running || !taskQueue.isEmpty()) {
				Runnable task;
				while((task = taskQueue.poll()) != null) {
					try {
						task.run();
					} catch(Throwable t) {
						if(crashThrowable == null)
							crashThrowable = t;
					}
				}
				if(running) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	public void stop() {
		running = false;
		synchronized(this) {
			//wait for the worker to finish
		}
		if(!taskQueue.isEmpty())
			throw new IllegalStateException();
	}

	private void enqueueInternal(Runnable task) {
		if(!running)
			throw new IllegalStateException();
		taskQueue.add(task);
		thread.interrupt();
	}

	public void enqueue(Runnable task) {
		enqueueInternal(() -> {
			if(crashThrowable != null)//ignoring regular tasks after a crash
				return;
			task.run();
		});
	}

	public <T> CompletableFuture<T> getFuture(Supplier<T> task) {//will crash the game on completion exception
		CompletableFuture<T> future = new CompletableFuture<>();
		enqueue(() -> future.complete(task.get()));
		return future;
	}

	public <T> T get(Supplier<T> task) {//does not crash the game unless the method caller doesn't handle an exception
		CompletableFuture<T> future = new CompletableFuture<>();
		enqueueInternal(() -> {
			try {
				if(crashThrowable != null)//complete exceptionally for supplier tasks after a crash
					throw new IOThreadWorkerException();
				future.complete(task.get());
			} catch(Throwable t) {
				future.completeExceptionally(t);
			}
		});
		return future.join();
	}

	public void checkCrashes() throws Throwable {
		if(crashThrowable != null) {
			Throwable toThrow = crashThrowable;
			crashThrowable = null;
			if(toThrow instanceof RuntimeException)
				throw toThrow;
			throw new RuntimeException(toThrow);
		}
	}

}
