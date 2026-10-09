package com.campuscare.model;

public record FeedbackRow(int complaintId, String title, String studentName, String departmentName,
                          int rating, String comments, String createdAt) { }
