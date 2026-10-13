package com.campuscare.model;

public record Department(int id, String name, String description, String status) {
    @Override public String toString() { return name; }
}
