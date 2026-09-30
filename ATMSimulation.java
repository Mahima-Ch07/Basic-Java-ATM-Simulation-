import java.util.Scanner;

public class ATMSimulation {

    // Simple in-memory "account" data
    static double balance = 5000.00;
    static String correctPin = "1234";

    // Transaction history stored using an array
    static String[] transactionHistory = new String[50];
    static int transactionCount = 0;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("       WELCOME TO JAVA BANK ATM");
        System.out.println("========================================");

        // PIN verification with limited attempts
        int attempts = 3;
        boolean authenticated = false;

        while (attempts > 0) {
            System.out.print("Enter your 4-digit PIN: ");
            String enteredPin = sc.next();

            if (enteredPin.equals(correctPin)) {
                authenticated = true;
                break;
            } else {
                attempts--;
                System.out.println("Incorrect PIN. Attempts left: " + attempts);
            }
        }

        if (!authenticated) {
            System.out.println("Too many incorrect attempts. Card blocked.");
            sc.close();
            return;
        }

        System.out.println("\nLogin successful!\n");

        int choice;
        do {
            printMenu();
            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    checkBalance();
                    break;
                case 2:
                    depositMoney(sc);
                    break;
                case 3:
                    withdrawMoney(sc);
                    break;
                case 4:
                    printTransactionHistory();
                    break;
                case 5:
                    System.out.println("Thank you for using Java Bank ATM. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please select 1-5.");
            }

        } while (choice != 5);

        sc.close();
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

    static void checkBalance() {
        System.out.println("Your current balance is: Rs. " + balance);
    }

    static void depositMoney(Scanner sc) {
        System.out.print("Enter amount to deposit: Rs. ");
        double amount = sc.nextDouble();

        if (amount <= 0) {
            System.out.println("Invalid amount. Deposit must be positive.");
            return;
        }

        balance += amount;
        addTransaction("Deposited Rs. " + amount);
        System.out.println("Deposit successful! New balance: Rs. " + balance);
    }

    static void withdrawMoney(Scanner sc) {
        System.out.print("Enter amount to withdraw: Rs. ");
        double amount = sc.nextDouble();

        if (amount <= 0) {
            System.out.println("Invalid amount. Withdrawal must be positive.");
            return;
        }

        if (amount > balance) {
            System.out.println("Insufficient balance. Your balance is Rs. " + balance);
            return;
        }

        // Simulate ATM only dispensing multiples of 100
        if (amount % 100 != 0) {
            System.out.println("Please enter an amount in multiples of 100.");
            return;
        }

        balance -= amount;
        addTransaction("Withdrew Rs. " + amount);
        System.out.println("Withdrawal successful! New balance: Rs. " + balance);
    }

    static void addTransaction(String record) {
        if (transactionCount < transactionHistory.length) {
            transactionHistory[transactionCount] = record;
            transactionCount++;
        }
    }

    static void printTransactionHistory() {
        System.out.println("---------- Transaction History ----------");
        if (transactionCount == 0) {
            System.out.println("No transactions yet.");
        } else {
            for (int i = 0; i < transactionCount; i++) {
                System.out.println((i + 1) + ". " + transactionHistory[i]);
            }
        }
        System.out.println("------------------------------------------");
    }
}
