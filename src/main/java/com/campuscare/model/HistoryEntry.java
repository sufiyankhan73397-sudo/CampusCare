package com.campuscare.model;

public record HistoryEntry(String updatedByName, String oldStatus, String newStatus,
                           String remarks, String updatedAt) { }
