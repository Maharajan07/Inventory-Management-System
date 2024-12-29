import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class InventoryManagement {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/inventory_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Rajan@db";  

    private static Scanner scanner = new Scanner(System.in);
    private static String loggedInUserRole = ""; // Track the logged-in user's role

    public static void main(String[] args) {
        InventoryManagement system = new InventoryManagement();
        if (system.login()) {
            system.run();
        } else {
            System.out.println("Invalid login attempt.");
        }
    }

    // Login method to authenticate the user
    private boolean login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        String query = "SELECT r.role_name FROM users u INNER JOIN roles r ON u.role_id = r.id WHERE u.username = ? AND u.password = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                loggedInUserRole = rs.getString("role_name");
                System.out.println("Login successful! Welcome, " + username);
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Run the system
    public void run() {
        while (true) {
            System.out.println("\nInventory Management System");
            if (loggedInUserRole.equals("Admin")) {
                System.out.println("1. Add Item");
                System.out.println("2. View Items");
                System.out.println("3. Update Item Quantity");
                System.out.println("4. Delete Item");
                System.out.println("5. Generate CSV");
            } else if (loggedInUserRole.equals("Manager")) {
                System.out.println("1. Add Item");
                System.out.println("2. View Items");
                System.out.println("5. Generate CSV");
            } else if (loggedInUserRole.equals("Viewer")) {
                System.out.println("1. View Items");
            }

            System.out.println("6. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    if (loggedInUserRole.equals("Admin") || loggedInUserRole.equals("Manager")) {
                        addItem();
                    } else if (loggedInUserRole.equals("Viewer")) {
                        viewItems();
                    }
                    break;
                case 2:
                    if (loggedInUserRole.equals("Admin") || loggedInUserRole.equals("Manager")) {
                        viewItems();
                    }
                    break;
                case 3:
                    if (loggedInUserRole.equals("Admin")) {
                        updateItemQuantity();
                    }
                    break;
                case 4:
                    if (loggedInUserRole.equals("Admin")) {
                        deleteItem();
                    }
                    break;
                case 5:
                    generateCSV();
                    break;
                case 6:
                    System.out.println("Exiting... Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Add Item
    private void addItem() {
        System.out.print("Enter item name: ");
        String name = scanner.nextLine();
        System.out.print("Enter item quantity: ");
        int quantity = scanner.nextInt();
        System.out.print("Enter item price: ");
        double price = scanner.nextDouble();

        String query = "INSERT INTO items (name, quantity, price) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setInt(2, quantity);
            stmt.setDouble(3, price);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Item added successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // View Items
    private void viewItems() {
        String query = "SELECT * FROM items";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("ID | Name | Quantity | Price");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");
                System.out.println(id + " | " + name + " | " + quantity + " | " + price);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update Item Quantity
    private void updateItemQuantity() {
        System.out.print("Enter item ID to update quantity: ");
        int itemId = scanner.nextInt();
        System.out.print("Enter new quantity: ");
        int newQuantity = scanner.nextInt();

        String query = "UPDATE items SET quantity = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, newQuantity);
            stmt.setInt(2, itemId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Item quantity updated successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete Item
    private void deleteItem() {
        System.out.print("Enter item ID to delete: ");
        int itemId = scanner.nextInt();

        String query = "DELETE FROM items WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, itemId);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Item deleted successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Generate CSV for inventory
    private void generateCSV() {
        String query = "SELECT * FROM items";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             FileWriter csvWriter = new FileWriter("inventory.csv")) {

            // Write CSV header
            csvWriter.append("ID, Name, Quantity, Price\n");

            // Write data rows
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");

                csvWriter.append(id + "," + name + "," + quantity + "," + price + "\n");
            }
            System.out.println("CSV file generated successfully!");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
