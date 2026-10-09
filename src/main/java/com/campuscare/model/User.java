package com.campuscare.model;

public record User(int id, String name, String email, String phone, String role,
                   Integer departmentId, String departmentName, String status, String createdAt) { }
