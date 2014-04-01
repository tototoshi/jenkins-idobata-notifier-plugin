package com.github.tototoshi.jenkinsidobataplugin;

import hudson.model.AbstractBuild;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import jenkins.model.JenkinsLocationConfiguration;

public class SimpleTemplate {

    private String template;

    public SimpleTemplate(String template) {
        this.template = template;
    }

    public String render(AbstractBuild<?, ?> build) {
        StringBuilder userBuilder = new StringBuilder();
        for (User user : build.getCulprits()) {
            userBuilder.append(user.getDisplayName() + " ");
        }
        StringBuilder changeSetBuilder = new StringBuilder();
        for(ChangeLogSet.Entry entry : build.getChangeSet()) {
            changeSetBuilder.append(entry.getAuthor() + " : " + entry.getMsg() + "\n");
        }
        String replacedMessage = template.replace("${user}", userBuilder.toString());
        replacedMessage = replacedMessage.replace("${result}", build.getResult().toString());
        replacedMessage = replacedMessage.replace("${project}", build.getProject().getName());
        replacedMessage = replacedMessage.replace("${number}", String.valueOf(build.number));
        replacedMessage = replacedMessage.replace("${url}", JenkinsLocationConfiguration.get().getUrl() + build.getUrl());
        replacedMessage = replacedMessage.replace("${changeSet}", changeSetBuilder.toString());
        return replacedMessage;
    }

}
