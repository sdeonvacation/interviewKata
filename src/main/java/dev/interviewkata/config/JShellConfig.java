package dev.interviewkata.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "interviewkata.sandbox")
public class JShellConfig {

    private long timeoutMs = 5000;
    private int maxHeapMb = 256;
    private int warmPoolSize = 2;
    private int maxOutputBytes = 10240;
    private List<String> allowedModules = List.of("java.base", "jdk.jshell");

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getMaxHeapMb() {
        return maxHeapMb;
    }

    public void setMaxHeapMb(int maxHeapMb) {
        this.maxHeapMb = maxHeapMb;
    }

    public int getWarmPoolSize() {
        return warmPoolSize;
    }

    public void setWarmPoolSize(int warmPoolSize) {
        this.warmPoolSize = warmPoolSize;
    }

    public int getMaxOutputBytes() {
        return maxOutputBytes;
    }

    public void setMaxOutputBytes(int maxOutputBytes) {
        this.maxOutputBytes = maxOutputBytes;
    }

    public List<String> getAllowedModules() {
        return allowedModules;
    }

    public void setAllowedModules(List<String> allowedModules) {
        this.allowedModules = allowedModules;
    }
}
