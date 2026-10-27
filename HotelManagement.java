import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class HotelManagement {
    static final String URL = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/hotel_management");
    static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    static final String PASS = System.getenv().getOrDefault("DB_PASSWORD", "");

    public static void main(String[] args) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             Scanner sc = new Scanner(System.in)) {
            System.out.println("Connected to Hotel Management database.");
            while (true) {
                System.out.println("\n1. View rooms\n2. Check availability\n3. Book room\n4. View bookings\n5. Exit");
                System.out.print("Choose: ");
                String choice = sc.nextLine().trim();
                if (choice.equals("1")) viewRooms(con);
                else if (choice.equals("2")) availability(con, sc);
                else if (choice.equals("3")) bookRoom(con, sc);
                else if (choice.equals("4")) viewBookings(con);
                else if (choice.equals("5")) { System.out.println("Goodbye!"); break; }
                else System.out.println("Choose a number from 1 to 5.");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            System.out.println("Run hotelmanagement.sql and check MySQL credentials and Connector/J.");
        }
    }

    static void viewRooms(Connection con) throws SQLException {
        String sql = "SELECT room_number, room_type, tariff_per_night FROM rooms ORDER BY room_number";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            System.out.println("\nRoom Number | Type | Tariff per night");
            while (rs.next())
                System.out.printf("%d | %s | Rs. %.2f%n", rs.getInt(1), rs.getString(2), rs.getBigDecimal(3));
        }
    }

    static void availability(Connection con, Scanner sc) throws SQLException {
        LocalDate in = date(sc, "Check-in (YYYY-MM-DD): ");
        LocalDate out = date(sc, "Check-out (YYYY-MM-DD): ");
        validate(in, out);
        String sql = "SELECT r.room_number, r.room_type, r.tariff_per_night FROM rooms r " +
            "WHERE NOT EXISTS (SELECT 1 FROM bookings b WHERE b.room_number=r.room_number " +
            "AND b.check_in < ? AND b.check_out > ?) ORDER BY r.room_number";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(out));
            ps.setDate(2, Date.valueOf(in));
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("Room %d | %s | Rs. %.2f per night%n",
                        rs.getInt(1), rs.getString(2), rs.getBigDecimal(3));
                }
                if (!found) System.out.println("No rooms available for these dates.");
            }
        }
    }

    static void bookRoom(Connection con, Scanner sc) throws SQLException {
        System.out.print("Customer name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Name cannot be empty."); return; }
        System.out.print("Room number: ");
        int room;
        try { room = Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("Invalid room number."); return; }
        LocalDate in = date(sc, "Check-in (YYYY-MM-DD): ");
        LocalDate out = date(sc, "Check-out (YYYY-MM-DD): ");
        validate(in, out);
        long nights = ChronoUnit.DAYS.between(in, out);

        String find = "SELECT r.tariff_per_night FROM rooms r WHERE r.room_number=? AND NOT EXISTS " +
            "(SELECT 1 FROM bookings b WHERE b.room_number=r.room_number AND b.check_in < ? AND b.check_out > ?)";
        try (PreparedStatement ps = con.prepareStatement(find)) {
            ps.setInt(1, room); ps.setDate(2, Date.valueOf(out)); ps.setDate(3, Date.valueOf(in));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) { System.out.println("Room does not exist or is already booked for those dates."); return; }
                java.math.BigDecimal tariff = rs.getBigDecimal(1);
                java.math.BigDecimal total = tariff.multiply(java.math.BigDecimal.valueOf(nights));
                String insert = "INSERT INTO bookings(room_number, customer_name, check_in, check_out, total_cost) VALUES(?,?,?,?,?)";
                try (PreparedStatement add = con.prepareStatement(insert)) {
                    add.setInt(1, room); add.setString(2, name);
                    add.setDate(3, Date.valueOf(in)); add.setDate(4, Date.valueOf(out));
                    add.setBigDecimal(5, total); add.executeUpdate();
                }
                System.out.println("Booking successful!");
                System.out.println("Nights: " + nights + " | Total cost: Rs. " + total);
            }
        }
    }

    static void viewBookings(Connection con) throws SQLException {
        String sql = "SELECT booking_id, customer_name, room_number, check_in, check_out, total_cost FROM bookings ORDER BY booking_id";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("Booking %d | %s | Room %d | %s to %s | Rs. %.2f%n",
                    rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getDate(4), rs.getDate(5), rs.getBigDecimal(6));
            }
            if (!found) System.out.println("No bookings yet.");
        }
    }

    static LocalDate date(Scanner sc, String prompt) {
        System.out.print(prompt);
        try { return LocalDate.parse(sc.nextLine().trim()); }
        catch (Exception e) { throw new IllegalArgumentException("Use YYYY-MM-DD date format."); }
    }

    static void validate(LocalDate in, LocalDate out) {
        if (!out.isAfter(in)) throw new IllegalArgumentException("Check-out must be after check-in.");
    }
}
