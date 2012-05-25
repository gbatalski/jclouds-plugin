package jenkins.plugins.jclouds.compute;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import jenkins.plugins.jclouds.compute.internal.NodePlan;
import jenkins.plugins.jclouds.compute.internal.ProvisionPlannedInstancesAndDestroyAllOnError;
import jenkins.plugins.jclouds.compute.internal.RunningNode;
import jenkins.plugins.jclouds.compute.internal.TerminateNodes;
import jenkins.plugins.jclouds.internal.BuildListenerLogger;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.util.concurrent.MoreExecutors;

public class JCloudsBuildWrapper extends BuildWrapper {
   private final List<InstancesToRun> instancesToRun;

    // #2 Expose running nodes for later
   private transient Iterable<RunningNode> runningNode;
   
   public Iterable<RunningNode> getRunningNodes() {
        return runningNode;
   }	

   @DataBoundConstructor
   public JCloudsBuildWrapper(List<InstancesToRun> instancesToRun) {
      this.instancesToRun = instancesToRun;
   }

    public List<InstancesToRun> getInstancesToRun() {
        return instancesToRun;
    }

   //   
   // convert Jenkins staticy stuff into pojos; performing as little critical stuff here as
   // possible, as this method is very hard to test due to static usage, etc.
   //   
   @Override
   public Environment setUp(AbstractBuild build, Launcher launcher, final BuildListener listener) {
      //TODO: on shutdown, close all
      final LoadingCache<String, ComputeService> computeCache = CacheBuilder.newBuilder().build(new CacheLoader<String, ComputeService>(){

         @Override
         public ComputeService load(String arg0) throws Exception {
            return JCloudsCloud.getByName(arg0).getCompute();
         }
          
      });
      
      // eagerly lookup node supplier so that errors occur before we attempt to provision things
      Iterable<NodePlan> nodePlans = Iterables.transform(instancesToRun, new Function<InstancesToRun, NodePlan>() {

         public NodePlan apply(InstancesToRun instance) {
            String cloudName = instance.cloudName;
            String templateName = instance.templateName;
            Supplier<NodeMetadata> nodeSupplier = JCloudsCloud.getByName(cloudName).getTemplate(templateName);
            // take the hit here, as opposed to later
            computeCache.getUnchecked(cloudName);
            return new NodePlan(cloudName, templateName, instance.count, instance.suspendOrTerminate, nodeSupplier);
         }

      });

      // converting to a logger as it is an interface and easier to test
      final Logger logger = new BuildListenerLogger(listener);
      
      final TerminateNodes terminateNodes = new TerminateNodes(logger, computeCache);

      ProvisionPlannedInstancesAndDestroyAllOnError provisioner = new ProvisionPlannedInstancesAndDestroyAllOnError(
               MoreExecutors.listeningDecorator(Computer.threadPoolForRemoting), logger, terminateNodes);

	// #2 Expose running nodes for later
      runningNode = provisioner.apply(nodePlans);

      return new Environment() {
         @Override
         public void buildEnvVars(Map<String, String> env) {
            List<String> ips = getInstanceIPs(runningNode, listener.getLogger());
            env.put("JCLOUDS_IPS", Util.join(ips, ","));
         }

         @Override
         public boolean tearDown(AbstractBuild build, final BuildListener listener) throws IOException,
                  InterruptedException {
            terminateNodes.apply(runningNode);
            return true;
         }

      };

   }

   public List<String> getInstanceIPs(Iterable<RunningNode> runningNodes, PrintStream logger) {
      Builder<String> ips = ImmutableList.<String> builder();

      for (RunningNode runningNode : runningNodes) {
         String[] possibleIPs = JCloudsLauncher.getConnectionAddresses(runningNode.getNode(), logger);
         if (possibleIPs[0] != null) {
            ips.add(possibleIPs[0]);
         }
      }

      return ips.build();
   }

   @Extension
   public static final class DescriptorImpl extends BuildWrapperDescriptor {
      @Override
      public String getDisplayName() {
         return "JClouds Instance Creation";
      }

      @Override
      public boolean isApplicable(AbstractProject item) {
         return true;
      }

   }
}
