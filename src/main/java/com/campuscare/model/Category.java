package com.campuscare.model;

public record Category(int id, String name, int departmentId, String departmentName) {
    @Override public String toString() { return name; }
}
