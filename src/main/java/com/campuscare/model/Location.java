package com.campuscare.model;

public record Location(int id, String name, String building, String floor, String description) {
    @Override public String toString() { return name; }
}
