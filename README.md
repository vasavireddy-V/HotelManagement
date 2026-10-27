# Hotel Management System (Java + JDBC)

A console-based hotel management application using Java, JDBC, and MySQL.

## Features
- Stores room number, room type, and tariff per night.
- Lists all rooms and their tariffs.
- Checks room availability for requested check-in and check-out dates.
- Books a room only when it is available for the full date range.
- Calculates total price using the number of nights.
- Displays saved bookings.

## Files
- `HotelManagement.java` — Java application.
- `hotelmanagement.sql` — creates the database and tables and inserts sample rooms.

## Requirements
- Java JDK 8 or newer.
- MySQL Server.
- MySQL Connector/J JDBC driver.

## Setup
1. Start MySQL.
2. Run `hotelmanagement.sql` in MySQL Workbench or the MySQL command line.
3. Ensure the MySQL JDBC driver (Connector/J) is available on the classpath.
4. Set database connection values if needed:
   - `DB_URL`: defaults to `jdbc:mysql://localhost:3306/hotel_management`
   - `DB_USER`: defaults to `root`
   - `DB_PASSWORD`: defaults to an empty password
5. Compile and run the Java program with Connector/J on the classpath.

### Example commands

Linux/macOS (replace the JAR path with the location of your Connector/J file):

```bash
javac -cp .:mysql-connector-j.jar HotelManagement.java
java -cp .:mysql-connector-j.jar HotelManagement
```

Windows:

```bat
javac -cp .;mysql-connector-j.jar HotelManagement.java
java -cp .;mysql-connector-j.jar HotelManagement
```

If your MySQL user or password differs from the defaults, set `DB_USER` and `DB_PASSWORD` before running.

## Date and cost rules
- Enter dates in `YYYY-MM-DD` format.
- Check-out must be later than check-in.
- A booking includes the check-in date and excludes the check-out date for availability checks.
- Total cost = tariff per night × number of nights.

## Notes
This is a simple educational console application. It uses prepared statements for SQL input values. For a production booking system, additional authentication, payment handling, and stronger concurrent-booking controls would be needed.
