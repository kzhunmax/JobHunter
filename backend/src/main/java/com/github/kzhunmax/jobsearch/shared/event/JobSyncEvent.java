package com.github.kzhunmax.jobsearch.shared.event;

public record JobSyncEvent(
        Long jobId,
        SyncAction action
) {
}
