import java.sql.*;
import java.util.Scanner;

public class ATMSimulationDB {

    static final String DB_URL = "jdbc:sqlite:atm.db";
    static String loggedInAccount = null;

    public static void main(String[] args)throws Exception {
	Class.forName("org.sqlite.JDBC");
        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            setupDatabase(conn);

            Scanner sc = new Scanner(System.in);
            System.out.println("========================================");
            System.out.println("     WELCOME TO JAVA BANK ATM (DB)");
            System.out.println("========================================");

            System.out.print("Enter account number: ");
            String accNum = sc.next();

            int attempts = 3;
            boolean authenticated = false;

            while (attempts > 0) {
                System.out.print("Enter your 4-digit PIN: ");
                String enteredPin = sc.next();

                if (checkPin(conn, accNum, enteredPin)) {
                    authenticated = true;
                    loggedInAccount = accNum;
                    break;
                } else {
                    attempts--;
                    System.out.println("Incorrect PIN or account. Attempts left: " + attempts);
                }
            }

            if (!authenticated) {
                System.out.println("Too many incorrect attempts. Card blocked.");
                return;
            }

            System.out.println("\nLogin successful!\n");

            int choice;
            do {
                printMenu();
                choice = sc.nextInt();

                switch (choice) {
                    case 1:
                        checkBalance(conn);
                        break;
                    case 2:
                        depositMoney(conn, sc);
                        break;
                    case 3:
                        withdrawMoney(conn, sc);
                        break;
                    case 4:
                        printTransactionHistory(conn);
                        break;
                    case 5:
                        System.out.println("Thank you for using Java Bank ATM. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please select 1-5.");
                }

            } while (choice != 5);

            sc.close();

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Creates tables and a default account if they don't already exist
    static void setupDatabase(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        stmt.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                "account_number TEXT PRIMARY KEY, " +
                "pin TEXT NOT NULL, " +
                "balance REAL NOT NULL)");

        stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "account_number TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP)");

        // Insert a default demo account only if the table is empty
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM accounts");
        if (rs.next() && rs.getInt("count") == 0) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO accounts (account_number, pin, balance) VALUES (?, ?, ?)");
            ps.setString(1, "1001");
            ps.setString(2, "1234");
            ps.setDouble(3, 5000.00);
            ps.executeUpdate();
            System.out.println("(First run: created a demo account -> 1001 / PIN 1234)");
        }
    }

    static boolean checkPin(Connection conn, String accNum, String pin) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM accounts WHERE account_number = ? AND pin = ?");
        ps.setString(1, accNum);
        ps.setString(2, pin);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    static void printMenu() {
        System.out.println("----------------------------------------");
        System.out.println("1. Check Balance");
        System.out.println("2. Deposit Money");
        System.out.println("3. Withdraw Money");
        System.out.println("4. Transaction History");
        System.out.println("5. Exit");
        System.out.println("----------------------------------------");
        System.out.print("Enter your choice: ");
    }

    static void checkBalance(Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT balance FROM accounts WHERE account_number = ?");
        ps.setString(1, loggedInAccount);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            System.out.println("Your current balance is: Rs. " + rs.getDouble("balance"));
        }
    }

    static void depositMoney(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter amount to deposit: Rs. ");
        double amount = sc.nextDouble();

        if (amount <= 0) {
            System.out.println("Invalid amount. Deposit must be positive.");
            return;
        }

        PreparedStatement update = conn.prepareStatement(
                "UPDATE accounts SET balance = balance + ? WHERE account_number = ?");
        update.setDouble(1, amount);
        update.setString(2, loggedInAccount);
        update.executeUpdate();

        logTransaction(conn, "Deposited Rs. " + amount);
        System.out.println("Deposit successful!");
        checkBalance(conn);
    }

    static void withdrawMoney(Connection conn, Scanner sc) throws SQLException {
        System.out.print("Enter amount to withdraw: Rs. ");
        double amount = sc.nextDouble();

        if (amount <= 0) {
            System.out.println("Invalid amount. Withdrawal must be positive.");
            return;
        }

        if (amount % 100 != 0) {
            System.out.println("Please enter an amount in multiples of 100.");
            return;
        }

        double currentBalance = getBalance(conn);
        if (amount > currentBalance) {
            System.out.println("Insufficient balance. Your balance is Rs. " + currentBalance);
            return;
        }

        PreparedStatement update = conn.prepareStatement(
                "UPDATE accounts SET balance = balance - ? WHERE account_number = ?");
        update.setDouble(1, amount);
        update.setString(2, loggedInAccount);
        update.executeUpdate();

        logTransaction(conn, "Withdrew Rs. " + amount);
        System.out.println("Withdrawal successful!");
        checkBalance(conn);
    }

    static double getBalance(Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT balance FROM accounts WHERE account_number = ?");
        ps.setString(1, loggedInAccount);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getDouble("balance") : 0;
    }

    static void logTransaction(Connection conn, String description) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO transactions (account_number, description) VALUES (?, ?)");
        ps.setString(1, loggedInAccount);
        ps.setString(2, description);
        ps.executeUpdate();
    }

    static void printTransactionHistory(Connection conn) throws SQLException {
        System.out.println("---------- Transaction History ----------");
        PreparedStatement ps = conn.prepareStatement(
                "SELECT description, created_at FROM transactions WHERE account_number = ? ORDER BY id DESC");
        ps.setString(1, loggedInAccount);
        ResultSet rs = ps.executeQuery();

        boolean any = false;
        while (rs.next()) {
            any = true;
            System.out.println(rs.getString("created_at") + " - " + rs.getString("description"));
        }
        if (!any) {
            System.out.println("No transactions yet.");
        }
        System.out.println("------------------------------------------");
    }
}
