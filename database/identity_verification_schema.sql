-- =====================================================
-- Identity Verification Schema
-- =====================================================
-- Stores student/alumni proof documents uploaded at registration.
-- =====================================================

USE alumnai;

CREATE TABLE IF NOT EXISTS identity_verifications (
    verification_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id              INT NOT NULL,
    user_type            VARCHAR(20) NOT NULL,
    document_type        VARCHAR(50) NOT NULL,
    document_path        VARCHAR(500) NOT NULL,
    verification_status  ENUM('pending','approved','rejected') DEFAULT 'pending',
    reviewer_note        VARCHAR(500) DEFAULT NULL,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at          TIMESTAMP NULL,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (verification_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
