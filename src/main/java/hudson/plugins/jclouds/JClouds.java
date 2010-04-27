package hudson.plugins.jclouds;

import hudson.Extension;
import hudson.model.Label;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner.PlannedNode;
import hudson.util.Secret;

import hudson.model.Descriptor;
import hudson.util.FormValidation;
import java.io.IOException;

import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.domain.Architecture;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.Size;
import org.jclouds.compute.util.ComputeUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;


/**
 *
 * @author mordred
 */
public class JClouds extends Cloud {

    private final String user;
    private final Secret secret;

    @DataBoundConstructor
    public JClouds(String user, String secret) {
        super("jclouds-" + user);
        this.user = user;
        this.secret = Secret.fromString(secret.trim());
    }
    private static final Logger LOGGER = Logger.getLogger(JClouds.class.getName());

    public String getSecret() {
        return secret.getEncryptedValue();
    }

    public String getUser() {
        return user;
    }

    @Override
    public boolean canProvision(Label label) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<PlannedNode> provision(Label label, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Cloud> {

        public String getDisplayName() {
            return "JClouds";
        }

        public Set<String> getSupportedProviders() {

            return ComputeUtils.getSupportedProviders();
        }

        public FormValidation doTestConnection(
                @QueryParameter String provider,
                @QueryParameter String user,
                @QueryParameter String secret) throws ServletException, IOException {

            try {
                
                ComputeServiceContext context = new ComputeServiceContextFactory().createContext(provider, user, secret);

                ComputeService client = context.getComputeService();

                //Set<? extends ComputeMetadata> nodes = Sets.newHashSet(connection.getNodes().values());

                for (Image image: client.getImages().values()) {
                    LOGGER.info(image.getArchitecture().toString());
                    LOGGER.info(image.getOsFamily().toString());
                }
                for (Size size: client.getSizes().values())
                {
                    LOGGER.log(Level.INFO, "size: {0}", size.toString());
                    LOGGER.log(Level.INFO, "\tcores: {0}", size.getCores());
                    LOGGER.log(Level.INFO, "\tdisk: {0}", size.getDisk());
                    LOGGER.log(Level.INFO, "\tram: {0}", size.getRam());
                    LOGGER.log(Level.INFO, "\tarchitectures");
                    for (Architecture arch : size.getSupportedArchitectures())
                    {
                       LOGGER.log(Level.INFO, "\t\t{0}", arch.toString());
                    }

                }
                for (ComputeMetadata node : client.getNodes().values()) {
                    LOGGER.log(Level.INFO, "Node {0}:{1} in {2}", new Object[]{
                                node.getId(),
                                node.getName(),
                                node.getLocation().getId()});
            
                }
                return FormValidation.ok();
            } catch (org.jclouds.rest.AuthorizationException ex) {
                return FormValidation.error("Authentication Error: " + ex.getLocalizedMessage());
            }
        }
    }
}