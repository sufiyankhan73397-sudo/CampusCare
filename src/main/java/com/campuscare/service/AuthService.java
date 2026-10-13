package com.campuscare.service;

import com.campuscare.dao.UserDao;
import com.campuscare.model.Status;
import com.campuscare.model.User;
import com.campuscare.util.AppException;
import com.campuscare.util.PasswordUtil;
import com.campuscare.util.SessionManager;
import com.campuscare.util.ValidationUtil;

/** Module M1: registration, login, profile, password. */
public class AuthService {
    private final UserDao userDao = new UserDao();

    public User login(String email, String password) {
        if (ValidationUtil.isBlank(email) || password == null || password.isEmpty())
            throw new AppException("Enter your email and password.");
        String mail = email.trim();
        User user = userDao.findByEmail(mail).orElseThrow(() -> new AppException("Invalid email or password."));
        String hash = userDao.findPasswordHashByEmail(mail).orElse(null);
        if (!PasswordUtil.verify(password, hash)) throw new AppException("Invalid email or password.");
        if (!"ACTIVE".equals(user.status()))
            throw new AppException("Your account is deactivated. Please contact the administrator.");
        SessionManager.login(user);
        return user;
    }

    /** Public self-registration: always creates a STUDENT. */
    public void register(String name, String email, String phone, String password, String confirm) {
        if (password == null || !password.equals(confirm)) throw new AppException("Passwords do not match.");
        createUser(name, email, phone, password, Status.ROLE_STUDENT, null);
    }

    /** Used by registration and by the admin "Add User" screen. */
    public int createUser(String name, String email, String phone, String password, String role, Integer departmentId) {
        ValidationUtil.checkName(name);
        ValidationUtil.checkEmail(email);
        ValidationUtil.checkPhone(phone);
        ValidationUtil.checkPassword(password);
        if (Status.ROLE_STAFF.equals(role) && departmentId == null)
            throw new AppException("Select a department for the staff member.");
        if (userDao.emailExists(email.trim())) throw new AppException("This email is already registered.");
        Integer dept = Status.ROLE_STAFF.equals(role) ? departmentId : null;
        return userDao.insert(name.trim(), email.trim().toLowerCase(), PasswordUtil.hash(password),
                phone.trim(), role, dept);
    }

    public User updateProfile(String name, String phone) {
        User me = SessionManager.requireUser();
        ValidationUtil.checkName(name);
        ValidationUtil.checkPhone(phone);
        userDao.updateProfile(me.id(), name.trim(), phone.trim());
        User fresh = userDao.findById(me.id()).orElse(me);
        SessionManager.login(fresh);
        return fresh;
    }

    public void changePassword(String oldPassword, String newPassword, String confirm) {
        User me = SessionManager.requireUser();
        String hash = userDao.findPasswordHashById(me.id()).orElse(null);
        if (!PasswordUtil.verify(oldPassword, hash)) throw new AppException("Current password is incorrect.");
        if (newPassword == null || !newPassword.equals(confirm)) throw new AppException("New passwords do not match.");
        ValidationUtil.checkPassword(newPassword);
        userDao.updatePassword(me.id(), PasswordUtil.hash(newPassword));
    }
}
