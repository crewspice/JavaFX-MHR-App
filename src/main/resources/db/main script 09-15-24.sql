-- Drop the database if it exists
DROP DATABASE IF EXISTS practice_db;

-- Create a new database
CREATE DATABASE practice_db;

-- Use the newly created database
USE practice_db;

-- Create customers table
CREATE TABLE customers (
    customer_id INT PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100)
);

-- Create orders table with status and driver columns
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    order_date DATE,
    amount DECIMAL(10, 2),
    status VARCHAR(20) DEFAULT 'Pending',  -- Adjusted default value and type
    driver CHAR(2) DEFAULT '',   -- Driver assigned, empty by default
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- Insert customers
INSERT INTO customers (customer_id, name, email)
VALUES (1, 'Alice', 'alice@example.com'),
       (2, 'Bob', 'bob@example.com');

-- Insert orders
INSERT INTO orders (customer_id, order_date, amount)
VALUES (1, '2024-09-10', 50.00),
       (2, '2024-09-11', 75.00);

-- Sample query to see results
SELECT * FROM customers;
SELECT * FROM orders;

-- Sample join query
SELECT orders.order_id, customers.name, orders.amount, orders.status, orders.driver
FROM orders
JOIN customers ON orders.customer_id = customers.customer_id;
