package com.campuscare.model;

public record Complaint(int id, int userId, String studentName, int categoryId, String categoryName,
                        int departmentId, String departmentName, int locationId, String locationName,
                        String title, String description, String priority, String status,
                        String createdAt, String updatedAt, String resolvedAt, String assignedStaff) { }
