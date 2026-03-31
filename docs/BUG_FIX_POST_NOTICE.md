# 🐛 Bug Fix: Post Notice Failed

## ❌ Problem
"Failed to post notice" error when trying to post job opportunities.

## 🔍 Root Cause
The **application deadline** field was being sent as a String (`"12-11-2026"`) but the database expects a SQL DATE type in format `YYYY-MM-DD`.

## ✅ Solution Applied

### 1. Fixed AlumniDAO.java
Changed from:
```java
stmt.setString(10, notif.getApplicationDeadline()); // ❌ Wrong!
```

To:
```java
// Convert string to SQL Date
if (notif.getApplicationDeadline() != null && !notif.getApplicationDeadline().trim().isEmpty()) {
  try {
    java.sql.Date sqlDate = java.sql.Date.valueOf(notif.getApplicationDeadline());
    stmt.setDate(10, sqlDate); // ✅ Correct!
  } catch (IllegalArgumentException e) {
    stmt.setDate(10, null);
  }
} else {
  stmt.setDate(10, null);
}
```

### 2. Added Validation in PostNotificationDialog.java
- Checks if date matches pattern: `YYYY-MM-DD`
- Validates the date is actually valid
- Shows helpful error messages

## 📝 How to Use (CORRECT FORMAT)

When posting a job notice:

**❌ WRONG Formats:**
- `12-11-2026` (DD-MM-YYYY)
- `11/12/2026` (MM/DD/YYYY)
- `2026/12/11` (YYYY/MM/DD with slashes)

**✅ CORRECT Format:**
```
2026-12-31
```
Format: `YYYY-MM-DD` (Year-Month-Day with dashes)

## 🎯 Example

**Job Posting:**
- Company: Google
- Position: Software Engineer
- Location: Mountain View
- Salary: $120,000 - $160,000
- **Deadline: 2026-12-31** ← Use this format!
- URL: https://careers.google.com/apply

## 🚀 Test It Now

1. Close and restart the app
2. Go to Notifications
3. Click "+ Post Notice"
4. Fill in job details
5. **Use deadline: 2026-12-31** (correct format)
6. Click "Post Notice"
7. ✅ Success!

---

**Fixed! The date format issue is resolved.** 🎉
