-- =====================================================
-- Alumni Connect Database Setup - CORRECTED Schema
-- =====================================================
-- Run this SQL script in your MySQL database
-- Fixed: Consistent user_id naming across all tables
-- =====================================================

-- Create database
CREATE DATABASE IF NOT EXISTS alumnai;
USE alumnai;

-- Drop existing tables in correct order (foreign keys first)
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS profiles;
DROP TABLE IF EXISTS alumni_profiles;  -- Old table if exists
DROP TABLE IF EXISTS users;

-- =====================================================
-- 1. USERS TABLE - Authentication & Login
-- =====================================================
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 2. PROFILES TABLE - Dashboard Display
-- =====================================================
CREATE TABLE profiles (
    user_id INT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    graduation_year INT,
    company VARCHAR(100),
    job_role VARCHAR(100),
    skills TEXT,
    bio TEXT,
    profile_picture VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_job_role (job_role),
    INDEX idx_graduation_year (graduation_year),
    INDEX idx_company (company)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 3. MESSAGES TABLE - Chat Functionality
-- =====================================================
CREATE TABLE messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    content TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_conversation (sender_id, receiver_id, timestamp),
    INDEX idx_unread (receiver_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Check all tables
SHOW TABLES;

-- Describe table structures
DESCRIBE users;
DESCRIBE profiles;
DESCRIBE messages;

-- Count records (will be 0 initially)
SELECT 'Users' AS Table_Name, COUNT(*) AS Record_Count FROM users
UNION ALL
SELECT 'Profiles', COUNT(*) FROM profiles
UNION ALL
SELECT 'Messages', COUNT(*) FROM messages;

-- Verify foreign key constraints
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    REFERENCED_TABLE_SCHEMA = 'alumnai'
    AND REFERENCED_TABLE_NAME IS NOT NULL;

SELECT '✅ Database setup completed successfully!' AS Status;
SELECT '📝 All tables created with consistent naming (user_id)' AS Message;
SELECT '👉 Register users through the application' AS Instructions;
