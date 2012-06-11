/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder;

import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.net.HostAndPort.fromString;

import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import jenkins.plugins.jclouds.compute.JCloudsCloud;

import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.jclouds.logging.Logger;
import org.jclouds.predicates.InetSocketAddressConnect;
import org.jclouds.predicates.RetryablePredicate;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * @author gena
 *
 */
public class CreateLoadbalancerAndDestroyOnError implements Function<Iterable<NodeMetadata>, LoadBalancerMetadata> {

    public CreateLoadbalancerAndDestroyOnError(LoadBalancer loadBalancer, ListeningExecutorService listeningDecorator, Logger logger) {
	super();
	this.loadBalancer = loadBalancer;
	this.listeningDecorator = listeningDecorator;
	this.logger = logger;
    }

    private final ListeningExecutorService listeningDecorator;
    private final LoadBalancer loadBalancer;
    private final Logger logger;

    /*
     *
     *
     * (non-Javadoc)
     *
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    @Override
    public LoadBalancerMetadata apply(final Iterable<NodeMetadata> input) throws NoSuchElementException {
	ListenableFuture<LoadBalancerMetadata> provisionLb = listeningDecorator.submit(new Callable<LoadBalancerMetadata>() {

	    @Override
	    public LoadBalancerMetadata call() throws Exception {

		LoadBalancerMetadata lbMetadata = JCloudsCloud
			.getByName(loadBalancer.cloudName)
			.getLb()
			.createLoadBalancerInLocation(null, loadBalancer.loadBalancerName, loadBalancer.protocol,
				loadBalancer.loadBalancerPort, loadBalancer.instancePort, input);
		if (loadBalancer.sticky == true) {
		    // create sticky cookie policy
		    JCloudsCloud
			    .getByName(loadBalancer.cloudName)
			    .getLb()
			    .createAppCookieStickinessPolicy(null, lbMetadata.getId(), loadBalancer.cookieName,
				    loadBalancer.cookieName);
		    // enable sticky cookie policy
		    JCloudsCloud
			    .getByName(loadBalancer.cloudName)
			    .getLb()
			    .setLoadBalancingPoliciesOfListener(null, lbMetadata.getId(), loadBalancer.loadBalancerPort,
				    loadBalancer.cookieName);
		}
		return lbMetadata;
	    }
	});

	final ImmutableList.Builder<LoadBalancerMetadata> loadBalancerBuilder = ImmutableList.<LoadBalancerMetadata> builder();

	Futures.addCallback(provisionLb, new FutureCallback<LoadBalancerMetadata>() {
	    public void onSuccess(LoadBalancerMetadata result) {
	    }

	    public void onFailure(Throwable t) {
		logger.warn(t, "Error while launching loadbalancer %s with port %d", loadBalancer.loadBalancerName,
			loadBalancer.loadBalancerPort, loadBalancer.instancePort);
	    }
	});
	// do the work
	LoadBalancerMetadata result = Futures.getUnchecked(provisionLb);
	// wait up to 15 minutes start polling from each 10 seconds and up to
	// default 100 seconds per try
	boolean listening = new RetryablePredicate<HostAndPort>(new InetSocketAddressConnect(), 900, 10, TimeUnit.SECONDS)
		.apply(fromString(getFirst(result.getAddresses(), null)).withDefaultPort(loadBalancer.loadBalancerPort));
	if (listening) {
	    loadBalancerBuilder.add(result);
	} else {
	    // doesn't listen on external port, so destroy and clean
	    JCloudsCloud.getByName(loadBalancer.cloudName).getLb().destroyLoadBalancer(result.getId());
	}


	return getOnlyElement(loadBalancerBuilder.build());
    }
}
