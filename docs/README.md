# Alumni Connect - Complete Guide

## ✅ What's Working Now

### 1. **Registration System** ✨
- When someone registers as **Student** or **Alumni**, their profile is automatically created
- Profile appears immediately in the dashboard for everyone to see
- Both students and alumni can see each other's profiles

### 2. **Dashboard Features** 🎯
- **View All Profiles**: See all students and alumni
- **Search**: Find people by name, skills, or company
- **Smart Badges**: 
  - Green badge = ALUMNI (graduated professionals)
  - Blue badge = STUDENT (current students)
- **Statistics**: Shows total alumni, students, and professionals

### 3. **Messaging System** 💬
- Click **"💬 Message"** button on any profile card
- Opens chat window to send messages
- Students can message Alumni for mentorship
- Alumni can message Students to offer guidance
- Everyone can message each other!

### 4. **Profile Management** 👤
- View your own profile
- Edit profile information
- Update skills, company, bio, etc.

---

## 🚀 How to Use

### Step 1: Setup Database
Run the SQL script to create tables:
```bash
mysql -u root -p < database_setup.sql
```

Or manually run the SQL commands in MySQL Workbench.

### Step 2: Start the Application
```powershell
cd e:\project
javac -cp ".;c:\Users\RA\OneDrive\Desktop\mysql-connector-j-9.5.0.jar" *.java
java -cp ".;c:\Users\RA\OneDrive\Desktop\mysql-connector-j-9.5.0.jar" LoginFrame
```

### Step 3: Register New Users
1. Click **"Register"** on login screen
2. Fill in:
   - Username (min 3 characters)
   - Full Name
   - Email
   - Select "Student" or "Alumni"
   - Password (min 6 characters)
3. Click **"Create Account"**
4. ✅ Profile automatically appears in dashboard!

### Step 4: Login & Explore
1. Login with your username/password
2. Dashboard opens showing all profiles
3. **Search** for specific people
4. **View Profile** to see details
5. **Send Message** to start conversation

---

## 📊 Profile Flow

```
Registration
    ↓
Creates User Account
    ↓
Creates Profile Entry
    ↓
Appears in Dashboard
    ↓
Everyone Can See & Message
```

---

## 💬 Messaging Flow

```
User A clicks "Message" on User B's profile
    ↓
Chat window opens
    ↓
User A types message and sends
    ↓
Message saved to database
    ↓
User B sees message in their Messages view
    ↓
User B replies
    ↓
Conversation continues!
```

---

## 🎨 Design Features

### Modern UI Elements
- ✨ Gradient backgrounds
- 🎯 Rounded cards with shadows
- 🌈 Color-coded badges (Student vs Alumni)
- 🔍 Real-time search
- 📱 Clean, professional layout
- 🎭 Smooth hover effects
- 📊 Statistics dashboard

### Navigation
- 🏠 **Dashboard**: Browse all profiles
- 👤 **My Profile**: View/edit your info
- 💬 **Messages**: See all conversations
- 🔔 **Notifications**: Coming soon
- ⚙️ **Settings**: Coming soon

---

## 🔒 Database Structure

### Tables
1. **users** - Login credentials
2. **profiles** - Public profile info (shown in dashboard)
3. **messages** - Chat messages between users

### How Registration Works
```sql
-- Step 1: Create user account
INSERT INTO users (username, password, email, full_name) VALUES ...

-- Step 2: Create profile (shown in dashboard)
INSERT INTO profiles (user_id, full_name, email, graduation_year, ...) VALUES ...

-- Now user appears in dashboard for everyone!
```

---

## 🎯 Example Scenarios

### Scenario 1: New Student Registers
1. "Sumon" registers as **Student**
2. Profile created with:
   - Name: Sumon
   - Role: Student
   - Graduation Year: 2026
   - Company: University
3. ✅ Sumon now appears in dashboard with **STUDENT** badge
4. Alumni can message Sumon for guidance

### Scenario 2: New Alumni Registers
1. "Rahim" registers as **Alumni**
2. Profile created with:
   - Name: Rahim
   - Role: Alumni
   - Graduation Year: 2020
   - Company: Not specified yet
3. ✅ Rahim now appears in dashboard with **ALUMNI** badge
4. Students can message Rahim for mentorship

### Scenario 3: Messaging
1. Student "Sumon" sees Alumni "Rahim" in dashboard
2. Clicks **"💬 Message"** button
3. Chat window opens
4. Sumon: "Hi, can you help me with career advice?"
5. Rahim receives message and replies
6. Both can continue conversation!

---

## 🐛 Troubleshooting

### Profile Not Appearing?
1. Check database connection in `DatabaseConnection.java`
2. Verify database tables exist
3. Check if profile was created: `SELECT * FROM profiles;`

### Can't Send Messages?
1. Ensure `messages` table exists
2. Check both users are logged in
3. Verify user IDs are correct

### Mock Data Showing?
- If database is empty, app shows sample profiles
- Add real users via registration
- Real profiles will replace mock data

---

## 📝 Summary

✅ **Registration** → Creates profile in database  
✅ **Profile** → Appears in dashboard for all users  
✅ **Students & Alumni** → Both visible with badges  
✅ **Messaging** → Everyone can message each other  
✅ **Modern Design** → Cool and professional UI  

**Everything is connected and working!** 🎉
