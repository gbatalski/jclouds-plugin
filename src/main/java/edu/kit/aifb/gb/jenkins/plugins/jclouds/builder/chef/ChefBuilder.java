/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

import java.io.IOException;

import jenkins.plugins.jclouds.compute.internal.RunningNode;

import org.jclouds.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 *
 * Executes one or more chef recipes
 *
 * @author gena
 *
 */
public class ChefBuilder extends JCloudsEnabledBuilder<ChefBuilder> {

    private final Iterable<String> recipes;
    private final Iterable<Integer> ports;

    private final String chefJson;

    @DataBoundConstructor
    public ChefBuilder(String recipesAsString, String chefJson, String portsAsString) {
	this.recipes = Splitter.onPattern("\\s|,|;").split(recipesAsString);
	this.chefJson = chefJson;
	this.ports = filterIntegers(Splitter.onPattern("\\s|,|;").split(portsAsString));

    }

    public String getRecipesAsString() {
	return isEmpty(recipes) ? "" : Joiner.on(" ").join(recipes.iterator());
    }

    public String getPortsAsString() {
	return isEmpty(recipes) ? "" : Joiner.on(" ").join(ports.iterator());
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

    /**
     * Filters out non-integer values from iterable of @param strings
     *
     * This method skips the not convertable values and nulls
     *
     * @return Iterable of Integer representations of @param strings
     *
     */
    public static Iterable<Integer> filterIntegers(Iterable<String> strings) {
	return filter(transform(strings, new Function<String, Integer>() {
	    @Override
	    public Integer apply(String input) {
		return nullToEmpty(input).trim().matches("\\d+") ? Integer.parseInt(input.trim()) : null;
	    }

	}), Predicates.notNull());
    }
}
