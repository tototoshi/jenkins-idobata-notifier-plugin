package com.github.tototoshi.jenkinsidobataplugin;

import com.m3.curly.HTTP;
import com.m3.curly.Request;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import org.kohsuke.stapler.DataBoundConstructor;
import java.io.IOException;
import java.net.URLConnection;
import java.net.URLEncoder;

public class IdobataNotifier extends Notifier {

    private String url;

    private String notificationStrategy;

    public String getUrl() {
        return url;
    }

    public String getNotificationStrategy() {
        return notificationStrategy;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @DataBoundConstructor
    public IdobataNotifier(String url, String notificationStrategy) {
        this.url = url;
        this.notificationStrategy = notificationStrategy;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        byte[] formData = ("source=" + URLEncoder.encode("<h1>Test</h1>", "utf-8")).getBytes();
        Request request = new Request(url + "?format=html").setBody(formData, "application/x-www-form-urlencoded");
        HTTP.post(request);
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Idobata Notifier";
        }

    }
}
