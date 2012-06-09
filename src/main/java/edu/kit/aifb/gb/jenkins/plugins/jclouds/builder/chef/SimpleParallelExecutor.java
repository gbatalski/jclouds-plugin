/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import jenkins.plugins.jclouds.compute.internal.RunningNode;

import org.jclouds.logging.Logger;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * @author gena
 *
 */
public class SimpleParallelExecutor<T> implements Function<Iterable<RunningNode>, Iterable<T>> {
    private final Supplier<Map<RunningNode, Function<RunningNode, T>>> delegateSupplier;
    private final ListeningExecutorService executor;
    private final Logger logger;
    private final long maxAttempts;

    public SimpleParallelExecutor(ListeningExecutorService executor, Logger logger,
	    Supplier<Map<RunningNode, Function<RunningNode, T>>> delegateSupplier) {
	this(executor, logger, 1l, delegateSupplier);
    }

    public SimpleParallelExecutor(ListeningExecutorService executor, Logger logger, long maxAttempts,
	    Supplier<Map<RunningNode, Function<RunningNode, T>>> delegateSupplier) {
	this.delegateSupplier = delegateSupplier;
	this.executor = executor;
	this.logger = logger;
	this.maxAttempts = maxAttempts;
    }

    @Override
    public Iterable<T> apply(Iterable<RunningNode> runningNodes) {

	final ImmutableList.Builder<T> resultBuilder = ImmutableList.<T> builder();
	final ImmutableList.Builder<ListenableFuture<T>> nodeLinstenerBuilder = ImmutableList.<ListenableFuture<T>> builder();

	final AtomicInteger failedLaunches = new AtomicInteger();
	final Map<RunningNode, Function<RunningNode, T>> delegates = delegateSupplier.get();

	for (final RunningNode runningNode : runningNodes) {

	    final ListenableFuture<T> delegateProvisioner = executor.submit(new RetrySupplierOnException(new Supplier<T>() {

		@Override
		public T get() {
		    return delegates.get(runningNode).apply(runningNode);
		}

	    }, maxAttempts));

	    Futures.addCallback(delegateProvisioner, new FutureCallback<T>() {
		public void onSuccess(T result) {
		    if (result != null) {
			resultBuilder.add(result);
		    } else {
			failedLaunches.incrementAndGet();
		    }
		}

		public void onFailure(Throwable t) {
		    failedLaunches.incrementAndGet();

		}
	    });

	    nodeLinstenerBuilder.add(delegateProvisioner);

	}

	// block until all complete
	List<T> nodesActuallyProcessed = Futures.getUnchecked(Futures.successfulAsList(nodeLinstenerBuilder.build()));

	final ImmutableList<T> resultNodes = resultBuilder.build();

	assert resultNodes.size() == nodesActuallyProcessed.size() : String.format(
		"expected nodes from callbacks to be the same count as those from the list of futures!%n"
			+ "fromCallbacks:%s%nfromFutures%s%n", resultNodes, nodesActuallyProcessed);

	if (failedLaunches.get() > 0) {

	    throw new IllegalStateException("One or more nodes couldn't be processed.");
	}
	return resultNodes;
    }

    private class RetrySupplierOnException implements Callable<T> {

	private final long maxAttempt;
	private final Supplier<T> supplier;

	RetrySupplierOnException(Supplier<T> supplier, long maxAttempt) {
	    this.supplier = supplier;
	    this.maxAttempt = maxAttempt;

	}

	public T call() throws Exception {
	    int attempts = 0;

	    while (attempts < maxAttempt) {
		attempts++;
		try {
		    T n = supplier.get();
		    if (n != null) {
			return n;
		    }
		} catch (RuntimeException e) {
		    logger.info(e.getMessage());
		}
	    }

	    return null;
	}
    }

}
