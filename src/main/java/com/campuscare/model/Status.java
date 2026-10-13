package com.campuscare.model;

import java.util.List;

/** Complaint lifecycle statuses, priorities and roles as constants. */
public final class Status {
    public static final String SUBMITTED = "SUBMITTED";
    public static final String ASSIGNED = "ASSIGNED";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String RESOLVED = "RESOLVED";
    public static final String CLOSED = "CLOSED";
    public static final String REOPENED = "REOPENED";

    public static final List<String> ALL =
            List.of(SUBMITTED, ASSIGNED, ACCEPTED, IN_PROGRESS, RESOLVED, CLOSED, REOPENED);
    public static final List<String> PRIORITIES = List.of("NORMAL", "MEDIUM", "HIGH", "CRITICAL");

    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_STAFF = "STAFF";
    public static final String ROLE_ADMIN = "ADMIN";

    private Status() { }

    public static String pretty(String s) { return s == null ? "" : s.replace('_', ' '); }
}
