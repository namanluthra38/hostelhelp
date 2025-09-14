

CREATE TABLE IF NOT EXISTS student(
                         id UUID PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) UNIQUE NOT NULL,
                         password VARCHAR(255) NOT NULL,
                         graduation_year INT NOT NULL,
                         UID VARCHAR(50) UNIQUE NOT NULL,
                         address VARCHAR(255) NOT NULL,
                         date_of_birth DATE NOT NULL,
                         gender VARCHAR(10) NOT NULL,
                         phone VARCHAR(20) NOT NULL,
                         hostel VARCHAR(255),
                         room_number INT
);
-- Insert sample students
INSERT INTO student (id, name, email, password, graduation_year, UID, address, date_of_birth, gender, phone, hostel, room_number)
VALUES (RANDOM_UUID(), 'Aman Sharma', 'aman.sharma@example.com', 'pass123', 2026, 'UID001', 'Delhi', '2003-04-12', 'Male', '9999999999', 'Hostel A', 101);

INSERT INTO student (id, name, email, password, graduation_year, UID, address, date_of_birth, gender, phone, hostel, room_number)
VALUES (RANDOM_UUID(), 'Riya Verma', 'riya.verma@example.com', 'pass456', 2025, 'UID002', 'Mumbai', '2002-08-25', 'Female', '8888888888', 'Hostel B', 202);

INSERT INTO student (id, name, email, password, graduation_year, UID, address, date_of_birth, gender, phone, hostel, room_number)
VALUES (RANDOM_UUID(), 'Karan Singh', 'karan.singh@example.com', 'pass789', 2027, 'UID003', 'Chandigarh', '2004-02-15', 'Male', '7777777777', 'Hostel C', 303);

INSERT INTO student (id, name, email, password, graduation_year, UID, address, date_of_birth, gender, phone, hostel, room_number)
VALUES (RANDOM_UUID(), 'Sneha Patel', 'sneha.patel@example.com', 'pass321', 2026, 'UID004', 'Ahmedabad', '2003-12-05', 'Female', '6666666666', 'Hostel A', 104);

INSERT INTO student (id, name, email, password, graduation_year, UID, address, date_of_birth, gender, phone, hostel, room_number)
VALUES (RANDOM_UUID(), 'Arjun Mehta', 'arjun.mehta@example.com', 'pass654', 2025, 'UID005', 'Jaipur', '2002-10-30', 'Male', '5555555555', 'Hostel B', 205);

INSERT INTO student (id, name, email, password, graduation_year, UID, address, date_of_birth, gender, phone, hostel, room_number)
VALUES (RANDOM_UUID(), 'Priya Das', 'priya.das@example.com', 'pass987', 2026, 'UID006', 'Kolkata', '2003-06-18', 'Female', '4444444444', 'Hostel C', 306);

INSERT INTO student (id, name, email, password, graduation_year, UID, address, date_of_birth, gender, phone, hostel, room_number)
VALUES (RANDOM_UUID(), 'Rohit Gupta', 'rohit.gupta@example.com', 'pass111', 2027, 'UID007', 'Lucknow', '2004-01-21', 'Male', '3333333333', 'Hostel A', 107);

INSERT INTO student (id, name, email, password, graduation_year, UID, address, date_of_birth, gender, phone, hostel, room_number)
VALUES (RANDOM_UUID(), 'Neha Reddy', 'neha.reddy@example.com', 'pass222', 2025, 'UID008', 'Hyderabad', '2002-11-14', 'Female', '2222222222', 'Hostel B', 208);

INSERT INTO student (id, name, email, password, graduation_year, UID, address, date_of_birth, gender, phone, hostel, room_number)
VALUES (RANDOM_UUID(), 'Vikram Nair', 'vikram.nair@example.com', 'pass333', 2026, 'UID009', 'Kochi', '2003-09-09', 'Male', '1111111111', 'Hostel C', 309);

INSERT INTO student (id, name, email, password, graduation_year, UID, address, date_of_birth, gender, phone, hostel, room_number)
VALUES (RANDOM_UUID(), 'Ananya Iyer', 'ananya.iyer@example.com', 'pass444', 2027, 'UID010', 'Chennai', '2004-05-02', 'Female', '1234567890', 'Hostel A', 110);
