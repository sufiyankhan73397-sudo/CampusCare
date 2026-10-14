# M1 - Authentication & Security

**Owner:** Sufiyan Khan (Team Lead)
**Branch:** `develop`

## Purpose
Lets users register, log in and manage their account, and makes sure every screen is used only by the right role (Student, Staff, Admin).

## Features
- Student registration with validation
- Login for all three roles, logout
- Edit profile (name, phone) and change password
- Role-based access to screens and session handling
- Default admin and one demo staff account per department created on first run
- Passwords stored as BCrypt hashes

## Code
| Layer | Classes |
|---|---|
| Controller | LoginController, RegisterController, ProfileController |
| Service | AuthService: login, register, createUser, updateProfile, changePassword |
| DAO | UserDao |
| Util | SessionManager, PasswordUtil, ValidationUtil, AlertUtil |

## Database tables
users

## Validation rules
- Name: 3 to 60 characters
- Email: valid format and unique
- Phone: valid format
- Password: at least 8 characters with one uppercase letter, one lowercase letter and one digit

## Screenshots
See docs/screenshots (login, register, profile).

## Testing
[to be filled: test cases and results]

## Contribution
[to be filled by owner: what was built]