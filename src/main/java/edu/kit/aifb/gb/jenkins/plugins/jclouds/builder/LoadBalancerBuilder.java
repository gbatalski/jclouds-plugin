/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.labels.LabelAtom;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;

import java.io.IOException;
import java.util.Set;

import jenkins.plugins.jclouds.compute.JCloudsBuildWrapper;
import jenkins.plugins.jclouds.compute.internal.RunningNode;

import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;
import org.jclouds.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;


import static com.google.common.collect.Iterables.*;
import com.google.common.util.concurrent.ListeningExecutorService;
import hudson.tasks.Builder;

/**
 * @author gena
 *
 */
public class LoadBalancerBuilder extends JCloudsEnabledBuilder<LoadBalancerBuilder> {

    private transient Set<LabelAtom> labelSet;

    @DataBoundConstructor
    public LoadBalancerBuilder(String loadBalancerName, String labelString, int loadBalancerPort, int instancePort,
	    String protocol, boolean sticky, String cookieName) {
	super();
	this.loadBalancerName = loadBalancerName;
	this.labelString = labelString;
	this.loadBalancerPort = loadBalancerPort;
	this.instancePort = instancePort;
	this.protocol = protocol;
	this.sticky = sticky;
	this.cookieName = Objects.firstNonNull(Strings.emptyToNull(cookieName), "JSESSIONID");
	readResolve();
    }

    private final String loadBalancerName;
    private final String labelString;
    private final int loadBalancerPort;
    private final int instancePort;
    private final String protocol;
    private final boolean sticky;
    private final String cookieName;

    private LoadBalancerMetadata loadbalancer;

    protected Object readResolve() {
	labelSet = Label.parse(labelString);
	return this;
    }

    public String getLoadBalancerName() {
	return loadBalancerName;
    }

    public String getLabelString() {
	return labelString;
    }

    public int getLoadBalancerPort() {
	return loadBalancerPort;
    }

    public int getInstancePort() {
	return instancePort;
    }

    public String getProtocol() {
	return protocol;
    }

    public Set<LabelAtom> getLabelSet() {
	return labelSet;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.JCloudsEnabledBuilder
     * #build(hudson.model.AbstractBuild, hudson.Launcher,
     * hudson.model.BuildListener,
     * com.google.common.util.concurrent.ListeningExecutorService,
     * java.lang.Iterable, org.jclouds.logging.Logger)
     */
    @Override
    protected void build(JCloudsBuildWrapper jCloudsBuildWrapper, AbstractBuild<?, ?> build, Launcher launcher,
	    BuildListener listener,
	    ListeningExecutorService listeningDecorator, Iterable<RunningNode> runningNodes, Logger logger) throws IOException,
	    InterruptedException {

	Iterable<RunningNode> filteredNodes = filter(runningNodes, new Predicate<RunningNode>() {

	    @Override
	    public boolean apply(RunningNode input) {
		return any(input.getLabelSet(), Predicates.in(labelSet));
	    }

	});

	final String cloudName = ImmutableList.copyOf(filteredNodes).get(0).getCloudName();

	Iterable<NodeMetadata> labeledNodes = transform(filteredNodes, new Function<RunningNode, NodeMetadata>() {
	    @Override
	    public NodeMetadata apply(RunningNode input) {
		return input.getNode();
	    }
	});

	loadbalancer = new CreateLoadbalancerAndDestroyOnError(new LoadBalancer(cloudName, loadBalancerName, protocol,
		loadBalancerPort, instancePort, sticky, cookieName), listeningDecorator, logger).apply(labeledNodes);
	build.getEnvironment(listener).put(loadBalancerName + "_IPS", Joiner.on(", ").join(loadbalancer.getAddresses()));
	jCloudsBuildWrapper.addLoadbalancer(loadbalancer);

    }

    @Extension
    // This indicates to Jenkins that this is an implementation of an extension
    // point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

	@Override
	public boolean isApplicable(Class<? extends AbstractProject> aClass) {
	    // Indicates that this builder can be used with all kinds of project
	    // types
	    return true;
	}

	/**
	 * This human readable name is used in the configuration screen.
	 */
	@Override
	public String getDisplayName() {
	    return "Loadbalancer builder";
	}

    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public Descriptor<Builder> getDescriptor() {

	return (DescriptorImpl) super.getDescriptor();
    }

    public boolean isSticky() {
	return sticky;
    }

    public String getCookieName() {
	return cookieName;
    }

}
