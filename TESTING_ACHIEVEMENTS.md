# Testing the Achievements Feature

## Quick Start

### 1. Create the Database Table
Run this SQL command in your MySQL client:

```sql
USE alumnai;

CREATE TABLE IF NOT EXISTS achievements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    photo_path VARCHAR(500) NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2. Test the Admin Panel
```bash
# Run the admin control panel directly
java -cp ".;lib/*" AdminControlPanel
```

Features to test:
- Click "Select Photo" and choose an image
- Enter a title and description
- Click "Upload Achievement"
- Click "View All" to see the gallery
- Test "Delete Achievement" with an ID

### 3. Test the Achievements Gallery
```bash
# Run the achievements page directly
java -cp ".;lib/*" AchievementsPage
```

### 4. Test from Main Dashboard
```bash
# Run the main dashboard
java -cp ".;lib/*" ModernDashboardUI
```

Then:
1. Click "🏆 Achievements" to view all achievements
2. Click "⚙️ Admin Panel" to manage achievements

## Sample Test Data

Insert some test achievements:

```sql
INSERT INTO achievements (title, description, photo_path) VALUES
('Outstanding Alumni Award 2025', 'Awarded to John Doe for exceptional contributions to the field of Computer Science and community service.', 'resources/images/achievements/award1.jpg'),
('Sports Excellence Championship', 'Our alumni team won the national inter-college sports championship, showcasing teamwork and dedication.', 'resources/images/achievements/sports1.jpg'),
('Innovation in Technology', 'Alumni startup developed groundbreaking AI solution, revolutionizing healthcare diagnostics.', 'resources/images/achievements/tech1.jpg');
```

Note: You'll need to place actual image files in `resources/images/achievements/` with these names, or update the paths after uploading through the admin panel.

## Verification Checklist

- [ ] Database table created successfully
- [ ] Admin Panel opens without errors
- [ ] Can select and preview photos
- [ ] Can upload achievements
- [ ] Achievements Gallery displays uploaded items
- [ ] Can delete achievements by ID
- [ ] Navigation from ModernDashboardUI works
- [ ] Images display correctly in gallery
- [ ] Refresh button works
- [ ] Empty state displays when no achievements exist

## Expected UI Flow

1. **First Time (No Achievements)**:
   - Achievements page shows: "🏆 No achievements yet"
   - Message: "Contact admin to add achievements"

2. **After Admin Upload**:
   - Admin panel shows success message
   - Gallery refreshes and displays new achievement
   - Achievement card shows: title, description, photo, date

3. **Viewing in Dashboard**:
   - Sidebar shows "🏆 Achievements" button
   - Clicking opens gallery in main content area
   - "↻ Refresh" button reloads content

## Common Issues & Solutions

### Issue: Photos not displaying
**Solution**: 
- Ensure photos are in `resources/images/achievements/`
- Check file permissions
- Verify database photo_path is correct

### Issue: "Table doesn't exist" error
**Solution**: 
- Run the CREATE TABLE SQL command
- Verify database name is "alumnai"
- Check database connection credentials

### Issue: Admin button not visible
**Solution**: 
- Recompile ModernDashboardUI.java
- Restart the application
- Clear any cached .class files

## Demo Workflow

1. **Admin uploads achievement**:
   - Open Admin Panel
   - Fill in: Title = "Best Project Award 2025"
   - Description = "Awarded for innovative final year project"
   - Select a photo
   - Click "Upload Achievement"
   - See success message

2. **Users view achievement**:
   - Open ModernDashboardUI
   - Click "🏆 Achievements"
   - See the new achievement card with photo

3. **Admin deletes achievement**:
   - Note the achievement ID from gallery
   - Click "Delete Achievement" in Admin Panel
   - Enter the ID
   - Confirm deletion
   - Gallery updates automatically

---
**Status**: ✅ Feature Implemented and Ready for Testing
