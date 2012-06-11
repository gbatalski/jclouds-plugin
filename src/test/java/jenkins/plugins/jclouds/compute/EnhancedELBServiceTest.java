/**
 *
 */
package jenkins.plugins.jclouds.compute;


import jenkins.plugins.jclouds.compute.elb.EnhancedLoadBalancerService;

import org.jclouds.compute.domain.NodeMetadata;

import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 *
 * FIXME: This test doesn't work without a nodes because it needs either regions
 * or subnets which we can not provide via the interfaces and for now only via
 * the instances. These, on the other hand, should be real instances as they
 * will be submitted to aws and also check for correctness there. One should
 * really split the lb creation from the instance assignment
 *
 * @author gena
 *
 */
public class EnhancedELBServiceTest extends JCloudsCloudInsideJenkinsLiveTest {

    @Test
    public void testELBService() {
	EnhancedLoadBalancerService elbService = cloud.getLb();
	assertNotNull(elbService);

	try{
	    @SuppressWarnings("unused")
	LoadBalancerMetadata loadBalancer = elbService.createLoadBalancerInLocation(null, "test1", "HTTP", 80, 80,
		    ImmutableList.<NodeMetadata> of());
	// assertNotNull(loadBalancer);
	}catch(Exception e){

	}
	elbService.createAppCookieStickinessPolicy(null, "test1", "JSESSIONID", "JSESSIONID");
	elbService.setLoadBalancingPoliciesOfListener(null, "test1", 80,
		"JSESSIONID");
	elbService.destroyLoadBalancer("test1");
    }
}
