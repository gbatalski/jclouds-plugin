/**
 *
 */
package edu.kit.aifb.gb.jenkins.plugins.jclouds.builder.chef;

import static com.google.common.collect.Collections2.*;
import static com.google.common.collect.Iterables.*;
import static java.lang.String.*;
import static org.jclouds.scriptbuilder.domain.Statements.*;

import java.util.concurrent.TimeUnit;

import jenkins.plugins.jclouds.compute.JCloudsCloud;
import jenkins.plugins.jclouds.compute.JCloudsSlaveTemplate;
import jenkins.plugins.jclouds.compute.internal.RunningNode;

import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.logging.Logger;
import org.jclouds.predicates.RetryablePredicate;
import org.jclouds.scriptbuilder.domain.Statement;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author gena
 *
 */
public class ChefRecipeExecutorFunction implements Function<RunningNode, Boolean> {

    // default successChecker
    private final static Predicate<ExecResponse> successChecker = new ExecutionSucceeded();

    private final Iterable<Integer> portsToCheck;

    public ChefRecipeExecutorFunction(Iterable<String> cookbooks, String chefJson, Logger logger, Integer... portsToCheck) {
	super();
	this.cookbooks = cookbooks;
	this.chefJson = chefJson;
	this.logger = logger;
	this.portsToCheck = ImmutableList.copyOf(portsToCheck);

    }

    private final Iterable<String> cookbooks;

    private final String chefJson;

    private final Logger logger;

    @Override
    public Boolean apply(RunningNode runningNode) {
	boolean successful = false;
	try {
	    final NodeMetadata nodeMetadata = runningNode.getNode();
	    final JCloudsSlaveTemplate slaveTemplate = JCloudsCloud.getByName(runningNode.getCloudName()).getTemplate(
		    nodeMetadata.getName());

	    final RunList runList = RunList.fromJSON(chefJson);

	    ExecResponse response = JCloudsCloud
		    .getByName(runningNode.getCloudName())
		    .getCompute()
		    .runScriptOnNode(
			    nodeMetadata.getId(),
			    newStatementList(
				    // exec("env LC_ALL=C && export DEBIAN_PRIORITY=critical && export DEBIAN_FRONTEND=noninteractive"),
				    exec(format(
					    "export HOME_DIR=\"`getent passwd %1$s | cut -d: -f6`\" && export SOLO_DIR=$HOME_DIR/chef-solo",
					    slaveTemplate.getJenkinsUser())),
				    exec("alias rm=\"rm -f\""),
				    exec("export PATH=\"/usr/local/bin/:/usr/local/sbin/:$PATH\""),
				    exec("cd $SOLO_DIR"),
				    runList.yieldKnifeStatement("$SOLO_DIR", cookbooks),
				    exec("cat > solo.json <<EOF\n"
					    + chefJson.replaceAll("(?:(?:github:(?:[^/]*/)))?([^/]+)(/[^/]+)?", "$1$2") + "\nEOF"),
				    exec("chef-solo -c $SOLO_DIR/solo.rb -j  $SOLO_DIR/solo.json")), RunScriptOptions.NONE);


	    logger.info(response.getOutput());

	    successful = successChecker.apply(response);
	    if (!successful)
		throw new RuntimeException(response.getError());
	    if (!isEmpty(portsToCheck))
		for (Integer port : portsToCheck) {
		    boolean listeningOnPort = new RetryablePredicate<Integer>(new ListeningOnPort(nodeMetadata,
			    runningNode.getCloudName()), 120, 5, TimeUnit.SECONDS).apply(port);
		    if (!listeningOnPort)
			throw new RuntimeException(format("Not listening on port %d after default back-off", port));
		}
	} finally {

	}
	return successful;
    }

    public static class RunList {
	public RunList(Iterable<String> runList) {

	    Iterable<String> transformResult = transform(runList, new Function<String, String>() {

		@Override
		public String apply(String input) {

		    return "recipe[" + input + "]";
		}
	    });
	    this.runList = toArray(transformResult, String.class);

	}

	private final String[] runList;

	public String toJSON() {
	    Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
	    return gson.toJson(this);
	}

	public static RunList fromJSON(String json) {
	    Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
	    return gson.fromJson(json, RunList.class);
	}

	public Iterable<String> getRecipes() {
	    return transform(ImmutableSet.copyOf(runList), new Function<String, String>() {
		@Override
		public String apply(String input) {
		    return input.replaceFirst("recipe\\[(.*)\\]", "$1");
		}
	    });
	}

	public Statement yieldKnifeStatement(final String pathToConfig, final Iterable<String> cookbooks) {

	    return new Function<Iterable<String>, Statement>() {

		@Override
		public Statement apply(Iterable<String> input) {
		    Iterable<String> distinctCookbooks = ImmutableSet.copyOf(transform(input, new Function<String, String>() {
			@Override
			public String apply(String input) {
			    return input.replaceFirst("^([^:]+).*", "$1");
			}
		    }));

		    return newStatementList(toArray(
			    transform(concat(cookbooks, distinctCookbooks), new Function<String, Statement>() {
			@Override
			public Statement apply(String input) {
			    String statement = "knife cookbook %1$s install %2$s -c %3$s/solo.rb -VV -o %3$s/%4$s";
			    String cookbookPath = "orig-cookbooks";
			    String gem = "site";
			    if (input.contains("github:")) {
				cookbookPath = "cookbooks";
				gem = "github";
			    }
			    return exec(format(statement, gem, input.replaceFirst("^(?:github:)?(.+)$", "$1"), pathToConfig,
				    cookbookPath));
			};
		    }), Statement.class));
		}
	    }.apply(getRecipes());

	}
    }
}
