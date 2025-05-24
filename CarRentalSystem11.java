import javax.swing.*;
import java.awt.*;
import java.sql.*;


public class CarRentalSystem {

    // JDBC connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/car_rental";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "W7301@jqir#";

    public static void main(String[] args) {
        new CarRentalSystem().createHomePage();
    }

    private JFrame frame;
    private String currentUser;

    public void createHomePage() {
        frame = new JFrame("Car Rental System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 550);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Car Rental System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        header.add(title, BorderLayout.CENTER);

        JPanel headerButtons = new JPanel();
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        headerButtons.add(loginButton);
        headerButtons.add(registerButton);
        header.add(headerButtons, BorderLayout.EAST);

        JPopupMenu optionMenu = new JPopupMenu();
        JMenuItem searchCars = new JMenuItem("Search Cars");
        JMenuItem availableCars = new JMenuItem("Available Cars");
        JMenuItem bookCar = new JMenuItem("Book A Car");
        JMenuItem manageCars = new JMenuItem("Manage Cars");
        JMenuItem aboutUs = new JMenuItem("About Us");

        optionMenu.add(searchCars);
        optionMenu.add(availableCars);
        optionMenu.add(bookCar);
        optionMenu.add(manageCars);
        optionMenu.add(aboutUs);

        JButton optionButton = new JButton("Options");
        optionButton.addActionListener(e -> optionMenu.show(optionButton, optionButton.getWidth() / 2, optionButton.getHeight() / 2));
        header.add(optionButton, BorderLayout.WEST);

        frame.add(header, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new GridLayout(1, 2, 50, 50));
        JButton servicesButton = new JButton("Services We Provide");
        JButton policiesButton = new JButton("Our Policies");

        servicesButton.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Welcome to the Car Rental System!\n\n"
                        + "Services we offer:\n"
                        + "- Affordable car rentals for personal or business use.\n"
                        + "- Wide selection of vehicles: Economy, SUVs, Luxury Cars.\n"
                        + "- Easy online booking system.\n"
                        + "- Flexible rental durations: hourly, daily, weekly.\n"
                        + "- Insurance coverage for all rentals.\n"
                        + "- Excellent customer support for your needs.\n",
                "Services", JOptionPane.INFORMATION_MESSAGE));
        policiesButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Our policies:\n"
                + "- Renters must be 21+ with a valid driver’s license.\n"
                + "- Mandatory insurance is required, with optional coverage for theft and roadside assistance.\n"
                + "- Vehicles are for legal use only; damages, fines, and late returns incur additional charges.\n"
                + "- The company may substitute a similar or higher-category vehicle if the reserved car is unavailable.\n"
                + "- Modifications to the booking are allowed up to 24 hours before pickup, with possible fees.\n",
                "Policies", JOptionPane.INFORMATION_MESSAGE));

        mainContent.add(servicesButton);
        mainContent.add(policiesButton);

        frame.add(mainContent, BorderLayout.CENTER);

        loginButton.addActionListener(e -> showLoginPage());
        registerButton.addActionListener(e -> showRegisterPage());
        searchCars.addActionListener(e -> showSearchCarsPage());
        availableCars.addActionListener(e -> showAvailableCarsPage());
        bookCar.addActionListener(e -> showLoginPage());
        manageCars.addActionListener(e -> showAdminLoginPage());
        aboutUs.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Group Members :\n"
                + "1. SUSMITA AKAND TAMA                                        \n    0242310005101478\n   CSE 64_RE(A)\n\n"
               ,
                "About Us", JOptionPane.INFORMATION_MESSAGE));

        frame.setVisible(true);
    }

    private void showSearchCarsPage() {
        JFrame searchFrame = new JFrame("Search Cars");
        searchFrame.setSize(400, 200);
        searchFrame.setLocationRelativeTo(null);
        searchFrame.setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("Search");

        searchPanel.add(new JLabel("Enter Car Model: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);

        searchButton.addActionListener(e -> {
            String carModel = searchField.getText();
            if (carModel.isEmpty()) {
                JOptionPane.showMessageDialog(searchFrame, "Please enter a car model.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "SELECT * FROM cars WHERE model = ? AND status = 'available'";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, carModel);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    StringBuilder details = new StringBuilder("Car Details:\n");
                    details.append("Brand: ").append(rs.getString("brand")).append("\n");
                    details.append("Model: ").append(rs.getString("model")).append("\n");
                    details.append("Plate No: ").append(rs.getString("plate_no")).append("\n");
                    details.append("Rent: ").append(rs.getDouble("rent")).append("/hour\n");
                    resultArea.setText(details.toString());
                } else {
                    resultArea.setText("Car not found.!!");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(searchFrame, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        searchFrame.add(searchPanel, BorderLayout.NORTH);
        searchFrame.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        searchFrame.setVisible(true);
    }

    private void showAvailableCarsPage() {
        JFrame availableCarsFrame = new JFrame("Available and Booked Cars");
        availableCarsFrame.setSize(600, 500);
        availableCarsFrame.setLocationRelativeTo(null);
        availableCarsFrame.setLayout(new GridLayout(2, 1, 10, 10));

        // Text areas for available and booked cars
        JTextArea availableCarsList = new JTextArea();
        availableCarsList.setEditable(false);
        JTextArea bookedCarsList = new JTextArea();
        bookedCarsList.setEditable(false);

        // Load available cars from the database
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String availableSql = "SELECT * FROM cars WHERE status = 'available'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(availableSql);

            StringBuilder availableDetails = new StringBuilder("Available Cars:\n");
            while (rs.next()) {
                availableDetails.append("Brand: ").append(rs.getString("brand"))
                        .append(", Model: ").append(rs.getString("model"))
                        .append(", Plate No: ").append(rs.getString("plate_no"))
                        .append(", Rent: ").append(rs.getDouble("rent")).append("/hour\n");
            }
            availableCarsList.setText(availableDetails.toString());

            // Load booked cars from the database
            String bookedSql = "SELECT * FROM cars WHERE status = 'booked'";
            ResultSet rsBooked = stmt.executeQuery(bookedSql);

            StringBuilder bookedDetails = new StringBuilder("Booked Cars:\n");
            while (rsBooked.next()) {
                bookedDetails.append("Brand: ").append(rsBooked.getString("brand"))
                        .append(", Model: ").append(rsBooked.getString("model"))
                        .append(", Plate No: ").append(rsBooked.getString("plate_no"))
                        .append(", Rent: ").append(rsBooked.getDouble("rent")).append("/hour\n");
            }
            bookedCarsList.setText(bookedDetails.toString());

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(availableCarsFrame, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        // Add text areas to the frame
        availableCarsFrame.add(new JScrollPane(availableCarsList));
        availableCarsFrame.add(new JScrollPane(bookedCarsList));

        availableCarsFrame.setVisible(true);
    }

    private void showAdminLoginPage() {
        JFrame adminLoginFrame = new JFrame("Admin Login");
        adminLoginFrame.setSize(400, 250); // Slightly increased height for better layout
        adminLoginFrame.setLocationRelativeTo(null);
        adminLoginFrame.setLayout(null); // Using null layout for precise control over component sizes and positions

        Font font = new Font("Arial", Font.PLAIN, 16);

// Username label and text field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(50, 40, 100, 30); // Position and size
        usernameLabel.setFont(font);
        adminLoginFrame.add(usernameLabel);

        JTextField adminUsernameField = new JTextField();
        adminUsernameField.setBounds(160, 40, 180, 30); // Position and size
        adminUsernameField.setFont(font);
        adminLoginFrame.add(adminUsernameField);

// Password label and text field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 90, 100, 30); // Position and size
        passwordLabel.setFont(font);
        adminLoginFrame.add(passwordLabel);

        JPasswordField adminPasswordField = new JPasswordField();
        adminPasswordField.setBounds(160, 90, 180, 30); // Position and size
        adminPasswordField.setFont(font);
        adminLoginFrame.add(adminPasswordField);

// Login button
        JButton loginButton = new JButton("Login");
        loginButton.setBounds(160, 140, 100, 40); // Position and size
        loginButton.setFont(font);
        adminLoginFrame.add(loginButton);
        loginButton.addActionListener(e -> {
            String adminUsername = adminUsernameField.getText();
            String adminPassword = new String(adminPasswordField.getPassword());

            if (adminUsername.equals("admin") && adminPassword.equals("admin")) {
                JOptionPane.showMessageDialog(adminLoginFrame, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                adminLoginFrame.dispose();
                showAdminPanel();
            } else {
                JOptionPane.showMessageDialog(adminLoginFrame, "Invalid Admin Credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        adminLoginFrame.setVisible(true);
    }

    private void showAdminPanel() {
        JFrame adminPanelFrame = new JFrame("Admin Panel");
        adminPanelFrame.setSize(600, 400);
        adminPanelFrame.setLocationRelativeTo(null);
        adminPanelFrame.setLayout(new GridLayout(5, 1, 10, 10));

        JButton addCarButton = new JButton("Add Cars");
        JButton updateCarButton = new JButton("Update Cars");
        JButton deleteCarButton = new JButton("Delete Cars");
        JButton searchUserButton = new JButton("Search User");
        JButton logoutButton = new JButton("Logout");

        adminPanelFrame.add(addCarButton);
        adminPanelFrame.add(updateCarButton);
        adminPanelFrame.add(deleteCarButton);
        adminPanelFrame.add(searchUserButton);
        adminPanelFrame.add(logoutButton);

        addCarButton.addActionListener(e -> showAddCarPage());
        updateCarButton.addActionListener(e -> showUpdateCarPage());
        deleteCarButton.addActionListener(e -> showDeleteCarPage());
        searchUserButton.addActionListener(e -> showSearchUserPage());

        logoutButton.addActionListener(e -> {
            adminPanelFrame.dispose();
            createHomePage();
        });

        adminPanelFrame.setVisible(true);
    }

    private void showAddCarPage() {
        JFrame addCarFrame = new JFrame("Add Cars");
        addCarFrame.setSize(400, 300);
        addCarFrame.setLocationRelativeTo(null);
        addCarFrame.setLayout(new GridLayout(5, 2, 5, 5));

        JTextField brandField = new JTextField();
        JTextField modelField = new JTextField();
        JTextField plateNoField = new JTextField();
        JTextField rentField = new JTextField();

        addCarFrame.add(new JLabel("Car Brand:"));
        addCarFrame.add(brandField);
        addCarFrame.add(new JLabel("Car Model:"));
        addCarFrame.add(modelField);
        addCarFrame.add(new JLabel("Plate Number:"));
        addCarFrame.add(plateNoField);
        addCarFrame.add(new JLabel("Rent (per hour):"));
        addCarFrame.add(rentField);

        JButton addButton = new JButton("Add Car");
        addCarFrame.add(new JLabel());
        addCarFrame.add(addButton);

        addButton.addActionListener(e -> {
            String brand = brandField.getText();
            String model = modelField.getText();
            String plateNo = plateNoField.getText();
            String rent = rentField.getText();

            if (brand.isEmpty() || model.isEmpty() || plateNo.isEmpty() || rent.isEmpty()) {
                JOptionPane.showMessageDialog(addCarFrame, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "INSERT INTO cars (brand, model, plate_no, rent, status) VALUES (?, ?, ?, ?, 'available')";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, brand);
                pstmt.setString(2, model);
                pstmt.setString(3, plateNo);
                pstmt.setDouble(4, Double.parseDouble(rent));
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(addCarFrame, "Car added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                addCarFrame.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(addCarFrame, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        addCarFrame.setVisible(true);
    }

    private void showBookCarPage(String username) {
        JFrame bookCarFrame = new JFrame("Book A Car");
        bookCarFrame.setSize(400, 300);
        bookCarFrame.setLocationRelativeTo(null);
        bookCarFrame.setLayout(new GridLayout(4, 2, 5, 5));

        JComboBox<String> carModelBox = new JComboBox<>();
        JTextField rentingPeriodField = new JTextField();
        JLabel approximateAmountLabel = new JLabel("Approximate Amount: ");
        JButton calculateButton = new JButton("Calculate");
        JButton bookButton = new JButton("Book Car");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT model FROM cars WHERE status = 'available'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                carModelBox.addItem(rs.getString("model"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(bookCarFrame, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        bookCarFrame.add(new JLabel("Select Car Model:"));
        bookCarFrame.add(carModelBox);
        bookCarFrame.add(new JLabel("Renting Period (hours):"));
        bookCarFrame.add(rentingPeriodField);
        bookCarFrame.add(approximateAmountLabel);
        bookCarFrame.add(calculateButton);
        bookCarFrame.add(new JLabel());
        bookCarFrame.add(bookButton);

        calculateButton.addActionListener(e -> {
            try {
                double rentPerHour = 0;
                String selectedModel = (String) carModelBox.getSelectedItem();

                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String sql = "SELECT rent FROM cars WHERE model = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, selectedModel);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        rentPerHour = rs.getDouble("rent");
                    }
                }

                int hours = Integer.parseInt(rentingPeriodField.getText());
                double approximateAmount = rentPerHour * hours;
                approximateAmountLabel.setText("Approximate Amount: " + approximateAmount);
            } catch (NumberFormatException | SQLException ex) {
                JOptionPane.showMessageDialog(bookCarFrame, "Invalid Input!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bookButton.addActionListener(e -> {
            String selectedModel = (String) carModelBox.getSelectedItem();
            int hours = Integer.parseInt(rentingPeriodField.getText());

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "INSERT INTO bookings (username, car_model, hours) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, username);
                pstmt.setString(2, selectedModel);
                pstmt.setInt(3, hours);
                pstmt.executeUpdate();

                String updateCarStatus = "UPDATE cars SET status = 'booked' WHERE model = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateCarStatus);
                updateStmt.setString(1, selectedModel);
                updateStmt.executeUpdate();

                JOptionPane.showMessageDialog(bookCarFrame, "Car booked successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                bookCarFrame.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(bookCarFrame, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bookCarFrame.setVisible(true);
    }
    private void showProfileHistory(String username) {
        JFrame profileHistoryFrame = new JFrame("Profile History");
        profileHistoryFrame.setSize(600, 400);
        profileHistoryFrame.setLocationRelativeTo(null);

        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT car_model, hours, booking_date FROM bookings WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder history = new StringBuilder("Booking History:\n");
            while (rs.next()) {
                history.append("Car Model: ").append(rs.getString("car_model"))
                        .append(", Hours: ").append(rs.getInt("hours"))
                        .append(", Date: ").append(rs.getDate("booking_date")).append("\n");
            }
            historyArea.setText(history.toString());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(profileHistoryFrame, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        profileHistoryFrame.add(new JScrollPane(historyArea));
        profileHistoryFrame.setVisible(true);
    }


    private void showUpdateProfilePage(String username) {
        JFrame updateProfileFrame = new JFrame("Update Profile");
        updateProfileFrame.setSize(400, 400);
        updateProfileFrame.setLocationRelativeTo(null);
        updateProfileFrame.setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(8, 1, 5, 5)); // 8 rows, 1 column for labels and fields
        JTextField phoneField = new JTextField();
        JTextField newUsernameField = new JTextField();
        JPasswordField oldPasswordField = new JPasswordField();
        JPasswordField newPasswordField = new JPasswordField();

        inputPanel.add(new JLabel("New Phone Number:"));
        inputPanel.add(phoneField);
        inputPanel.add(new JLabel("New Username:"));
        inputPanel.add(newUsernameField);
        inputPanel.add(new JLabel("Old Password:"));
        inputPanel.add(oldPasswordField);
        inputPanel.add(new JLabel("New Password:"));
        inputPanel.add(newPasswordField);

        JButton updateButton = new JButton("Update Profile");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(updateButton);

        updateProfileFrame.add(inputPanel, BorderLayout.CENTER);
        updateProfileFrame.add(buttonPanel, BorderLayout.SOUTH);

        updateButton.addActionListener(e -> {
            String newPhone = phoneField.getText();
            String newUsername = newUsernameField.getText();
            String oldPassword = new String(oldPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());

            if (newPhone.isEmpty() || newUsername.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(updateProfileFrame, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String checkPasswordSql = "SELECT * FROM users WHERE username = ? AND password = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkPasswordSql);
                checkStmt.setString(1, username);
                checkStmt.setString(2, oldPassword);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    String updateSql = "UPDATE users SET phone = ?, username = ?, password = ? WHERE username = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setString(1, newPhone);
                    updateStmt.setString(2, newUsername);
                    updateStmt.setString(3, newPassword);
                    updateStmt.setString(4, username);
                    updateStmt.executeUpdate();

                    JOptionPane.showMessageDialog(updateProfileFrame, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    updateProfileFrame.dispose();
                } else {
                    JOptionPane.showMessageDialog(updateProfileFrame, "Incorrect old password!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(updateProfileFrame, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        updateProfileFrame.setVisible(true);
    }



    private void showUpdateCarPage() {
        JFrame updateCarFrame = new JFrame("Update Car Status");
        updateCarFrame.setSize(450, 250); // Slightly increased width and height for better layout
        updateCarFrame.setLocationRelativeTo(null);
        updateCarFrame.setLayout(null); // Using null layout for precise control

        Font font = new Font("Arial", Font.PLAIN, 16);

// Car Plate Number label and text field
        JLabel plateNoLabel = new JLabel("Car Plate Number:");
        plateNoLabel.setBounds(30, 30, 150, 30); // Position and size
        plateNoLabel.setFont(font);
        updateCarFrame.add(plateNoLabel);

        JTextField plateNoField = new JTextField();
        plateNoField.setBounds(200, 30, 200, 30); // Position and size
        plateNoField.setFont(font);
        updateCarFrame.add(plateNoField);

// New Status label and dropdown
        JLabel statusLabel = new JLabel("New Status:");
        statusLabel.setBounds(30, 80, 150, 30); // Position and size
        statusLabel.setFont(font);
        updateCarFrame.add(statusLabel);

        JComboBox<String> statusBox = new JComboBox<>(new String[]{"available", "booked", "out for service"});
        statusBox.setBounds(200, 80, 200, 30); // Position and size
        statusBox.setFont(font);
        updateCarFrame.add(statusBox);

// Update Status button
        JButton updateButton = new JButton("Update Status");
        updateButton.setBounds(150, 140, 150, 40); // Position and size
        updateButton.setFont(font);
        updateCarFrame.add(updateButton);

        updateButton.addActionListener(e -> {
            String plateNo = plateNoField.getText();
            String newStatus = (String) statusBox.getSelectedItem();

            if (plateNo.isEmpty()) {
                JOptionPane.showMessageDialog(updateCarFrame, "Please enter the plate number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "UPDATE cars SET status = ? WHERE plate_no = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, newStatus);
                pstmt.setString(2, plateNo);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(updateCarFrame, "Car status updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(updateCarFrame, "Car not found!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(updateCarFrame, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        updateCarFrame.setVisible(true);
    }

    private void showDeleteCarPage() {
        JFrame deleteCarFrame = new JFrame("Delete Car");
        deleteCarFrame.setSize(450, 200); // Slightly increased width for better layout
        deleteCarFrame.setLocationRelativeTo(null);
        deleteCarFrame.setLayout(null); // Using null layout for precise control

        Font font = new Font("Arial", Font.PLAIN, 16);

// Car Plate Number label and text field
        JLabel plateNoLabel = new JLabel("Car Plate Number:");
        plateNoLabel.setBounds(30, 40, 150, 30); // Position and size
        plateNoLabel.setFont(font);
        deleteCarFrame.add(plateNoLabel);

        JTextField plateNoField = new JTextField();
        plateNoField.setBounds(200, 40, 200, 30); // Position and size
        plateNoField.setFont(font);
        deleteCarFrame.add(plateNoField);

// Delete Car button
        JButton deleteButton = new JButton("Delete Car");
        deleteButton.setBounds(150, 100, 150, 40); // Position and size
        deleteButton.setFont(font);
        deleteCarFrame.add(deleteButton);

        deleteButton.addActionListener(e -> {
            String plateNo = plateNoField.getText();

            if (plateNo.isEmpty()) {
                JOptionPane.showMessageDialog(deleteCarFrame, "Please enter the plate number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "DELETE FROM cars WHERE plate_no = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, plateNo);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(deleteCarFrame, "Car deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(deleteCarFrame, "Car not found!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(deleteCarFrame, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteCarFrame.setVisible(true);
    }

    private void showSearchUserPage() {
        JFrame searchUserFrame = new JFrame("Search User");
        searchUserFrame.setSize(500, 500);
        searchUserFrame.setLocationRelativeTo(null);

        JTextField usernameField = new JTextField();
        JTextArea userInfoArea = new JTextArea();
        userInfoArea.setEditable(false);

        JButton searchButton = new JButton("Search");

        searchUserFrame.setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        inputPanel.add(new JLabel("Enter Username:"));
        inputPanel.add(usernameField);

        searchButton.addActionListener(e -> {
            String username = usernameField.getText();

            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(searchUserFrame, "Please enter a username.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String userSql = "SELECT * FROM users WHERE username = ?";
                PreparedStatement userPstmt = conn.prepareStatement(userSql);
                userPstmt.setString(1, username);
                ResultSet userRs = userPstmt.executeQuery();

                StringBuilder userInfo = new StringBuilder();

                if (userRs.next()) {
                    userInfo.append("User Details:\n");
                    userInfo.append("Name: ").append(userRs.getString("first_name"))
                            .append(" ").append(userRs.getString("last_name")).append("\n");
                    userInfo.append("Phone: ").append(userRs.getString("phone")).append("\n");
                    userInfo.append("Address: ").append(userRs.getString("address")).append("\n");
                    userInfo.append("NID: ").append(userRs.getString("nid")).append("\n");
                    userInfo.append("License: ").append(userRs.getString("license")).append("\n\n");

                    String bookingSql = "SELECT car_model, booking_date, hours FROM bookings WHERE username = ?";
                    PreparedStatement bookingPstmt = conn.prepareStatement(bookingSql);
                    bookingPstmt.setString(1, username);
                    ResultSet bookingRs = bookingPstmt.executeQuery();

                    userInfo.append("Booking History:\n");
                    while (bookingRs.next()) {
                        userInfo.append("Car Model: ").append(bookingRs.getString("car_model"))
                                .append(", Booking Date: ").append(bookingRs.getDate("booking_date"))
                                .append(", Hours: ").append(bookingRs.getInt("hours"))
                                .append("\n");
                    }

                    if (!bookingRs.isBeforeFirst()) {
                        userInfo.append("No booking history found.\n");
                    }

                } else {
                    userInfo.append("User not found.\n");
                }

                userInfoArea.setText(userInfo.toString());

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(searchUserFrame, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        searchUserFrame.add(inputPanel, BorderLayout.NORTH);
        searchUserFrame.add(new JScrollPane(userInfoArea), BorderLayout.CENTER);
        searchUserFrame.add(searchButton, BorderLayout.SOUTH);

        searchUserFrame.setVisible(true);
    }


    private void showLoginPage() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setSize(400, 250);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setLayout(null); // Using null layout for precise control over component sizes and positions.

        Font font = new Font("Arial", Font.PLAIN, 16);

// Username label and text field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(50, 50, 100, 30); // Position and size
        usernameLabel.setFont(font);
        loginFrame.add(usernameLabel);

        JTextField usernameField = new JTextField();
        usernameField.setBounds(160, 50, 180, 30); // Position and size
        usernameField.setFont(font);
        loginFrame.add(usernameField);

// Password label and text field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 100, 100, 30); // Position and size
        passwordLabel.setFont(font);
        loginFrame.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(160, 100, 180, 30); // Position and size
        passwordField.setFont(font);
        loginFrame.add(passwordField);

// Login button
        JButton loginButton = new JButton("Login");
        loginButton.setBounds(160, 160, 100, 40); // Position and size
        loginButton.setFont(font);
        loginFrame.add(loginButton);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(loginFrame, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    currentUser = username;
                    loginFrame.dispose();
                    showUserMenu();
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "You are not registered yet. Please register first to log-in.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(loginFrame, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loginFrame.setVisible(true);
    }

    private void showUserMenu() {
        JFrame userMenuFrame = new JFrame("User Menu");
        userMenuFrame.setSize(600, 400);
        userMenuFrame.setLocationRelativeTo(null);
        userMenuFrame.setLayout(new GridLayout(5, 1, 10, 10));

        JButton searchCarsButton = new JButton("Search Cars");
        JButton bookCarButton = new JButton("Book A Car");
        JButton profileHistoryButton = new JButton("Profile History");
        JButton updateProfileButton = new JButton("Update Profile");
        JButton logoutButton = new JButton("Logout");

        searchCarsButton.addActionListener(e -> showSearchCarsPage());
        bookCarButton.addActionListener(e -> {
            if (currentUser == null) {
                JOptionPane.showMessageDialog(userMenuFrame, "You are not logged in. Please log-in first.", "Error", JOptionPane.ERROR_MESSAGE);
                showLoginPage();
            } else {
                showBookCarPage(currentUser);
            }
        });

        profileHistoryButton.addActionListener(e -> showProfileHistory(currentUser));
        updateProfileButton.addActionListener(e -> showUpdateProfilePage(currentUser));
        logoutButton.addActionListener(e -> {
            currentUser = null;
            JOptionPane.showMessageDialog(userMenuFrame, "Logged out successfully!", "Info", JOptionPane.INFORMATION_MESSAGE);
            userMenuFrame.dispose();
            createHomePage();
        });

        userMenuFrame.add(searchCarsButton);
        userMenuFrame.add(bookCarButton);
        userMenuFrame.add(profileHistoryButton);
        userMenuFrame.add(updateProfileButton);
        userMenuFrame.add(logoutButton);

        userMenuFrame.setVisible(true);
    }

    private void showRegisterPage() {
        JFrame registerFrame = new JFrame("Register");
        registerFrame.setSize(450, 450);
        registerFrame.setLocationRelativeTo(null);
        registerFrame.setLayout(new GridLayout(10, 2, 5, 5));

        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField nidField = new JTextField();
        JTextField licenseField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        registerFrame.add(new JLabel("First Name:"));
        registerFrame.add(firstNameField);
        registerFrame.add(new JLabel("Last Name:"));
        registerFrame.add(lastNameField);
        registerFrame.add(new JLabel("Address:"));
        registerFrame.add(addressField);
        registerFrame.add(new JLabel("Phone:"));
        registerFrame.add(phoneField);
        registerFrame.add(new JLabel("NID No:"));
        registerFrame.add(nidField);
        registerFrame.add(new JLabel("License No:"));
        registerFrame.add(licenseField);
        registerFrame.add(new JLabel("Username:"));
        registerFrame.add(usernameField);
        registerFrame.add(new JLabel("Password:"));
        registerFrame.add(passwordField);
        registerFrame.add(new JLabel("Confirm Password:"));
        registerFrame.add(confirmPasswordField);

        JButton registerButton = new JButton("Register");
        registerFrame.add(new JLabel());
        registerFrame.add(registerButton);

        registerButton.addActionListener(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String address = addressField.getText();
            String phone = phoneField.getText();
            String nid = nidField.getText();
            String license = licenseField.getText();
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(registerFrame, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "INSERT INTO users (first_name, last_name, address, phone, nid, license, username, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, firstName);
                pstmt.setString(2, lastName);
                pstmt.setString(3, address);
                pstmt.setString(4, phone);
                pstmt.setString(5, nid);
                pstmt.setString(6, license);
                pstmt.setString(7, username);
                pstmt.setString(8, password);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(registerFrame, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                registerFrame.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(registerFrame, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerFrame.setVisible(true);
    }


}

