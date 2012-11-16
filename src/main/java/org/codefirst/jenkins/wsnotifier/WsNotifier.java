package org.codefirst.jenkins.wsnotifier;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.*;
import hudson.tasks.*;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

public class WsNotifier extends Notifier {
    @DataBoundConstructor
    public WsNotifier() {
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        WsServer.send(build);
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService(){
        return BuildStepMonitor.NONE;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private int port = 8081;
        private int pingInterval = 20;

        public int port(){ return port; }
        public boolean keepalive(){ return pingInterval >= 0; }
        public int pingInterval(){ if (keepalive()) return pingInterval; else return 20; }

        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        public String getDisplayName() {
            return "Websocket Notifier";
        }

        public FormValidation doCheckPort(@QueryParameter String value)
            throws IOException, ServletException {
            try{
                Integer.parseInt(value);
                return FormValidation.ok();
            }catch(NumberFormatException ex){
                return FormValidation.error("invalid port number");
            }
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        	System.out.println(formData);
            port = formData.getInt("port");
            if (formData.has("keepalive")) {
            	JSONObject keepalive = formData.getJSONObject("keepalive");
                pingInterval = keepalive.getInt("pingInterval");
            } else {
            	pingInterval = -1;
            }
            save();
            WsServer.reset(port, pingInterval);
            return super.configure(req,formData);
        }
    }
}
