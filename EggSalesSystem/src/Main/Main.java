package Main;

import config.dbConnect;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class Main {

    private static String loggedInUserType = null;
    

    public static void viewCustomers(dbConnect con) {
        String Query = "SELECT * FROM tbl_customer";
        String[] custHeaders = {"ID", "Name", "Phone", "Address"};
        String[] custColumns = {"customer_id", "name", "phone", "address"};
        con.viewRecords(Query, custHeaders, custColumns);
    }

    public static void viewProducts(dbConnect con) {
        String Query = "SELECT * FROM tbl_product";
        String[] prodHeaders = {"Product ID", "Name", "Price", "Stock"};
        String[] prodColumns = {"product_id", "name", "unit_price", "stock_quantity"};
        con.viewRecords(Query, prodHeaders, prodColumns);
    }
    
    public static void viewSales(dbConnect con) {
        String Query = "SELECT s.sale_id, c.name AS customer_name, s.sale_date, p.name AS product_name, si.quantity_sold, si.subtotal "
                      + "FROM tbl_sale s "
                      + "JOIN tbl_customer c ON s.customer_id = c.customer_id "
                      + "JOIN tbl_sale_item si ON s.sale_id = si.sale_id "
                      + "JOIN tbl_product p ON si.product_id = p.product_id "
                      + "ORDER BY s.sale_id DESC, si.sale_item_id ASC";

        String[] salesHeaders = {"Sale ID", "Customer Name", "Date", "Product", "Qty Sold", "Subtotal"};
        String[] salesColumns = {"sale_id", "customer_name", "sale_date", "product_name", "quantity_sold", "subtotal"};
        con.viewRecords(Query, salesHeaders, salesColumns);
    }

    
    public static void viewUsers(dbConnect con) {
        String Query = "SELECT u_id, u_name, u_email, u_type, u_status FROM tbl_user";
        String[] userHeaders = {"User ID", "Name", "Email", "Type", "Status"};
        String[] userColumns = {"u_id", "u_name", "u_email", "u_type", "u_status"};
        con.viewRecords(Query, userHeaders, userColumns);
    }
    
    
    public static boolean login(Scanner sc, dbConnect con) {
        System.out.println("\n===== SYSTEM LOGIN =====");
        System.out.print("Enter Email/Username: ");
        String email = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        String loginSql = "SELECT u_type, u_status FROM tbl_user WHERE u_email = ? AND u_pass = ?";
        
        List<Map<String, Object>> result = con.fetchRecords(loginSql, email, password);
        
        if (result != null && !result.isEmpty()) {
            Map<String, Object> user = result.get(0);
            String status = user.get("u_status").toString();
            
            if ("Approved".equalsIgnoreCase(status) || "Active".equalsIgnoreCase(status)) {
                loggedInUserType = user.get("u_type").toString();
                System.out.println("\nLogin Successful! Welcome, " + loggedInUserType + ".");
                return true;
            } else if ("Pending".equalsIgnoreCase(status)) {
                System.out.println("Login Failed: Your account is still pending approval.");
                return false;
            } else {
                System.out.println("Login Failed: Your account is inactive.");
                return false;
            }
        } else {
            System.out.println("Login Failed: Invalid credentials.");
            return false;
        }
    }
    
    public static void register(Scanner sc, dbConnect con) {
        System.out.println("\n===== NEW USER REGISTRATION =====");
        
        System.out.print("Enter Full Name: ");
        String name = sc.nextLine();
        
        String email = "";
        boolean emailExists = true;
        
        do {
            System.out.print("Enter Email/Username (must be unique): ");
            email = sc.nextLine();
            
            String checkSql = "SELECT u_email FROM tbl_user WHERE u_email = ?";
            List<Map<String, Object>> existingUser = con.fetchRecords(checkSql, email);
            
            if (existingUser == null || existingUser.isEmpty()) {
                emailExists = false;
            } else {
                System.out.println("Email already registered. Please use a different email.");
            }
        } while (emailExists);

        
        System.out.println("Choose User Role:");
        System.out.println("1. Staff (Sales/Customer Management)");
        System.out.println("2. Admin (Full Access)");
        System.out.print("Enter user Type (1 or 2): ");
        int typeChoice = 0;
        
        
        while (true) {
            if (sc.hasNextInt()) {
                typeChoice = sc.nextInt();
                sc.nextLine();
                if (typeChoice == 1 || typeChoice == 2) {
                    break;
                }
            } else {
                sc.next(); 
            }
            System.out.print("Invalid choice, choose between 1 & 2 only: ");
        }
        
        String userType = (typeChoice == 1) ? "Staff" : "Admin";
        
        System.out.print("Enter Password: ");
        String password = sc.nextLine();
        
        
        String sql = "INSERT INTO tbl_user(u_name, u_email, u_type, u_status, u_pass) VALUES (?, ?, ?, ?, ?)";
        
        con.addRecord(sql, name, email, userType, "Pending", password); 
        
        System.out.println("\nRegistration successful. Your account is **Pending Approval** from an administrator.");
    }

    
    public static void manageUserApproval(Scanner sc, dbConnect con) {
        System.out.println("\n--- USER ACCOUNT MANAGEMENT (Admin Only) ---");
        
        
        System.out.println("--- Current System Users ---");
        viewUsers(con);
        
        System.out.println("\n1. Approve/Activate a User");
        System.out.println("2. Deactivate a User");
        System.out.println("3. Delete a User (Permanent)"); 
        System.out.println("4. Back to Main Menu"); 
        System.out.print("Enter choice: ");
        
        int choice = 0;
        if (sc.hasNextInt()) {
            choice = sc.nextInt();
        } else {
            sc.next(); 
            System.out.println("Invalid input. Returning to menu.");
            return;
        }
        sc.nextLine();

        if (choice == 1 || choice == 2) {
            System.out.print("Enter the User ID (u_id) to modify: ");
            int userId = 0;
            if (sc.hasNextInt()) {
                 userId = sc.nextInt();
            } else {
                sc.next();
                System.out.println("Invalid User ID input. Aborting.");
                return;
            }
            sc.nextLine();
            
            String newStatus = (choice == 1) ? "Active" : "Inactive";
            
            
            String updateSql = "UPDATE tbl_user SET u_status = ? WHERE u_id = ?";
            con.updateRecord(updateSql, newStatus, userId);
            
            System.out.printf("User ID %d status successfully changed to %s.%n", userId, newStatus);
            viewUsers(con); 
        } else if (choice == 3) { 
            deleteUser(sc, con);
        } else if (choice == 4) {
            System.out.println("Returning to main menu.");
        } else {
            System.out.println("Invalid choice.");
        }
    }

    // --- NEW DELETE METHODS ---
    
    // Deletes a specific Sale and its related Sale Items (essential for data integrity)
    public static void deleteSale(Scanner sc, dbConnect con) {
        viewSales(con);
        System.out.println("\n--- DELETE SALE (Admin Only) ---");
        System.out.print("Enter the Sale ID (sale_id) to DELETE: ");
        int saleId = 0;
        
        if (sc.hasNextInt()) {
             saleId = sc.nextInt();
        } else {
            sc.next();
            System.out.println("Invalid Sale ID input. Aborting delete.");
            return;
        }
        sc.nextLine();
        
        System.out.printf("WARNING: Deleting Sale ID %d will also delete all associated Sale Items. Are you sure? (Y/N): ", saleId);
        String confirmation = sc.nextLine();
        
        if (confirmation.equalsIgnoreCase("Y")) {
            
            
            
            String deleteItemsSql = "DELETE FROM tbl_sale_item WHERE sale_id = ?";
            con.updateRecord(deleteItemsSql, saleId); 
            
            
            String deleteSaleSql = "DELETE FROM tbl_sale WHERE sale_id = ?";
            con.updateRecord(deleteSaleSql, saleId); 
            
            System.out.printf("Sale ID %d and its items successfully DELETED.%n", saleId);
        } else {
            System.out.println("Sale deletion cancelled.");
        }
    }
    
   
    public static void deleteCustomer(Scanner sc, dbConnect con) {
        viewCustomers(con);
        System.out.println("\n--- DELETE CUSTOMER ---");
        System.out.print("Enter the Customer ID (customer_id) to DELETE: ");
        int customerId = 0;
        
        if (sc.hasNextInt()) {
             customerId = sc.nextInt();
        } else {
            sc.next();
            System.out.println("Invalid Customer ID input. Aborting delete.");
            return;
        }
        sc.nextLine();
        
        
        String checkSalesSql = "SELECT COUNT(*) AS sale_count FROM tbl_sale WHERE customer_id = ?";
        List<Map<String, Object>> salesResult = con.fetchRecords(checkSalesSql, customerId);
        
        long saleCount = 0;
        if (salesResult != null && !salesResult.isEmpty()) {
            Object countObj = salesResult.get(0).get("sale_count");
            if (countObj instanceof Number) {
                saleCount = ((Number) countObj).longValue();
            }
        }
        
        if (saleCount > 0) {
            System.out.printf("ERROR: Cannot delete Customer ID %d. They have %d existing sales records. Delete the sales first.%n", customerId, saleCount);
            return;
        }

        System.out.printf("Are you sure you want to permanently DELETE Customer ID %d? (Y/N): ", customerId);
        String confirmation = sc.nextLine();
        
        if (confirmation.equalsIgnoreCase("Y")) {
            String deleteCustomerSql = "DELETE FROM tbl_customer WHERE customer_id = ?";
            con.updateRecord(deleteCustomerSql, customerId);
            
            System.out.printf("Customer ID %d successfully DELETED.%n", customerId);
            viewCustomers(con);
        } else {
            System.out.println("Customer deletion cancelled.");
        }
    }
    
   
    public static void deleteUser(Scanner sc, dbConnect con) {
        viewUsers(con);
        System.out.print("Enter the User ID (u_id) to DELETE: ");
        int userId = 0;
        
        if (sc.hasNextInt()) {
             userId = sc.nextInt();
        } else {
            sc.next();
            System.out.println("Invalid User ID input. Aborting delete.");
            return;
        }
        sc.nextLine();
        
        System.out.printf("Are you sure you want to permanently DELETE User ID %d? (Y/N): ", userId);
        String confirmation = sc.nextLine();
        
        if (confirmation.equalsIgnoreCase("Y")) {
            String deleteUserSql = "DELETE FROM tbl_user WHERE u_id = ?";
            con.updateRecord(deleteUserSql, userId);
            
            System.out.printf("User ID %d successfully DELETED.%n", userId);
            viewUsers(con);
        } else {
            System.out.println("User deletion cancelled.");
        }
    }

    // --- END NEW DELETE METHODS ---
    
    public static void main(String[] args) {
        dbConnect con = new dbConnect();
        con.connectDB();
        
        Scanner sc = new Scanner(System.in);
        char cont;

        
        while (loggedInUserType == null) {
            System.out.println("\n===== EGG SALES SYSTEM | AUTH MENU =====");
            System.out.println("1. Login");
            System.out.println("2. Register New User");
            System.out.println("3. Exit System");
            System.out.print("Enter choice: ");
            
            int authChoice = 0;
            if (sc.hasNextInt()) {
                authChoice = sc.nextInt();
            } else {
                sc.next();
                authChoice = 0;
            }
            sc.nextLine();

            switch (authChoice) {
                case 1:
                    login(sc, con);
                    break;
                case 2:
                    register(sc, con);
                    break;
                case 3:
                    System.out.println("System shutting down. Goodbye! 👋");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please select 1, 2, or 3.");
            }
        }

          int choice;
        String menuTitle = "===== EGG SALES SYSTEM | " + loggedInUserType.toUpperCase() + " MENU =====";


        do {
            System.out.println("\n" + menuTitle);
            System.out.println("1. Record a New Sale");
            
            
            if ("Admin".equalsIgnoreCase(loggedInUserType)) {
                System.out.println("2. Manage Products");
                System.out.println("3. View All Sales History");
                System.out.println("4. Delete a Sale (Admin Only)"); 
                System.out.println("5. Manage Customers");
                System.out.println("6. ** Manage User Approvals / Deletions **");
                System.out.println("7. Exit"); // Changed from 6 to 7
            } else { 
                System.out.println("2. View All Sales History");
                System.out.println("3. Manage Customers");
                System.out.println("4. Exit");
            }

            System.out.print("Enter choice: ");
            
            
            if (sc.hasNextInt()) {
                choice = sc.nextInt();
                if ("Staff".equalsIgnoreCase(loggedInUserType) && choice > 1) {
                    if (choice == 2) choice = 3;
                    else if (choice == 3) choice = 5; 
                    else if (choice == 4) choice = 7; 
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                sc.next(); 
                choice = 0;
            }
            sc.nextLine();

            switch (choice) {
                case 1:
                    recordNewSale(sc, con);
                    break;

                case 2:
                    
                    if ("Admin".equalsIgnoreCase(loggedInUserType)) {
                            manageProducts(sc, con);
                    } else {
                        
                        System.out.println("Access Denied. Product Management requires Admin privileges.");
                    }
                    break;

                case 3:
                    
                    System.out.println("\n--- ALL SALES HISTORY ---");
                    viewSales(con);
                    break;
                    
                case 4:
                
                    if ("Admin".equalsIgnoreCase(loggedInUserType)) {
                        deleteSale(sc, con);
                    } else {
                        System.out.println("Invalid choice.");
                    }
                    break;
                    
                case 5:
                    
                    manageCustomers(sc, con);
                    break;

                case 6:
                   
                    if ("Admin".equalsIgnoreCase(loggedInUserType)) {
                        manageUserApproval(sc, con);
                    } else {
                        System.out.println("Invalid choice.");
                    }
                    break;

                case 7:
                    
                    System.out.println("System shutting down. Goodbye! 👋");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice. Please select an option from the menu.");
            }

            System.out.print("Do you want to return to the Main Menu? (Y/N): ");
            String continueInput = sc.nextLine();
            cont = (continueInput.length() > 0) ? continueInput.toUpperCase().charAt(0) : 'N';

        } while (cont == 'Y');

        System.out.println("Thank you! Program ended. 👋");
    }
    
    public static void  recordNewSale(Scanner sc, dbConnect con) {
        System.out.println("\n--- RECORD NEW SALE ---");
        
        viewCustomers(con);
        System.out.print("Enter Customer ID for this sale: ");
        int customerId;
        try {
            customerId = sc.nextInt();
        } catch (java.util.InputMismatchException e) {
            System.out.println("Invalid input for Customer ID. Aborting sale.");
            sc.nextLine(); 
            return;
        }
        sc.nextLine(); 
        
        String saleDate = LocalDateTime.now().toString();
        String saleSql = "INSERT INTO tbl_sale(customer_id, sale_date, total_amount) VALUES (?, ?, ?)";
        
        
        con.addRecord(saleSql, customerId, saleDate, 0.0);
        
        
        String maxIdQuery = "SELECT MAX(sale_id) AS max_id FROM tbl_sale";
        List<Map<String, Object>> maxIdResult = con.fetchRecords(maxIdQuery);
        
        int saleId = 0;
        if (maxIdResult != null && !maxIdResult.isEmpty()) {
            Object maxIdObj = maxIdResult.get(0).get("max_id");
            if (maxIdObj != null) {
                
                if (maxIdObj instanceof Number) {
                    saleId = ((Number) maxIdObj).intValue();
                } else {
                    try {
                        saleId = Integer.parseInt(maxIdObj.toString());
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing Sale ID after insert.");
                        
                    }
                }
            }
        }

        if (saleId == 0) { 
            System.out.println("Failed to retrieve new Sale ID. Aborting sale item entry.");
            return;
        }
        
        System.out.printf("Sale Header created with ID: %d.%n", saleId);
        
        double totalSaleAmount = 0.0;
        char addMore;
        
        do {
            viewProducts(con);
            System.out.print("Enter Product ID to purchase: ");
            int productId;
            int quantity;
            try {
                productId = sc.nextInt();
                System.out.print("Enter Quantity: ");
                quantity = sc.nextInt();
            } catch (java.util.InputMismatchException e) {
                System.out.println("Invalid input for Product ID or Quantity. Skipping item.");
                sc.nextLine(); 
                addMore = 'N'; 
                continue;
            }
            sc.nextLine();

            String prodQry = "SELECT unit_price, stock_quantity FROM tbl_product WHERE product_id = ?";
            List<Map<String, Object>> productResult = con.fetchRecords(prodQry, productId);
            
            if (productResult == null || productResult.isEmpty()) {
                System.out.println("Product ID not found. Skipping item.");
            } else {
                Map<String, Object> product = productResult.get(0);
                double unitPrice = Double.parseDouble(product.get("unit_price").toString());
                
                int stock = Integer.valueOf(product.get("stock_quantity").toString());

                if (quantity > stock) {
                    System.out.println("Error: Insufficient stock. Available: " + stock);
                } else if (quantity <= 0) {
                     System.out.println("Error: Quantity must be a positive number. Skipping item.");
                } else {
                    double subtotal = unitPrice * quantity;
                    totalSaleAmount += subtotal;
                    
                    String itemSql = "INSERT INTO tbl_sale_item(sale_id, product_id, quantity_sold, subtotal) VALUES (?, ?, ?, ?)";
                    con.addRecord(itemSql, saleId, productId, quantity, subtotal); 
                    
                    String stockSql = "UPDATE tbl_product SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
                    con.updateRecord(stockSql, quantity, productId);
                    
                    System.out.printf("Added %d units of Product ID %d. Subtotal: $%.2f%n", quantity, productId, subtotal);
                }
            }

            System.out.print("Add another item to this sale? (Y/N): ");
            String addMoreInput = sc.nextLine();
            addMore = (addMoreInput.length() > 0) ? addMoreInput.toUpperCase().charAt(0) : 'N';
            
        } while (addMore == 'Y');
        
        
        String finalSaleSql = "UPDATE tbl_sale SET total_amount = ? WHERE sale_id = ?";
        con.updateRecord(finalSaleSql, totalSaleAmount, saleId);
        
        System.out.printf("\n--- SALE COMPLETED (ID: %d) ---%n", saleId);
        System.out.printf("Total Amount Due: $%.2f%n", totalSaleAmount);
    }
    
    public static void manageProducts(Scanner sc, dbConnect con) {
        System.out.println("\n--- PRODUCT MANAGEMENT (Admin Only) ---");
        System.out.println("1. Add New Product");
        System.out.println("2. View All Products");
        System.out.println("3. Update Product Stock");
        System.out.print("Enter choice: ");
        
        int choice = 0;
        if (sc.hasNextInt()) {
            choice = sc.nextInt();
        } else {
            sc.next();
            System.out.println("Invalid input.");
            return;
        }
        sc.nextLine();

        switch (choice) {
            case 1:
                System.out.print("Enter Product Name (e.g., Large White Eggs): ");
                String name = sc.nextLine();
                double price = 0.0;
                int stock = 0;
                
                try {
                    System.out.print("Enter Unit Price: ");
                    price = sc.nextDouble();
                    System.out.print("Enter Initial Stock Quantity: ");
                    stock = sc.nextInt();
                } catch (java.util.InputMismatchException e) {
                    System.out.println("Invalid input for Price or Stock. Aborting product addition.");
                    sc.nextLine(); // Clear the buffer
                    return;
                }
                sc.nextLine(); 
                
                String sql = "INSERT INTO tbl_product(name, unit_price, stock_quantity) VALUES (?, ?, ?)";
                con.addRecord(sql, name, price, stock); // Call the void method
                System.out.println("Product added successfully.");
                break;
                
            case 2:
                viewProducts(con);
                break;
                
            case 3:
                viewProducts(con);
                int id = 0;
                int newStock = 0;
                
                try {
                    System.out.print("Enter Product ID to update stock: ");
                    id = sc.nextInt();
                    System.out.print("Enter NEW stock quantity (overwrite existing): ");
                    newStock = sc.nextInt();
                } catch (java.util.InputMismatchException e) {
                    System.out.println("Invalid input for ID or Stock. Aborting stock update.");
                    sc.nextLine(); // Clear the buffer
                    return;
                }
                sc.nextLine();
                
                String updateSql = "UPDATE tbl_product SET stock_quantity = ? WHERE product_id = ?";
                con.updateRecord(updateSql, newStock, id);
                System.out.println("Stock updated successfully.");
                break;
                
            default:
                System.out.println("Invalid choice for Product Management.");
        }
    }
    
    public static void manageCustomers(Scanner sc, dbConnect con) {
        System.out.println("\n--- CUSTOMER MANAGEMENT ---");
        System.out.println("1. Add New Customer");
        System.out.println("2. View All Customers");
        System.out.println("3. Delete a Customer"); 
        System.out.print("Enter choice: ");
        
        int choice = 0;
        if (sc.hasNextInt()) {
            choice = sc.nextInt();
        } else {
            sc.next();
            System.out.println("Invalid input.");
            return;
        }
        sc.nextLine();
        
        switch (choice) {
            case 1 -> {
                System.out.print("Enter Customer Name: ");
                String name = sc.nextLine();
                System.out.print("Enter Phone Number: ");
                String phone = sc.nextLine();
                System.out.print("Enter Address: ");
                String address = sc.nextLine();
                
                String sql = "INSERT INTO tbl_customer(name, phone, address) VALUES (?, ?, ?)";
                con.addRecord(sql, name, phone, address);
                System.out.println("Customer added successfully.");
            }
                
            case 2 -> viewCustomers(con);
                
            case 3 -> deleteCustomer(sc, con); // NEW
                
            default -> System.out.println("Invalid choice for Customer Management.");
        }
    }

}
