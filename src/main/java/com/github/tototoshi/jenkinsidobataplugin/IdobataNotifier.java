package com.github.tototoshi.jenkinsidobataplugin;

import com.m3.curly.HTTP;
import com.m3.curly.Request;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.scm.ChangeLogSet;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import jenkins.model.JenkinsLocationConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class IdobataNotifier extends Notifier {

    private String url;

    private String notificationStrategy;

    private String format;

    private String successMessage;

    private String failureMessage;

    public String getSuccessMessage() {
        return successMessage;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public String getUrl() {
        return url;
    }

    public String getNotificationStrategy() {
        return notificationStrategy;
    }

    public String getFormat() {
        return format;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @DataBoundConstructor
    public IdobataNotifier(String url, String notificationStrategy, String format, String successMessage, String failureMessage) {
        this.url = url;
        this.notificationStrategy = notificationStrategy;
        this.format = format;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        if (!needNotification(build)) return true;

        if (format == null) {
            format = "text";
        }

        Request request = createRequest(build, format);
        HTTP.post(request);
        return true;
    }

    /**
     * Create request for idobata
     *
     * @param format text|html|image
     * @return Request
     * @throws UnsupportedEncodingException
     */
    private Request createRequest(AbstractBuild<?, ?> build, String format) throws UnsupportedEncodingException {
        String message = selectMessage(build);

        StringBuilder userBuilder = new StringBuilder();
        for (User user : build.getCulprits()) {
            userBuilder.append(user.getDisplayName() + " ");
        }
        StringBuilder changeSetBuilder = new StringBuilder();
        for(ChangeLogSet.Entry entry : build.getChangeSet()) {
            changeSetBuilder.append(entry.getAuthor() + " : " + entry.getMsg() + "\n");
        }

        String replacedMessage = message.replace("${user}", userBuilder.toString());
        replacedMessage = replacedMessage.replace("${result}", build.getResult().toString());
        replacedMessage = replacedMessage.replace("${project}", build.getProject().getName());
        replacedMessage = replacedMessage.replace("${number}", String.valueOf(build.number));
        replacedMessage = replacedMessage.replace("${url}", JenkinsLocationConfiguration.get().getUrl() + build.getUrl());
        replacedMessage = replacedMessage.replace("${changeSet}", changeSetBuilder.toString());

        byte[] formData = ("source=" + URLEncoder.encode(replacedMessage, "utf-8")).getBytes();
        if (format.equals("html")) {
            return new Request(url + "?format=html").setBody(formData, "application/x-www-form-urlencoded");
        } else {
            return new Request(url).setBody(formData, "application/x-www-form-urlencoded");
        }
    }

    private boolean needNotification(AbstractBuild<?, ?> build) {
        NotificationStrategy strategy = NotificationStrategy.fromString(notificationStrategy);
        return strategy.needNotification(build);
    }


    private String selectMessage(AbstractBuild<?, ?> build) {
        if (build.getResult() == Result.SUCCESS) {
            return successMessage;
        } else {
            return failureMessage;
        }
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
