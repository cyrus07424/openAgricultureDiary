# Email Functionality Implementation

This document describes the email functionality that has been implemented for user registration and password reset using SendGrid.

## Features Implemented

### 1. Welcome Email on Registration
- When a user successfully registers, a welcome email is automatically sent
- Email is sent asynchronously to avoid delaying the registration process
- If email sending fails, the registration still succeeds (graceful degradation)

### 2. Password Reset Functionality
- Users can click "パスワードを忘れた方はこちら" on the login page
- Enter their email address to receive a password reset link
- Reset token is valid for 24 hours
- Secure token generation using UUID
- Reset link redirects to password reset form

### 3. Email Service Configuration
The EmailService requires the following environment variables:

```bash
SENDGRID_API_KEY=your_sendgrid_api_key
SENDGRID_FROM_EMAIL=your_sender_email@example.com
SENDGRID_FROM_NAME=Open Agriculture Diary (optional)
```

## Database Changes

Added two new columns to the `app_user` table:
- `reset_token` - varchar(255) - Stores the password reset token
- `reset_token_expires` - timestamp - Token expiration time

## New Routes

```
GET   /forgot-password    - Display forgot password form
POST  /forgot-password    - Handle forgot password request
GET   /reset-password     - Display reset password form (with token parameter)
POST  /reset-password     - Handle password reset submission
```

## New Files Created

### Controllers
- Enhanced `AuthController.java` with password reset methods

### Forms
- `ForgotPasswordForm.java` - Form for requesting password reset
- `ResetPasswordForm.java` - Form for setting new password

### Services
- `EmailService.java` - SendGrid integration service

### Views
- `forgotPassword.scala.html` - Forgot password form
- `resetPassword.scala.html` - Reset password form
- Updated `login.scala.html` with forgot password link

### Models
- Enhanced `User.java` with reset token functionality

### Tests
- `EmailFunctionalityTest.java` - Tests for email functionality

## Usage

1. Set up SendGrid account and get API key
2. Configure environment variables
3. Users can register and receive welcome emails
4. Users can reset passwords via email when needed

## Error Handling

- Email service gracefully handles missing configuration
- Invalid or expired reset tokens show appropriate error messages
- Email sending failures don't break the user experience

## Security Features

- Reset tokens expire after 24 hours
- Tokens are generated using secure UUID
- Password reset doesn't reveal if email exists in system
- New passwords must meet minimum requirements (6+ characters)