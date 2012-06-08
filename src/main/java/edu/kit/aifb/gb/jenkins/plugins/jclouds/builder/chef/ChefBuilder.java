/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef;

import static com.google.common.collect.Iterables.toArray;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.Environment;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.labels.LabelAtom;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

import java.io.IOException;
import java.util.Set;

import jenkins.plugins.jclouds.compute.JCloudsBuildWrapper;
import jenkins.plugins.jclouds.compute.internal.RunningNode;

import org.jclouds.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListeningExecutorService;

import edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.JCloudsEnabledBuilder;
import edu.kit.aifb.gb.utils.FilterIntegers;

/**
 *
 * Executes one or more chef cookbooks
 *
 * @author gena
 *
 */
public class ChefBuilder extends JCloudsEnabledBuilder<ChefBuilder> {

    private transient Iterable<String> cookbooks;
    private transient Iterable<Integer> listeningPorts;
    private final String portString;
    private final String labelString;
    private final String cookbookString;

    private transient Set<LabelAtom> labelSet;

    private final String chefJson;

    @DataBoundConstructor
    public ChefBuilder(String cookbookString, String chefJson, String portString, String labelString) {
	this.chefJson = chefJson;
	this.portString = portString;
	this.labelString = labelString;
	this.cookbookString = cookbookString;

	readResolve();
    }

    protected Object readResolve() {
	labelSet = Label.parse(labelString);
	listeningPorts = FilterIntegers.filterIntegers(portString);
	cookbooks = cookbookString == null ? ImmutableSet.<String> of() : Splitter.onPattern("\\s|,|;").split(cookbookString);
	return this;
    }

    public String getCookbookString() {
	return cookbookString;
    }

    public String getPortString() {
	return portString;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException,
	    IOException {

	return super.perform(build, launcher, listener);
    }

    public String getChefJson() {
	return chefJson;
    }

    @Override
    protected void build(JCloudsBuildWrapper jCloudsBuildWrapper, AbstractBuild<?, ?> build, Launcher launcher,
	    BuildListener listener,
	    ListeningExecutorService listeningDecorator,
 Iterable<RunningNode> runningNodes, Logger logger) throws IOException,
	    InterruptedException {
	for (Environment env : build.getEnvironments())
	    env.buildEnvVars(build.getEnvironment(listener));


	Function<RunningNode, Boolean> chefExecFunction = new ChefRecipeExecutorFunction(cookbooks, Util.replaceMacro(chefJson,
		build.getEnvironment(listener)), logger, labelSet,
		toArray(listeningPorts, Integer.class));
	SimpleParallelExecutor<Boolean> executor = new SimpleParallelExecutor<Boolean>(listeningDecorator, logger, chefExecFunction);
	executor.apply(runningNodes);
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
	    return "Simple Chef-Solo builder";
	}

    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public Descriptor<Builder> getDescriptor() {

	return (DescriptorImpl) super.getDescriptor();
    }

    public String getLabelString() {
	return labelString;
    }


    public Set<LabelAtom> getLabelSet() {
	return labelSet;
    }
}
