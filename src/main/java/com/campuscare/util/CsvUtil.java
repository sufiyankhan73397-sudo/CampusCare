package com.campuscare.util;

import com.campuscare.model.Complaint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Simple CSV export for admin reports (opens in Excel). */
public final class CsvUtil {
    private CsvUtil() { }

    public static void exportComplaints(List<Complaint> list, Path file) {
        List<String> lines = new ArrayList<>();
        lines.add("ID,Title,Student,Category,Department,Location,Priority,Status,Assigned Staff,Created,Updated,Resolved");
        for (Complaint c : list) {
            lines.add(String.join(",",
                    String.valueOf(c.id()), q(c.title()), q(c.studentName()), q(c.categoryName()),
                    q(c.departmentName()), q(c.locationName()), c.priority(), c.status(),
                    q(c.assignedStaff()), q(c.createdAt()), q(c.updatedAt()), q(c.resolvedAt())));
        }
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AppException("Could not write report: " + e.getMessage(), e);
        }
    }

    private static String q(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
