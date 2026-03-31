# 🔐 Admin Access Control - Quick Guide

## How It Works

### For Regular Users
- ✅ Can view all achievements (🏆 Achievements button visible)
- ✅ Can browse the achievements gallery
- ❌ Cannot see Admin Panel button
- ❌ Cannot upload or delete achievements

### For ADMIN User
- ✅ Can view all achievements
- ✅ Can see "⚙️ Admin Panel" button in sidebar
- ✅ Must enter password to access admin panel
- ✅ Can upload, view, and delete achievements

## Admin Login Credentials

**Username:** `ADMIN` (case-insensitive)  
**Password:** `123456`

## How to Login as Admin

### Step 1: Open the Application
```powershell
java -cp ".;lib/*" LoginFrame
```

### Step 2: Login with Admin Credentials
- Username: `ADMIN`
- Password: `123456`

### Step 3: Access Admin Panel
1. Look for "⚙️ Admin Panel" button in the sidebar (only visible for ADMIN)
2. Click the button
3. Enter admin password when prompted: `123456`
4. Admin Control Panel opens

## Features

### All Users Can See:
- 🏠 Dashboard
- 👤 My Profile
- 💬 Messages
- 🔔 Notifications
- 🏆 **Achievements** ← Everyone can view!

### Only ADMIN Can See:
- ⚙️ **Admin Panel** ← Only appears for ADMIN user

### Security Features:
1. **Username Check**: Admin Panel button only appears if logged in as "ADMIN"
2. **Password Verification**: Requires password "123456" to open Admin Panel
3. **Access Denied**: Shows error if wrong password entered

## Testing

### Test as Regular User:
1. Login as any regular user (not ADMIN)
2. Click "🏆 Achievements" - Should see all achievements ✅
3. Admin Panel button should NOT appear ✅

### Test as Admin:
1. Login with username: `ADMIN`, password: `123456`
2. Admin Panel button appears in sidebar ✅
3. Click Admin Panel button
4. Enter password: `123456`
5. Admin Control Panel opens ✅
6. Can upload/delete achievements ✅

## Creating ADMIN User in Database

If ADMIN user doesn't exist in your database yet, run this SQL:

```sql
USE alumnai;

-- Create ADMIN user
INSERT INTO users (username, password, email, role) 
VALUES ('ADMIN', '123456', 'admin@alumni.com', 'admin');

-- Get the user_id
SELECT user_id FROM users WHERE username = 'ADMIN';

-- Create profile for ADMIN (use the user_id from above)
INSERT INTO profiles (user_id, full_name, email, graduation_year) 
VALUES (LAST_INSERT_ID(), 'System Administrator', 'admin@alumni.com', 2024);
```

## Screenshots Flow

```
Regular User:
┌─────────────────┐
│   Dashboard     │
│ ├─ 🏠 Dashboard │
│ ├─ 👤 Profile   │
│ ├─ 💬 Messages  │
│ ├─ 🔔 Notices   │
│ └─ 🏆 Achievements │ ← Can view achievements
│                 │
└─────────────────┘

ADMIN User:
┌─────────────────┐
│   Dashboard     │
│ ├─ 🏠 Dashboard │
│ ├─ 👤 Profile   │
│ ├─ 💬 Messages  │
│ ├─ 🔔 Notices   │
│ ├─ 🏆 Achievements │ ← Can view achievements
│ │                │
│ └─ ⚙️ Admin Panel │ ← Only ADMIN sees this!
│       │          │
│       └─ [Password: 123456]
│              │
│         ┌────▼──────┐
│         │  Admin    │
│         │  Control  │
│         │  Panel    │
│         └───────────┘
└─────────────────┘
```

## Error Messages

### "Invalid password!"
- You entered the wrong admin password
- Correct password is: `123456`

### Admin Panel button not visible
- You're not logged in as ADMIN user
- Username must be exactly "ADMIN"

## Security Notes

⚠️ **Important**: This is a basic implementation. For production:
1. Store passwords hashed (bcrypt, SHA-256)
2. Use role-based access control (RBAC)
3. Store admin credentials securely in database
4. Add session timeout
5. Log admin actions
6. Use HTTPS

## Summary

✅ **Achievements visible to everyone**  
✅ **Admin Panel only for ADMIN user**  
✅ **Password protection: 123456**  
✅ **Double security: username + password**

---
**Admin Username:** ADMIN  
**Admin Password:** 123456
