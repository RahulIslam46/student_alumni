# Alumni Connect - Project Structure 📁

## Overview
Complete Alumni Management System with modern UI, messaging, and job notification features.

---

## 📂 Project Directory Structure

```
e:\project\
│
├── 📁 database/              # Database Schema Files
│   ├── database_setup.sql           # Main database structure (users, profiles, messages)
│   └── notifications_schema.sql     # Notification system tables
│
├── 📁 docs/                  # Documentation
│   ├── README.md                    # Project overview
│   ├── SETUP_GUIDE.md               # Installation instructions
│   ├── USER_GUIDE.md                # How to use features
│   ├── NOTIFICATION_SYSTEM_GUIDE.md # Notification feature guide
│   ├── BUG_FIX_POST_NOTICE.md      # Date format fix documentation
│   ├── FIX_SUMMARY.md               # All fixes applied
│   └── PROJECT_STRUCTURE.md         # This file
│
├── 📄 Java Source Files      # Application Code
│   ├── LoginFrame.java              # Login UI
│   ├── RegistrationFrame.java       # User registration
│   ├── ModernDashboardUI.java       # Main dashboard (PRIMARY UI)
│   ├── ChatConversationFrame.java   # One-on-one messaging
│   ├── ProfileEditDialog.java       # Edit profile dialog
│   ├── PostNotificationDialog.java  # Post job/event/announcement
│   ├── AlumniDAO.java               # Database access layer
│   ├── DatabaseConnection.java      # MySQL connection manager
│   ├── AlumniProfile.java           # Profile data model
│   ├── Message.java                 # Message data model
│   └── Notification.java            # Notification data model
│
└── 📄 Scripts
    └── run-ui-tests.ps1             # UI testing automation script

```

---

## 🎯 Core Features

### 1. Authentication System
- **Login**: [LoginFrame.java](../LoginFrame.java)
- **Registration**: [RegistrationFrame.java](../RegistrationFrame.java)
- Database authentication via AlumniDAO

### 2. Main Dashboard
- **File**: [ModernDashboardUI.java](../ModernDashboardUI.java) - **PRIMARY UI**
- **Features**:
  - Profile view and editing
  - Alumni network browsing
  - Real-time messaging
  - Notification feed
  - Job postings/events/announcements

### 3. Messaging System
- **File**: [ChatConversationFrame.java](../ChatConversationFrame.java)
- One-on-one chat between alumni
- Real-time message sending
- Message history with timestamps

### 4. Notification System
- **File**: [PostNotificationDialog.java](../PostNotificationDialog.java)
- Post job opportunities
- Post events and announcements
- Priority levels (HIGH, MEDIUM, LOW)
- Read/unread tracking
- Application deadline tracking

---

## 🗄️ Database Schema

### Tables:
1. **users** - User authentication (user_id, username, password, email)
2. **profiles** - Alumni profiles (user_id, name, batch_year, branch, company, etc.)
3. **messages** - Chat messages (sender_id, receiver_id, content, timestamp)
4. **notifications** - Job postings/events (notification_id, title, content, type, company_name, etc.)
5. **notification_views** - Read tracking (notification_id, user_id, viewed_at)

### Setup Commands:
```powershell
# Run from project root
mysql -u root -p < database\database_setup.sql
mysql -u root -p < database\notifications_schema.sql
```

---

## 🔧 Technical Stack

- **Language**: Java SE (JDK 23)
- **GUI Framework**: Swing
- **Database**: MySQL 8.x
- **JDBC Driver**: mysql-connector-j-9.5.0.jar
- **Architecture**: MVC with DAO pattern

---

## ▶️ How to Run

### Compilation:
```powershell
javac -cp ".;c:\Users\RA\OneDrive\Desktop\mysql-connector-j-9.5.0.jar" *.java
```

### Run Application:
```powershell
java -cp ".;c:\Users\RA\OneDrive\Desktop\mysql-connector-j-9.5.0.jar" LoginFrame
```

---

## 📝 Key Implementation Details

### Date Format Requirements:
- **Application Deadline**: YYYY-MM-DD format (e.g., 2026-12-31)
- **Validation**: Regex pattern `\d{4}-\d{2}-\d{2}`
- **Conversion**: `java.sql.Date.valueOf(dateString)`

### Database Connection:
- **Host**: 127.0.0.1:3306
- **Database**: alumnai
- **User**: root
- **Password**: 445784!

### Primary Key Naming:
- All tables use `user_id` (not `id`) for consistency
- Foreign keys: `sender_id`, `receiver_id`, `posted_by_user_id`

---

## 🐛 Recent Fixes Applied

1. ✅ Fixed column naming consistency (user_id across all tables)
2. ✅ Fixed message timestamp column (changed from sent_at to timestamp)
3. ✅ Fixed notification posting date format validation
4. ✅ Added proper error handling with detailed messages
5. ✅ Removed duplicate files (AlumniDashboard.java, MainFrame.java)

---

## 📚 Documentation Files

- **[README.md](README.md)** - Project overview and quick start
- **[SETUP_GUIDE.md](SETUP_GUIDE.md)** - Detailed installation steps
- **[USER_GUIDE.md](USER_GUIDE.md)** - Feature usage instructions
- **[NOTIFICATION_SYSTEM_GUIDE.md](NOTIFICATION_SYSTEM_GUIDE.md)** - Notification feature guide
- **[BUG_FIX_POST_NOTICE.md](BUG_FIX_POST_NOTICE.md)** - Date format fix details
- **[FIX_SUMMARY.md](FIX_SUMMARY.md)** - All bug fixes log

---

## 🎨 UI Components

### Color Scheme:
- **Primary Blue**: #0A66C2 (LinkedIn-style)
- **Sidebar**: #2C3E50 (Dark charcoal)
- **Background**: #F0F2F5 (Light gray)
- **Accent**: Gradient backgrounds for cards

### Custom Components:
- Rounded borders
- Avatar icons with initials
- Material Design-inspired cards
- Hover effects on buttons
- Badge indicators for unread items

---

## 🚀 Next Steps for Development

1. Add email notifications for job postings
2. Implement search/filter in notification feed
3. Add file attachment support in messaging
4. Export profile as PDF
5. Advanced analytics dashboard

---

**Last Updated**: January 17, 2026  
**Version**: 1.0  
**Status**: Production Ready ✅
