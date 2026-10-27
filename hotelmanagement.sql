CREATE DATABASE IF NOT EXISTS hotel_management;
USE hotel_management;

CREATE TABLE IF NOT EXISTS rooms (
    room_number INT PRIMARY KEY,
    room_type VARCHAR(50) NOT NULL,
    tariff_per_night DECIMAL(10, 2) NOT NULL CHECK (tariff_per_night >= 0)
);

CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number INT NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    total_cost DECIMAL(10, 2) NOT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_room FOREIGN KEY (room_number) REFERENCES rooms(room_number),
    CONSTRAINT chk_booking_dates CHECK (check_out > check_in)
);

-- Sample rooms. INSERT IGNORE avoids duplicates when this script is run again.
INSERT IGNORE INTO rooms (room_number, room_type, tariff_per_night) VALUES
(101, 'Single', 1500.00),
(102, 'Double', 2500.00),
(103, 'Deluxe', 3500.00),
(201, 'Double', 2500.00),
(202, 'Suite', 5000.00);
