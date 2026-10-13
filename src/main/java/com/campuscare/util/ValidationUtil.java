package com.campuscare.util;

import java.util.regex.Pattern;

/** Input validation helpers. Each check method throws AppException with a clear message. */
public final class ValidationUtil {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^(\\+91)?[6-9][0-9]{9}$");

    private ValidationUtil() { }

    public static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    public static String clean(String s) { return s == null ? "" : s.trim(); }

    public static void checkName(String name) {
        if (isBlank(name) || name.trim().length() < 3 || name.trim().length() > 60)
            throw new AppException("Name must be 3 to 60 characters.");
    }

    public static void checkEmail(String email) {
        if (isBlank(email) || !EMAIL.matcher(email.trim()).matches())
            throw new AppException("Enter a valid email address.");
    }

    public static void checkPhone(String phone) {
        if (isBlank(phone) || !PHONE.matcher(phone.trim()).matches())
            throw new AppException("Enter a valid 10-digit Indian mobile number.");
    }

    public static void checkPassword(String pw) {
        if (pw == null || pw.length() < 8 || !pw.matches(".*[A-Z].*")
                || !pw.matches(".*[a-z].*") || !pw.matches(".*[0-9].*"))
            throw new AppException("Password must be 8+ characters with upper-case, lower-case and a digit.");
    }

    public static void checkLength(String label, String value, int min, int max) {
        int len = value == null ? 0 : value.trim().length();
        if (len < min || len > max)
            throw new AppException(label + " must be " + min + " to " + max + " characters.");
    }
}
