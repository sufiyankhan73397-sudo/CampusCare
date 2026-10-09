package com.campuscare.model;

public record Notification(int id, Integer complaintId, String message, boolean read, String createdAt) { }
