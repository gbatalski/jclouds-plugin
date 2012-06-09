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
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.util.Set;

import jenkins.plugins.jclouds.compute.JCloudsBuildWrapper;
import jenkins.plugins.jclouds.compute.internal.RunningNode;

import nl.javadude.scannit.Configuration;
import nl.javadude.scannit.Scannit;
import nl.javadude.scannit.scanner.TypeAnnotationScanner;


import org.jclouds.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;


import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import com.google.common.util.concurrent.ListeningExecutorService;

import edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.JCloudsEnabledBuilder;
import edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution.CassandraChefExecutorStrategy;
import edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution.ChefExecutorStrategy;
import edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution.SimpleChefExecutorStrategy;
import edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution.Strategy;
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
    private final String executionStrategy;

    private transient Set<LabelAtom> labelSet;

    private final String chefJson;

    @DataBoundConstructor
    public ChefBuilder(String cookbookString, String chefJson, String portString, String labelString, String executionStrategy) {
	this.chefJson = chefJson;
	this.portString = portString;
	this.labelString = labelString;
	this.cookbookString = cookbookString;
	this.executionStrategy = executionStrategy;
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

    @SuppressWarnings("unchecked")
    @Override
    protected void build(JCloudsBuildWrapper jCloudsBuildWrapper, AbstractBuild<?, ?> build, Launcher launcher,
	    BuildListener listener,
	    ListeningExecutorService listeningDecorator,
	    Iterable<RunningNode> runningNodes, Logger logger)
	    throws IOException,
	    InterruptedException {
	for (Environment env : build.getEnvironments())
	    env.buildEnvVars(build.getEnvironment(listener));

	String strategyClass = Objects.firstNonNull(executionStrategy, SimpleChefExecutorStrategy.class.getCanonicalName());


	Class<ChefExecutorStrategy<?>> strategyClazz;
	try {
	    strategyClazz = (Class<ChefExecutorStrategy<?>>) Class.forName(strategyClass);

	    Iterable<RunningNode> filteredNodes = Iterables.filter(runningNodes, new Predicate<RunningNode>() {

		@Override
		public boolean apply(RunningNode input) {
		    if (!Sets.intersection(labelSet, input.getLabelSet()).isEmpty())
			return true;
		    return false;

		}
	    });

	ChefExecutorStrategy<?> executorStrategy = (ChefExecutorStrategy<?>) strategyClazz.getDeclaredConstructors()[0].newInstance(listeningDecorator, cookbooks,
		Util.replaceMacro(chefJson,
		build.getEnvironment(listener)), logger, labelSet,
		toArray(listeningPorts, Integer.class));
	    executorStrategy.apply(filteredNodes);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    @Extension
    // This indicates to Jenkins that this is an implementation of an extension
    // point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {



	public static Set<Class<?>> getExecutionStrategyClazzes() {
	    // FIXME: Classloader problems

	    // // ClassLoader cl =
	    // Thread.currentThread().getContextClassLoader();
	    // //
	    // Thread.currentThread().setContextClassLoader(Strategy.class.getClassLoader());
	    // Configuration config = Configuration.config().with(new
	    // TypeAnnotationScanner())
	    // .scan("edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef.execution");
	    // // Thread.currentThread().setContextClassLoader(cl);
	    // Scannit scannit = new Scannit(config);
	    // return scannit.getTypesAnnotatedWith(Strategy.class);
	    //
	    return ImmutableSet.<Class<?>> of(SimpleChefExecutorStrategy.class, CassandraChefExecutorStrategy.class);
	}

	public ListBoxModel doFillExecutionStrategyItems() {
	    ListBoxModel m = new ListBoxModel();

	    for (Class<?> executionStrategy : getExecutionStrategyClazzes()) {

		m.add(executionStrategy.getAnnotation(Strategy.class).value(), executionStrategy.getCanonicalName());
	    }

	    return m;
	}

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

    public String getExecutionStrategy() {
	return executionStrategy;
    }
}
