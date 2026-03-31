-- Achievements table schema
CREATE TABLE IF NOT EXISTS achievements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    photo_path VARCHAR(500) NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sample data (optional)
-- INSERT INTO achievements (title, description, photo_path) VALUES
-- ('Best Student Award 2025', 'Awarded to outstanding students for academic excellence', 'resources/images/achievements/award1.jpg'),
-- ('Sports Championship', 'Winners of the inter-college sports championship', 'resources/images/achievements/sports1.jpg');
