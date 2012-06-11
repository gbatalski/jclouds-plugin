/**
 *
 */
package jenkins.plugins.jclouds.compute.elb;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.jclouds.aws.filters.FormSigner;
import org.jclouds.elb.ELBAsyncClient;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.location.functions.RegionToEndpointOrProviderIfNull;
import org.jclouds.rest.annotations.BinderParam;
import org.jclouds.rest.annotations.EndpointParam;
import org.jclouds.rest.annotations.FormParams;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.VirtualHost;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;

import static org.jclouds.aws.reference.FormParameters.*;

/**
 * @author gena
 *
 */
@Beta
@RequestFilters(FormSigner.class)
@VirtualHost
public interface EnhancedELBAsyncClient extends ELBAsyncClient {
    /**
     *TODO: Implement error handling like
     * <p>
     *
     * @ExceptionParser(ReturnEmptySetOnNotFoundOr404.class) </p>
     *
     * <table cellspacing="0" border="0"><colgroup><col><col><col></colgroup><thead><tr><th> Error </th><th> Description </th><th> HTTP Status Code </th></tr></thead><tbody><tr><td>
                     <code class="code">DuplicatePolicyName</code>
                  </td><td>
                     <p>
        Policy with the same name exists for this LoadBalancer.
        Please choose another name.
        </p>
                  </td><td>400</td></tr><tr><td>
                     <code class="code">InvalidConfigurationRequest</code>
                  </td><td>
                     <p>
        Requested configuration change is invalid.
        </p>
                  </td><td>409</td></tr><tr><td>
                     <code class="code">LoadBalancerNotFound</code>
                  </td><td>
                     <p>
            The specified LoadBalancer could not be found.
        </p>
                  </td><td>400</td></tr><tr><td>
                     <code class="code">TooManyPolicies</code>
                  </td><td>
                     <p>
        Quota for number of policies for this LoadBalancer
        has already been reached.
        </p>
                  </td><td>400</td></tr></tbody></table>
     *
     * @see EnhancedELBClient#createAppCookieStickinessPolicy
     */
    @POST
    @Path("/")
    @FormParams(keys = ACTION, values = "CreateAppCookieStickinessPolicy")
    ListenableFuture<Void> createAppCookieStickinessPolicy(
	    @EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
	    @FormParam("LoadBalancerName") String name, @FormParam("CookieName") String cookieName,
	    @FormParam("PolicyName") String policyName);

    @POST
    @Path("/")
    @FormParams(keys = ACTION, values = "SetLoadBalancerPoliciesOfListener")
    ListenableFuture<Void> setLoadBalancingPoliciesOfListener(
	    @EndpointParam(parser = RegionToEndpointOrProviderIfNull.class) @Nullable String region,
	    @FormParam("LoadBalancerName") String name, @FormParam("LoadBalancerPort") Integer loadBalancerPort,
	    @BinderParam(BindPolicyNamesToIndexedFormParams.class) String... policyNames);

}
