/**
 *
 */
package jenkins.plugins.jclouds.compute.elb;


import org.jclouds.Context;

import org.jclouds.loadbalancer.internal.LoadBalancerServiceContextImpl;
import org.jclouds.location.Provider;
import org.jclouds.rest.Utils;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;



/**
 * @author gena
 *
 */
@Singleton
public class EnhancedLoadBalancerServiceContextImpl extends LoadBalancerServiceContextImpl implements
	EnhancedLoadBalancerServiceContext {

    private final EnhancedLoadBalancerService loadBalancerService;

    /**
     * @param backend
     * @param backendType
     * @param loadBalancerService
     * @param utils
     */
    @Inject
    public EnhancedLoadBalancerServiceContextImpl(@Provider Context backend, @Provider TypeToken<? extends Context> backendType,
	    EnhancedLoadBalancerService loadBalancerService, Utils utils) {
	super(backend, backendType, loadBalancerService, utils);
	this.loadBalancerService = loadBalancerService;
    }


    @Override
    public EnhancedLoadBalancerService getLoadBalancerService() {

	return loadBalancerService;
    }

}
