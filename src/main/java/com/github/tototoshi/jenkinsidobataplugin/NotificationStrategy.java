package com.github.tototoshi.jenkinsidobataplugin;

import hudson.model.AbstractBuild;
import hudson.model.Result;

public enum NotificationStrategy {

    ALL("all") {
        @Override
        public boolean needNotification(AbstractBuild<?, ?> build) {
            return true;
        }
    },
    FAILURE("failure") {
        @Override
        public boolean needNotification(AbstractBuild<?, ?> build) {
            return build.getResult() == Result.FAILURE;
        }
    },
    FAILURE_AND_FIXED("failure and fixed") {
        @Override
        public boolean needNotification(AbstractBuild<?, ?> build) {
            Result result = build.getResult();
            Result previousResult = build.getPreviousBuild().getResult();
            return result == Result.FAILURE || (result == Result.SUCCESS && previousResult != Result.SUCCESS);
        }
    },
    NEW_FAILURE_AND_FIXED("new failure and fixed") {
        @Override
        public boolean needNotification(AbstractBuild<?, ?> build) {
            Result result = build.getResult();
            Result previousResult = build.getPreviousBuild().getResult();
            return (result == Result.FAILURE && previousResult != Result.FAILURE)
                    || (result == Result.SUCCESS && previousResult != Result.SUCCESS);
        }
    },
    CHANGE("change") {
        @Override
        public boolean needNotification(AbstractBuild<?, ?> build) {
            Result result = build.getResult();
            Result previousResult = build.getPreviousBuild().getResult();
            return result != previousResult;
        }
    };

    private String value;

    NotificationStrategy(String value) {
        this.value = value;
    }

    public static NotificationStrategy fromString(String text) {
        if (text != null) {
            for (NotificationStrategy b : NotificationStrategy.values()) {
                if (text.equalsIgnoreCase(b.value)) {
                    return b;
                }
            }
        }
        return null;
    }

    public boolean needNotification(AbstractBuild<?, ?> build) {
        return true;
    }

}
