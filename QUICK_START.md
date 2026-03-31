# 🚀 Alumni Connect - Quick Start Guide

## Prerequisites
- ✅ Java JDK 23 installed
- ✅ MySQL 8.x running
- ✅ JDBC Driver: mysql-connector-j-9.5.0.jar

---

## 1️⃣ Database Setup (One-Time)

```powershell
# Navigate to project folder
cd e:\project

# Setup main database
mysql -u root -p < database\database_setup.sql

# Setup notification system
mysql -u root -p < database\notifications_schema.sql
```

**Password**: `445784!`

---

## 2️⃣ Compile Application

```powershell
javac -cp ".;c:\Users\RA\OneDrive\Desktop\mysql-connector-j-9.5.0.jar" *.java
```

---

## 3️⃣ Run Application

```powershell
java -cp ".;c:\Users\RA\OneDrive\Desktop\mysql-connector-j-9.5.0.jar" LoginFrame
```

---

## 4️⃣ First Time Usage

### Register New User:
1. Click **"Create Account"** on login screen
2. Fill in:
   - Username
   - Email
   - Password
   - Name
   - Batch Year
   - Branch
   - Company (optional)
3. Click **"Register"**

### Login:
1. Enter username and password
2. Click **"Login"**
3. Dashboard opens automatically

---

## 5️⃣ Main Features

### 📱 Dashboard Tabs:
- **Profile** - View/edit your information
- **Network** - Browse all alumni
- **Messages** - Chat with other alumni
- **Notifications** - Job postings, events, announcements

### 💼 Post a Job:
1. Go to **Notifications** tab
2. Click **"+ Post Notice"**
3. Select type: **Job Posting**
4. Fill details:
   - Title: "Software Engineer Opening"
   - Company Name: "Tech Corp"
   - Position: "Senior Developer"
   - Location: "Remote"
   - Job Type: "Full-time"
   - Salary Range: "$80k-$120k"
   - **Deadline**: `2026-12-31` ⚠️ **Use YYYY-MM-DD format**
   - Application URL: "https://company.com/apply"
5. Click **"Post Notice"**

### 💬 Send Messages:
1. Go to **Network** tab
2. Click **"Message"** on any alumni card
3. Type message and click **"Send"**

---

## ⚠️ Important Notes

### Date Format:
- Always use **YYYY-MM-DD** format
- ✅ Correct: `2026-12-31`
- ❌ Wrong: `12-31-2026` or `31-12-2026`

### Database Credentials:
- Host: `127.0.0.1:3306`
- Database: `alumnai`
- User: `root`
- Password: `445784!`

---

## 🐛 Troubleshooting

### "Failed to post notice"
- Check date format is YYYY-MM-DD
- Verify all required fields filled

### "Database connection failed"
- Ensure MySQL is running
- Check password is correct (`445784!`)
- Verify database name is `alumnai`

### Compilation errors
- Check JDBC driver path matches your system
- Ensure all .java files are in same directory

---

## 📁 Project Structure

```
e:\project\
├── database/          # SQL schema files
├── docs/              # Documentation
├── *.java             # Source code
└── QUICK_START.md     # This file
```

---

## 📚 More Help

- **Full Documentation**: [docs/PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md)
- **Setup Guide**: [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md)
- **User Guide**: [docs/USER_GUIDE.md](docs/USER_GUIDE.md)
- **Notification Guide**: [docs/NOTIFICATION_SYSTEM_GUIDE.md](docs/NOTIFICATION_SYSTEM_GUIDE.md)

---

**Ready to start? Run the application and explore!** 🎉
