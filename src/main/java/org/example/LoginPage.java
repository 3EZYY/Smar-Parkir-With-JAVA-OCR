import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPage extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginPage() {
        // Setting up the frame
        setTitle("Login - Sistem Parkir Otomatis");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        // Main panel for the login form
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3, 2, 10, 10)); // 3 rows, 2 columns

        // Labels and fields
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        usernameField = new JTextField();
        passwordField = new JPasswordField();

        // Login button
        JButton loginButton = new JButton("Login");

        // Adding components to the panel
        mainPanel.add(usernameLabel);
        mainPanel.add(usernameField);
        mainPanel.add(passwordLabel);
        mainPanel.add(passwordField);
        mainPanel.add(new JLabel()); // Empty label for spacing
        mainPanel.add(loginButton);

        // Adding the panel to the frame
        add(mainPanel, BorderLayout.CENTER);

        // Login button action listener
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the username and password entered
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                // Check if the username and password are correct (admin/admin)
                if ("admin".equals(username) && "admin".equals(password)) {
                    // If login is successful, show the Admin Dashboard
                    JOptionPane.showMessageDialog(LoginPage.this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    new AdminDashboard(); // Open Admin Dashboard
                    dispose(); // Close login window
                } else {
                    // If login fails, show error message
                    JOptionPane.showMessageDialog(LoginPage.this, "Invalid Username or Password", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public static void main(String[] args) {
        // Run the login page
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginPage().setVisible(true);
            }
        });
    }
}
