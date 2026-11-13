CREATE DATABASE IF NOT EXISTS hostelcare_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hostelcare_db;
CREATE TABLE IF NOT EXISTS hostels (
 hostel_id BIGINT PRIMARY KEY AUTO_INCREMENT, hostel_name VARCHAR(100) NOT NULL, block_name VARCHAR(50), address VARCHAR(255)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS rooms (
 room_id BIGINT PRIMARY KEY AUTO_INCREMENT, hostel_id BIGINT NOT NULL, room_number VARCHAR(20) NOT NULL, floor INT, capacity INT,
 CONSTRAINT fk_rooms_hostel FOREIGN KEY(hostel_id) REFERENCES hostels(hostel_id), UNIQUE KEY uq_hostel_room(hostel_id,room_number), CHECK(capacity IS NULL OR capacity>0)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS users (
 user_id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100) NOT NULL, email VARCHAR(150) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL,
 phone VARCHAR(15), role VARCHAR(20) NOT NULL, room_id BIGINT NULL, active BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_users_room FOREIGN KEY(room_id) REFERENCES rooms(room_id), CHECK(role IN ('STUDENT','STAFF','ADMIN'))
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS categories (
 category_id BIGINT PRIMARY KEY AUTO_INCREMENT, category_name VARCHAR(100) NOT NULL UNIQUE, description VARCHAR(255)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS complaints (
 complaint_id BIGINT PRIMARY KEY AUTO_INCREMENT, student_id BIGINT NOT NULL, room_id BIGINT NOT NULL, category_id BIGINT NOT NULL,
 title VARCHAR(150) NOT NULL, description TEXT NOT NULL, priority VARCHAR(20) NULL, status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 CONSTRAINT fk_complaint_student FOREIGN KEY(student_id) REFERENCES users(user_id), CONSTRAINT fk_complaint_room FOREIGN KEY(room_id) REFERENCES rooms(room_id),
 CONSTRAINT fk_complaint_category FOREIGN KEY(category_id) REFERENCES categories(category_id), CHECK(priority IS NULL OR priority IN ('LOW','MEDIUM','HIGH')),
 CHECK(status IN ('SUBMITTED','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED','REOPENED','REJECTED')), INDEX idx_complaint_status(status), INDEX idx_complaint_created(created_at)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS assignments (
 assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT, complaint_id BIGINT NOT NULL, staff_id BIGINT NOT NULL, assigned_by BIGINT NOT NULL,
 assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, is_active BOOLEAN NOT NULL DEFAULT TRUE,
 CONSTRAINT fk_assignment_complaint FOREIGN KEY(complaint_id) REFERENCES complaints(complaint_id), CONSTRAINT fk_assignment_staff FOREIGN KEY(staff_id) REFERENCES users(user_id),
 CONSTRAINT fk_assignment_admin FOREIGN KEY(assigned_by) REFERENCES users(user_id), INDEX idx_assignment_active(complaint_id,is_active), INDEX idx_assignment_staff(staff_id,is_active)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS complaint_attachments (
 attachment_id BIGINT PRIMARY KEY AUTO_INCREMENT, complaint_id BIGINT NOT NULL, file_name VARCHAR(255) NOT NULL, file_path VARCHAR(500) NOT NULL,
 uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, CONSTRAINT fk_attachment_complaint FOREIGN KEY(complaint_id) REFERENCES complaints(complaint_id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS complaint_history (
 history_id BIGINT PRIMARY KEY AUTO_INCREMENT, complaint_id BIGINT NOT NULL, updated_by BIGINT NULL, old_status VARCHAR(30), new_status VARCHAR(30) NOT NULL,
 remarks TEXT, changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, CONSTRAINT fk_history_complaint FOREIGN KEY(complaint_id) REFERENCES complaints(complaint_id),
 CONSTRAINT fk_history_user FOREIGN KEY(updated_by) REFERENCES users(user_id), INDEX idx_history_complaint(complaint_id,changed_at)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS feedback (
 feedback_id BIGINT PRIMARY KEY AUTO_INCREMENT, complaint_id BIGINT NOT NULL UNIQUE, student_id BIGINT NOT NULL, rating INT NOT NULL, comments TEXT,
 submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, CONSTRAINT fk_feedback_complaint FOREIGN KEY(complaint_id) REFERENCES complaints(complaint_id),
 CONSTRAINT fk_feedback_student FOREIGN KEY(student_id) REFERENCES users(user_id), CHECK(rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;
INSERT IGNORE INTO categories(category_name,description) VALUES
('Electrical','Electrical repairs and lighting'),('Plumbing','Water supply and leakage'),('Cleaning','Housekeeping and sanitation'),('Furniture','Beds, chairs and fixtures'),('Internet','Wi-Fi and network issues'),('Civil Maintenance','Building and structural repairs'),('Other','Other maintenance issues');
INSERT INTO hostels(hostel_name,block_name,address) SELECT 'Main Hostel','Block A','Update address' WHERE NOT EXISTS(SELECT 1 FROM hostels);
INSERT INTO rooms(hostel_id,room_number,floor,capacity) SELECT h.hostel_id, r.room_number, r.floor, 3 FROM hostels h JOIN (
 SELECT '101' room_number,1 floor UNION ALL SELECT '102',1 UNION ALL SELECT '103',1 UNION ALL SELECT '201',2 UNION ALL SELECT '202',2 UNION ALL SELECT '203',2
) r ON h.hostel_name='Main Hostel' WHERE NOT EXISTS(SELECT 1 FROM rooms WHERE rooms.hostel_id=h.hostel_id AND rooms.room_number=r.room_number);
