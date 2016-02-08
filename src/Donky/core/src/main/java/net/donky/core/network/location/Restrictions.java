package net.donky.core.network.location;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Restrictions implements Serializable {

    @SerializedName("maximumExecutions")
    private int maximumExecutions;

    @SerializedName("maximumExecutionsInterval")
    private String maximumExecutionsInterval;

    @SerializedName("maximumExecutionsIntervalSeconds")
    private int maximumExecutionsIntervalSeconds;

    @SerializedName("maximumExecutionsPerInterval")
    private int maximumExecutionsPerInterval;

    private int executorCount;

    private int executorCountPerInterval;

    public Restrictions(int maximumExecutions, String maximumExecutionsInterval, int maximumExecutionsIntervalSeconds, int maximumExecutionsPerInterval, int executorCount, int executorCountPerInterval) {
        this.maximumExecutions = maximumExecutions;
        this.maximumExecutionsInterval = maximumExecutionsInterval;
        this.maximumExecutionsIntervalSeconds = maximumExecutionsIntervalSeconds;
        this.maximumExecutionsPerInterval = maximumExecutionsPerInterval;
        this.executorCount = executorCount;
        this.executorCountPerInterval = executorCountPerInterval;
    }

    public int getExecutorCountPerInterval() {
        return executorCountPerInterval;
    }

    public void setExecutorCountPerInterval(int executorCountPerInterval) {
        this.executorCountPerInterval = executorCountPerInterval;
    }

    public int getExecutorCount() {
        return executorCount;
    }

    public void setExecutorCount(int executorCount) {
        this.executorCount = executorCount;
    }

    public int getMaximumExecutions() {
        return maximumExecutions;
    }

    public void setMaximumExecutions(int maximumExecutions) {
        this.maximumExecutions = maximumExecutions;
    }

    public String getMaximumExecutionsInterval() {
        return maximumExecutionsInterval;
    }

    public void setMaximumExecutionsInterval(String maximumExecutionsInterval) {
        this.maximumExecutionsInterval = maximumExecutionsInterval;
    }

    public int getMaximumExecutionsIntervalSeconds() {
        return maximumExecutionsIntervalSeconds;
    }

    public void setMaximumExecutionsIntervalSeconds(int maximumExecutionsIntervalSeconds) {
        this.maximumExecutionsIntervalSeconds = maximumExecutionsIntervalSeconds;
    }

    public int getMaximumExecutionsPerInterval() {
        return maximumExecutionsPerInterval;
    }

    public void setMaximumExecutionsPerInterval(int maximumExecutionsPerInterval) {
        this.maximumExecutionsPerInterval = maximumExecutionsPerInterval;
    }

    @Override
    public String toString() {
        return "Restrictions{" +
                "maximumExecutions=" + maximumExecutions +
                ", maximumExecutionsInterval='" + maximumExecutionsInterval + '\'' +
                ", maximumExecutionsIntervalSeconds=" + maximumExecutionsIntervalSeconds +
                ", maximumExecutionsPerInterval=" + maximumExecutionsPerInterval +
                ", executorCount=" + executorCount +
                '}';
    }
}
