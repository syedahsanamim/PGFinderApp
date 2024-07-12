import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.ParseException;
import java.util.InputMismatchException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;


public class PGFinderApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to PG Finder!");
        boolean exit = false;

        while (!exit) {
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String input = scanner.nextLine();
            int choice=0;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input: " + input + " is not a valid input.");
            }

            switch (choice) {
                case 1:
                    registerUser(scanner);
                    break;
                case 2:
                    loginUser(scanner);
                    break;
                case 3:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
    }

    private static void registerUser(Scanner scanner) {
        while (true) {
            System.out.print("Enter name: ");
            String name = scanner.nextLine();

            // Validate name using regular expression
            if (name.matches("[a-zA-Z]+( [a-zA-Z]+)*")) {
                // Valid name entered
                String email;
                while (true) {
                    System.out.print("Enter email: ");
                    email = scanner.nextLine();
                    if (isValidEmail(email)) {
                        break;
                    } else {
                        System.out.println("Invalid email. Please enter a valid email address.");
                    }
                }

                String password;
                while (true) {
                    System.out.print("Enter password: ");
                    password = scanner.nextLine();
                    if (password.length() >= 4) {
                        break;
                    } else {
                        System.out.println("Password must be 4 characters or more.");
                    }
                }

                String phone;
                while (true) {
                    System.out.print("Enter phone number: ");
                    phone = scanner.nextLine();
                    if (phone.length() == 10 && phone.matches("\\d+")) {
                        break;
                    } else {
                        System.out.println("Phone number must be 10 digits.");
                    }
                }

                String aadhar;
                while (true) {
                    System.out.print("Enter Aadhar number: ");
                    aadhar = scanner.nextLine();
                    if (aadhar.length() == 12 && aadhar.matches("\\d+")) {
                        break;
                    } else {
                        System.out.println("Please enter valid Aadhar number (12 digits).");
                    }
                }

                String userType;
                while (true) {
                    System.out.print("Are you an owner or renter? ");
                    userType = scanner.nextLine();
                    if (userType.equals("renter") || userType.equals("owner")) {
                        break;
                    } else {
                        System.out.println("You can either be an owner or a renter!");
                    }
                }

                try (Connection connection = DBUtil.getConnection()) {
                    String sql = "INSERT INTO User_Table (name, password, email, phone_number, aadhar_number, user_type) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement statement = connection.prepareStatement(sql, new String[]{"user_id"});
                    statement.setString(1, name);
                    statement.setString(2, password);
                    statement.setString(3, email);
                    statement.setString(4, phone);
                    statement.setString(5, aadhar);
                    statement.setString(6, userType);
                    statement.executeUpdate();

                    if (userType.equalsIgnoreCase("owner")) {
                        System.out.print("Enter your address: ");
                        String address = scanner.nextLine();
                        ResultSet rs = statement.getGeneratedKeys();
                        if (rs.next()) {
                            int userId = rs.getInt(1);
                            String ownerSql = "INSERT INTO Owner_Table (owner_id, address) VALUES (?, ?)";
                            PreparedStatement ownerStatement = connection.prepareStatement(ownerSql);
                            ownerStatement.setInt(1, userId);
                            ownerStatement.setString(2, address);
                            ownerStatement.executeUpdate();
                        }
                    }

                    System.out.println("User registered successfully.");
                    break; // Exit registration loop after successful registration
                } catch (SQLException e) {
                    if (e.getErrorCode() == 1) { // ORA-00001: unique constraint violation
                        System.out.println("Aadhar number already exists. Please use a different Aadhar number.");
                    } else {
                        // e.printStackTrace();
                        System.out.println("Registration failed.");
                    }
                }
            } else {
                System.out.println("Invalid name. Name should contain only alphabetic characters with a single space between words.");
            }
        }
    }



    private static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z]+[a-zA-Z0-9]*@[a-zA-Z]+\\.[a-zA-Z]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }



    private static void loginUser(Scanner scanner) {
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try (Connection connection = DBUtil.getConnection()) {
            String sql = "SELECT user_id, user_type FROM User_Table WHERE email = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, email);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String userType = rs.getString("user_type");

                System.out.println("Login successful.");
                if (userType.equalsIgnoreCase("owner")) {
                    ownerMenu(scanner, userId);
                } else if (userType.equalsIgnoreCase("renter")) {
                    renterMenu(scanner, userId);
                }
            } else {
                System.out.println("Invalid email or password.");
            }
        } catch (SQLException e) {
//            e.printStackTrace();
            System.out.println("Login failed.");
        }
    }

    private static void ownerMenu(Scanner scanner, int ownerId) {
        boolean exit = false;

        while (!exit) {
            System.out.println("Owner Menu:");
            System.out.println("1. Add PG Details");
            System.out.println("2. Update PG Details");
            System.out.println("3. Delete PG Details");
            System.out.println("4. View Your PG Listings");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");
            String input = scanner.nextLine();
            int choice=0;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input: " + input + " is not a valid input.");
            }

            switch (choice) {
                case 1:
                    addPGDetails(scanner, ownerId);
                    break;
                case 2:
                    updatePGDetails(scanner, ownerId);
                    break;
                case 3:
                    deletePGDetails(scanner, ownerId);
                    break;
                case 4:
                    viewPGListings(ownerId);
                    break;
                case 5:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void renterMenu(Scanner scanner, int renterId) {
        boolean exit = false;

        while (!exit) {
            System.out.println("Renter Menu:");
            System.out.println("1. Search for PG");
            System.out.println("2. Book PG");
            System.out.println("3. Extend Stay");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");
            String input = scanner.nextLine();
            int choice=0;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input: " + input + " is not a valid input.");
            }

            switch (choice) {
                case 1:
                    searchForPG(scanner);
                    break;
                case 2:
                    bookPG(scanner, renterId);
                    break;
                case 3:
                    extendBooking(scanner, renterId);
                    break;
                case 4:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void addPGDetails(Scanner scanner, int ownerId) {
        System.out.print("Enter PG name: ");
        String pgName = scanner.nextLine();

        String area;
        while (true) {
            System.out.print("Enter area in format Sector XX in Gurgaon: ");
            area = scanner.nextLine();
            if (isValidArea(area)) {
                break;
            } else {
                System.out.println("Please enter area in valid format as mentioned (Sector XX).");
            }
        }

        int availableRooms = -1;
        while (availableRooms < 0) {
            System.out.print("Enter available rooms: ");
            if (scanner.hasNextInt()) {
                availableRooms = scanner.nextInt();
                if (availableRooms < 0) {
                    System.out.println("Number of available rooms must be a non-negative integer.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number for available rooms.");
                scanner.next(); // consume the invalid input
            }
        }

        int rent = -1;
        while (rent < 1000) {
            System.out.print("Enter rent: ");
            if (scanner.hasNextInt()) {
                rent = scanner.nextInt();
                if (rent < 0) {
                    System.out.println("Rent must be more than 1000.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number for rent.");
                scanner.next(); // consume the invalid input
            }
        }
        scanner.nextLine(); // consume newline

        try (Connection connection = DBUtil.getConnection()) {
            String sql = "INSERT INTO Pg_Table (owner_id, pg_name, area, available_rooms, rent) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, ownerId);
            statement.setString(2, pgName);
            statement.setString(3, area);
            statement.setInt(4, availableRooms);
            statement.setInt(5, rent);
            statement.executeUpdate();

            System.out.println("PG details added successfully.");
        } catch (SQLException e) {
//            e.printStackTrace();
            System.out.println("Failed to add PG details.");
        }
    }

    private static boolean isValidArea(String area) {
        // Regex to match the format "Sector XX" where XX is a number from 01 to 99
        String areaRegex = "^Sector (?!00)\\d{1,2}$";
        Pattern pattern = Pattern.compile(areaRegex);
        Matcher matcher = pattern.matcher(area);
        return matcher.matches();
    }



    private static void updatePGDetails(Scanner scanner, int ownerId) {
        System.out.print("Enter PG ID to update: ");
        int pgId = scanner.nextInt();
        scanner.nextLine(); // consume newline
        System.out.print("Enter new PG name: ");
        String pgName = scanner.nextLine();
        System.out.print("Enter new area: ");
        String area = scanner.nextLine();

        int availableRooms = -1;
        while (availableRooms < 0) {
            System.out.print("Enter new available rooms: ");
            try {
                availableRooms = scanner.nextInt();
                if (availableRooms < 0) {
                    System.out.println("Number of available rooms must be a non-negative integer.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number for available rooms.");
                scanner.next(); // consume the invalid input
            }
        }

        int rent = -1;
        while (rent < 0) {
            System.out.print("Enter new rent: ");
            try {
                rent = scanner.nextInt();
                if (rent < 0) {
                    System.out.println("Rent must be a non-negative integer.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number for rent.");
                scanner.next(); // consume the invalid input
            }
        }
        scanner.nextLine(); // consume newline

        try (Connection connection = DBUtil.getConnection()) {
            String sql = "UPDATE Pg_Table SET pg_name = ?, area = ?, available_rooms = ?, rent = ? WHERE pg_id = ? AND owner_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, pgName);
            statement.setString(2, area);
            statement.setInt(3, availableRooms);
            statement.setInt(4, rent);
            statement.setInt(5, pgId);
            statement.setInt(6, ownerId);
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("PG details updated successfully.");
            } else {
                System.out.println("Failed to update PG details or PG not found.");
            }
        } catch (SQLException e) {
//            e.printStackTrace();
            System.out.println("Failed to update PG details.");
        }
    }


    private static void deletePGDetails(Scanner scanner, int ownerId) {
        System.out.print("Enter PG ID to delete: ");
        int pgId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        try (Connection connection = DBUtil.getConnection()) {
            String sql = "DELETE FROM Pg_Table WHERE pg_id = ? AND owner_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, pgId);
            statement.setInt(2, ownerId);
            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("PG details deleted successfully.");
            } else {
                System.out.println("Failed to delete PG details or PG not found.");
            }
        } catch (SQLException e) {
//            e.printStackTrace();
            System.out.println("Failed to delete PG details.");
        }
    }

    private static void viewPGListings(int ownerId) {
        boolean foundListings = false;
        try (Connection connection = DBUtil.getConnection()) {
            String sql = "SELECT * FROM Pg_Table WHERE owner_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, ownerId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                int pgId = rs.getInt("pg_id");
                String pgName = rs.getString("pg_name");
                String area = rs.getString("area");
                int availableRooms = rs.getInt("available_rooms");
                int rent = rs.getInt("rent");
                System.out.println("PG ID: " + pgId + ", Name: " + pgName + ", Area: " + area + ", Available Rooms: " + availableRooms + ", Rent: " + rent);
                foundListings = true;
            }

            if (!foundListings) {
                System.out.println("You have no listed PGs.");
            }
        } catch (SQLException e) {
//        e.printStackTrace();
            System.out.println("Failed to retrieve PG listings.");
        }
    }


    private static void searchForPG(Scanner scanner) {
        String area;
        while (true) {
            System.out.print("Enter area in format Sector XX in Gurgaon: ");
            area = scanner.nextLine();
            if (isValidArea1(area)) {
                break;
            } else {
                System.out.println("Please enter area in valid format as mentioned (Sector XX).");
            }
        }

        int maxRent = -1;
        while (maxRent < 0) {
            System.out.print("Enter maximum rent: ");
            try {
                maxRent = scanner.nextInt();
                if (maxRent < 0) {
                    System.out.println("Maximum rent must be a non-negative number.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number for maximum rent.");
                scanner.next(); // consume the invalid input
            }
        }
        scanner.nextLine(); // consume newline

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        Date today = new Date();
        String todayStr = dateFormat.format(today);

        String startDate = "";
        while (true) {
            System.out.print("Enter start date (YYYY-MM-DD): ");
            startDate = scanner.nextLine();
            try {
                Date start = dateFormat.parse(startDate);
                if (!startDate.equals(todayStr) && start.before(today)) {
                    System.out.println("Start date must be today's date or a date ahead of that.");
                } else {
                    break;
                }
            } catch (ParseException e) {
                System.out.println("Invalid date format. Please enter date in YYYY-MM-DD format.");
            }
        }

        String endDate = "";
        while (true) {
            System.out.print("Enter end date (YYYY-MM-DD): ");
            endDate = scanner.nextLine();
            try {
                Date end = dateFormat.parse(endDate);
                Date start = dateFormat.parse(startDate);
                if (end.before(start)) {
                    System.out.println("End date must be later than the start date.");
                } else {
                    break;
                }
            } catch (ParseException e) {
                System.out.println("Invalid date format. Please enter date in YYYY-MM-DD format.");
            }
        }

        try (Connection connection = DBUtil.getConnection()) {
            String sql = "SELECT * FROM Pg_Table \n" +
                    "WHERE area = ? \n" +
                    "AND rent <= ? \n" +
                    "AND (available_rooms > 0 \n" +
                    "     OR pg_id NOT IN (\n" +
                    "         SELECT pg_id \n" +
                    "         FROM Booking_Table \n" +
                    "         WHERE TO_DATE(start_date, 'YYYY-MM-DD') <= TO_DATE(?, 'YYYY-MM-DD') \n" +
                    "         AND TO_DATE(end_date, 'YYYY-MM-DD') >= TO_DATE(?, 'YYYY-MM-DD')\n" +
                    "     )\n" +
                    ")";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, area);
            statement.setInt(2, maxRent);
            statement.setString(3, endDate);
            statement.setString(4, startDate);
            ResultSet rs = statement.executeQuery();

            boolean found = false;
            while (rs.next()) {
                int pgId = rs.getInt("pg_id");
                String pgName = rs.getString("pg_name");
                int availableRooms = rs.getInt("available_rooms");
                int rent = rs.getInt("rent");
                System.out.println("PG ID: " + pgId + ", Name: " + pgName + ", Area: " + area + ", Available Rooms: " + availableRooms + ", Rent: " + rent);
                found = true;
            }

            if (!found) {
                System.out.println("Sorry, there are no PGs available with such requirements!!");
            }
        } catch (SQLException e) {
            // e.printStackTrace();
            System.out.println("Failed to search for PG.");
        }
    }

    private static boolean isValidArea1(String area) {
        // Regex to match the format "Sector XX" where XX is a number
        String areaRegex = "^Sector \\d+$";
        Pattern pattern = Pattern.compile(areaRegex);
        Matcher matcher = pattern.matcher(area);
        return matcher.matches();
    }

    private static void bookPG(Scanner scanner, int renterId) {
        System.out.print("Enter PG ID to book: ");
        int pgId = scanner.nextInt();
        scanner.nextLine(); // consume newline

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        Date today = new Date();
        String todayStr = dateFormat.format(today);

        String startDate = "";
        while (true) {
            System.out.print("Enter start date (YYYY-MM-DD): ");
            startDate = scanner.nextLine();
            try {
                Date start = dateFormat.parse(startDate);
                if (!startDate.equals(todayStr) && start.before(today)) {
                    System.out.println("Start date must be today's date or a date ahead of today.");
                } else {
                    break;
                }
            } catch (ParseException e) {
                System.out.println("Invalid date format. Please enter date in YYYY-MM-DD format.");
            }
        }

        String endDate = "";
        while (true) {
            System.out.print("Enter end date (YYYY-MM-DD): ");
            endDate = scanner.nextLine();
            try {
                Date end = dateFormat.parse(endDate);
                Date start = dateFormat.parse(startDate);
                if (end.before(start)) {
                    System.out.println("End date must be later than the start date.");
                } else {
                    break;
                }
            } catch (ParseException e) {
                System.out.println("Invalid date format. Please enter date in YYYY-MM-DD format.");
            }
        }

        System.out.print("Enter token amount (3000 or more): ");
        int tokenAmount = scanner.nextInt();
        scanner.nextLine(); // consume newline

        if (tokenAmount < 3000) {
            System.out.println("Token amount must be 3000 or more.");
            return;
        }

        try (Connection connection = DBUtil.getConnection()) {
            // Check availability of rooms
            String checkAvailabilitySql = "SELECT available_rooms FROM Pg_Table WHERE pg_id = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkAvailabilitySql);
            checkStatement.setInt(1, pgId);
            ResultSet rs = checkStatement.executeQuery();
            if (rs.next()) {
                int availableRooms = rs.getInt("available_rooms");
                if (availableRooms <= 0) {
                    System.out.println("No rooms available for the selected PG. Please choose another PG.");
                    return;
                }
            } else {
                System.out.println("PG not found. Please enter a valid PG ID.");
                return;
            }

            // Proceed with booking
            String sql = "INSERT INTO Booking_Table (pg_id, renter_id, start_date, end_date, token_amount, booking_status) VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'), ?, 'confirmed')";
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"booking_id"});
            statement.setInt(1, pgId);
            statement.setInt(2, renterId);
            statement.setString(3, startDate);
            statement.setString(4, endDate);
            statement.setInt(5, tokenAmount);
            statement.executeUpdate();

            // Retrieve the generated booking ID
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int bookingId = generatedKeys.getInt(1);
                System.out.println("Booking successful. Your booking ID is: " + bookingId);
            } else {
                throw new SQLException("Creating booking failed, no ID obtained.");
            }

            // Update available rooms in Pg_Table
            String updateSql = "UPDATE Pg_Table SET available_rooms = available_rooms - 1 WHERE pg_id = ? AND available_rooms > 0";
            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                updateStatement.setInt(1, pgId);
                int rowsAffected = updateStatement.executeUpdate();
                if (rowsAffected == 0) {
                    // If no rows were updated, it means there were no available rooms
                    connection.rollback();
                    System.out.println("No available rooms left to book.");
                    return;
                }
            }

            System.out.println("PG booked successfully.");
        } catch (SQLException e) {
//            e.printStackTrace();
            System.out.println("Failed to book PG.");
        }
    }




    private static void extendBooking(Scanner scanner, int renterId) {
        System.out.print("Enter Booking ID to extend: ");
        int bookingId = scanner.nextInt();
        scanner.nextLine(); // consume newline
        System.out.print("Enter new end date (YYYY-MM-DD): ");
        String newEndDateStr = scanner.nextLine();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);

        try (Connection connection = DBUtil.getConnection()) {
            // Check if the renter has any active bookings
            String checkBookingSql = "SELECT * FROM Booking_Table WHERE booking_id = ? AND renter_id = ?";
            PreparedStatement checkBookingStatement = connection.prepareStatement(checkBookingSql);
            checkBookingStatement.setInt(1, bookingId);
            checkBookingStatement.setInt(2, renterId);
            ResultSet bookingResult = checkBookingStatement.executeQuery();

            if (!bookingResult.next()) {
                System.out.println("You don't have any active bookings.");
                return;
            }

            // Fetch the current end date of the booking
            String fetchEndDateSql = "SELECT end_date FROM Booking_Table WHERE booking_id = ? AND renter_id = ?";
            PreparedStatement fetchEndDateStatement = connection.prepareStatement(fetchEndDateSql);
            fetchEndDateStatement.setInt(1, bookingId);
            fetchEndDateStatement.setInt(2, renterId);
            ResultSet rs = fetchEndDateStatement.executeQuery();

            if (rs.next()) {
                Date currentEndDate = rs.getDate("end_date");
                Date newEndDate = dateFormat.parse(newEndDateStr);

                // Check if the new end date is at least one month after the current end date
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentEndDate);
                calendar.add(Calendar.MONTH, 1); // Add one month to the current end date
                Date minNewEndDate = calendar.getTime();

                if (newEndDate.before(minNewEndDate)) {
                    System.out.println("At least increase 1 month of stay !!");
                    return;
                }

                // Update the booking with the new end date
                String sql = "UPDATE Booking_Table SET end_date = TO_DATE(?, 'YYYY-MM-DD') WHERE booking_id = ? AND renter_id = ?";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, newEndDateStr);
                statement.setInt(2, bookingId);
                statement.setInt(3, renterId);
                int rowsUpdated = statement.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("Booking extended successfully.");
                } else {
                    System.out.println("Failed to extend booking or booking not found.");
                }
            } else {
                System.out.println("Booking not found.");
            }
        } catch (SQLException | ParseException e) {
//        e.printStackTrace();
            System.out.println("Failed to extend booking.");
        }
    }


}

