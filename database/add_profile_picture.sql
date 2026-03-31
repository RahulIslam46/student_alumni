-- Add profile_picture column to profiles table
-- Run this once if the column does not already exist

ALTER TABLE profiles 
ADD COLUMN IF NOT EXISTS profile_picture VARCHAR(500) DEFAULT NULL;

-- (The Java app also runs this automatically at startup via AlumniDAO.ensureProfilePictureColumn())
