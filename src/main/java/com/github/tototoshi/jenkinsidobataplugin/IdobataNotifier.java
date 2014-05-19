package com.github.tototoshi.jenkinsidobataplugin;

import com.m3.curly.HTTP;
import com.m3.curly.Request;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
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
     * You can post a message with source parameter like below:
     *
     * $ curl --data-urlencode "source=hello, world" https://idobata.io/hook/bc8d4f4e-80f6-4018-a6a5-af56b3e9a251
     * You can also use HTML format:
     *
     * $ curl --data-urlencode "source=>h1>hi</h1>" -d format=html https://idobata.io/hook/bc8d4f4e-80f6-4018-a6a5-af56b3e9a251
     * Additionally, you can post image with image parameter:
     *
     * $ curl --form image=@/path/to/image.png https://idobata.io/hook/bc8d4f4e-80f6-4018-a6a5-af56b3e9a251
     *
     * @param format text|html|image
     * @return Request
     * @throws UnsupportedEncodingException
     */
    private Request createRequest(AbstractBuild<?, ?> build, String format) throws UnsupportedEncodingException {
        String message = "source=" + new SimpleTemplate(selectMessage(build)).render(build);
        if (format.equals("html")) {
            message = "format=html&" + message;
        }
        byte[] formData = message.getBytes();
        return new Request(url).setBody(formData, "application/x-www-form-urlencoded");
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
