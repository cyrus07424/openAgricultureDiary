# Slack Notification Setup Guide

## Overview
This application now includes Slack notification functionality that sends notifications to a Slack channel via webhook for various user activities.

## Setup Instructions

### 1. Create a Slack Webhook URL

1. Go to your Slack workspace
2. Navigate to "Apps" > "Manage Apps" > "Custom Integrations" > "Incoming Webhooks"
3. Click "Add to Slack"
4. Choose the channel where you want notifications to be sent
5. Copy the webhook URL (it will look like: `https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX`)

### 2. Configure Environment Variable

Set the `SLACK_WEBHOOK_URL` environment variable with your webhook URL:

```bash
export SLACK_WEBHOOK_URL="https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX"
```

For production deployment, set this environment variable in your deployment platform (Heroku, AWS, etc.).

### 3. Restart the Application

After setting the environment variable, restart your application to enable Slack notifications.

## Notification Types

The application will send Slack notifications for the following events:

### User Events
- **User Registration**: When a new user creates an account
- **User Login**: When a user logs into the system

### Data Operations
- **Crop Operations**: Create, update, delete crop records
- **Field Operations**: Create, update, delete field records
- **Work History Operations**: Create, update, delete work history records

## Notification Content

Each notification includes:
- Event type and description (in Japanese)
- User information (ID, username, email)
- Request details (IP address, User-Agent)
- Timestamp information

## Example Notification

```
🆕 新規ユーザー登録 - john_doe (john@example.com)

ユーザー情報
ID: 123
ユーザー名: john_doe
メール: john@example.com

リクエスト情報
IP: 192.168.1.100
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36
```

## Testing

To test the Slack integration:

1. Set up the webhook URL as described above
2. Start the application
3. Perform any of the monitored actions (register, login, create/update/delete data)
4. Check your Slack channel for notifications

## Troubleshooting

- If notifications are not working, check the application logs for error messages
- Ensure the `SLACK_WEBHOOK_URL` environment variable is correctly set
- Verify the webhook URL is valid and the Slack app has permissions to post to the channel
- The application will continue to work normally even if Slack notifications fail