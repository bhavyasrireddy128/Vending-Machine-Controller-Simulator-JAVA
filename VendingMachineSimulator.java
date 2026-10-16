
import java.util.*;
import java.nio.file.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class VendingMachineSimulator {

    private static final Scanner sc = new Scanner(System.in);
    private static final String FILE_NAME = "transactions.txt";

    // Module 4: Interface
    interface Sellable {
        boolean purchase(int quantity);
    }

    // Module 4: Abstract class and inheritance
    static abstract class Product implements Sellable {
        private final int id;
        private final String name;
        private final double price;
        private int stock;

        public Product(int id, String name, double price, int stock) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
        }

        // Accessors
        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public int getStock() {
            return stock;
        }

        // Module 4: Encapsulation and mutator
        public void restock(int quantity) {
            if (quantity > 0) {
                stock += quantity;
            }
        }

        @Override
        public boolean purchase(int quantity) {
            if (quantity <= 0 || quantity > stock) {
                return false;
            }
            stock -= quantity;
            return true;
        }

        public abstract String getCategory();

        @Override
        public String toString() {
            return String.format(
                "%-5d %-18s Rs.%-9.2f %-12s %d",
                id, name, price, getCategory(), stock
            );
        }
    }

    // Module 4: Inheritance and method overriding
    static class Snack extends Product {
        public Snack(int id, String name, double price, int stock) {
            super(id, name, price, stock);
        }

        @Override
        public String getCategory() {
            return "Snack";
        }
    }

    static class Drink extends Product {
        public Drink(int id, String name, double price, int stock) {
            super(id, name, price, stock);
        }

        @Override
        public String getCategory() {
            return "Drink";
        }
    }

    // Module 4: Transaction object
    static class Transaction {
        private final String details;
        private final double amount;

        public Transaction(String details, double amount) {
            this.details = details;
            this.amount = amount;
        }

        public double getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return details + " | Total: Rs."
                    + String.format("%.2f", amount);
        }
    }

    // Module 3: Arrays and object creation
    private static final Product[] products = {
        new Snack(1, "Chips", 20.00, 10),
        new Snack(2, "Chocolate", 30.00, 8),
        new Snack(3, "Biscuits", 15.00, 12),
        new Drink(4, "Water", 10.00, 15),
        new Drink(5, "Juice", 25.00, 7),
        new Drink(6, "Cold Coffee", 40.00, 5)
    };

    // Module 6: Collections
    private static final List<Transaction> history =
            new ArrayList<>();

    private static final Map<Integer, Integer> salesCount =
            new HashMap<>();

    // Module 1: Formatted console output
    public static void displayProducts() {
        System.out.println("\n========== VENDING MACHINE ==========");
        System.out.printf("%-5s %-18s %-12s %-12s %s%n",
                "ID", "Product", "Price", "Category", "Stock");
        System.out.println("-----------------------------------------------");

        for (Product p : products) {
            System.out.println(p);
        }
    }

    // Module 3: Searching an array
    public static Product findProduct(int id) {
        for (Product p : products) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    // Module 3: Method overloading
    public static double calculateTotal(Product p, int quantity) {
        return p.getPrice() * quantity;
    }

    public static double calculateTotal(
            Product p, int quantity, double discountRate) {
        return calculateTotal(p, quantity)
                * (1 - discountRate);
    }

    // Module 5: String validation
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    // Module 5: File I/O using NIO
    public static void saveTransaction(Transaction t) {
        try {
            String line = LocalDateTime.now()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    + " | " + t;

            Files.write(
                Path.of(FILE_NAME),
                Collections.singletonList(line),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.out.println("Could not save transaction: "
                    + e.getMessage());
        }
    }

    // Module 5: Read file line by line
    public static void showSavedTransactions() {
        Path path = Path.of(FILE_NAME);

        if (!Files.exists(path)) {
            System.out.println("No saved transaction file yet.");
            return;
        }

        try (var reader = Files.newBufferedReader(path)) {
            String line;
            System.out.println("\n--- Saved Transactions ---");

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        }
    }

    // Module 1: Type conversion and input validation
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            try {
                double value = Double.parseDouble(input);

                if (Double.isFinite(value) && value >= 0) {
                    return value;
                }

                System.out.println("Enter a finite, non-negative amount.");
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid amount.");
            }
        }
    }

    // Purchase controller
    public static void purchaseProduct() {
        displayProducts();

        int id = readInt("Enter product ID: ");
        Product p = findProduct(id);

        if (p == null) {
            System.out.println("Invalid product ID.");
            return;
        }

        if (p.getStock() == 0) {
            System.out.println("Product is out of stock.");
            return;
        }

        int quantity = readInt("Enter quantity: ");

        if (quantity <= 0 || quantity > p.getStock()) {
            System.out.println("Invalid quantity or insufficient stock.");
            return;
        }

        double total = calculateTotal(p, quantity);

        System.out.printf("Total amount: Rs.%.2f%n", total);
        double paid = readDouble("Insert money (Rs.): ");

        if (paid < total) {
            System.out.println("Insufficient money. Transaction cancelled.");
            System.out.printf("Refund: Rs.%.2f%n", paid);
            return;
        }

        // Module 1: Arithmetic and type casting
        double change = paid - total;

        if (p.purchase(quantity)) {
            String details = p.getName() + " x " + quantity;
            Transaction t = new Transaction(details, total);

            history.add(t);
            salesCount.merge(p.getId(), quantity, Integer::sum);
            saveTransaction(t);

            System.out.println("\nPurchase successful!");
            System.out.println("Dispensing: " + p.getName());
            System.out.printf("Change: Rs.%.2f%n", change);
        } else {
            System.out.println("Purchase failed. Please try again.");
        }
    }

    // Module 6: Predicate, lambda and streams
    public static void searchProducts() {
        System.out.print("Enter product name to search: ");
        String query = sc.nextLine().trim().toLowerCase();

        Predicate<Product> matches =
                p -> p.getName().toLowerCase().contains(query);

        List<Product> results = Arrays.stream(products)
                .filter(matches)
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            System.out.println("No matching products found.");
        } else {
            results.forEach(System.out::println);
        }
    }

    // Module 6: Stream operations and report generation
    public static void showSalesReport() {
        double revenue = history.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();

        long count = history.size();

        System.out.println("\n========== SALES REPORT ==========");
        System.out.println("Completed transactions: " + count);
        System.out.printf("Total sales: Rs.%.2f%n", revenue);

        System.out.println("\nUnits sold by product:");
        salesCount.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    Product p = findProduct(entry.getKey());
                    if (p != null) {
                        System.out.println(p.getName()
                                + ": " + entry.getValue());
                    }
                });
    }

    // Module 3: Array summation and average
    public static void showInventorySummary() {
        int totalStock = 0;
        double totalValue = 0;

        for (Product p : products) {
            totalStock += p.getStock();
            totalValue += p.getPrice() * p.getStock();
        }

        double averageStock = (double) totalStock / products.length;

        System.out.println("\n--- Inventory Summary ---");
        System.out.println("Product types: " + products.length);
        System.out.println("Total units remaining: " + totalStock);
        System.out.printf("Average stock per product: %.2f%n",
                averageStock);
        System.out.printf("Value of remaining stock: Rs.%.2f%n",
                totalValue);
    }

    public static void main(String[] args) {
        int choice;

        System.out.println("Welcome to the Vending Machine Simulator!");

        // Module 2: do-while, switch and break
        do {
            System.out.println("\n1. View Products");
            System.out.println("2. Purchase Product");
            System.out.println("3. Search Product");
            System.out.println("4. View Inventory Summary");
            System.out.println("5. View Sales Report");
            System.out.println("6. View Saved Transactions");
            System.out.println("7. Exit");

            choice = readInt("Choose an option: ");

            switch (choice) {
                case 1:
                    displayProducts();
                    break;
                case 2:
                    purchaseProduct();
                    break;
                case 3:
                    searchProducts();
                    break;
                case 4:
                    showInventorySummary();
                    break;
                case 5:
                    showSalesReport();
                    break;
                case 6:
                    showSavedTransactions();
                    break;
                case 7:
                    System.out.println("Thank you for using the machine!");
                    break;
                default:
                    System.out.println("Invalid choice. Select 1-7.");
            }
        } while (choice != 7);

        sc.close();
    }
}
