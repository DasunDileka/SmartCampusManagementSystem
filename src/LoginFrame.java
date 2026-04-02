import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

/**
 * Demo accounts (change via Administrator → User management after login):
 * admin / password123 — Administrator;
 * staff / secret — Staff member;
 * student / student123 — Student.
 */
public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final AppServices app;

    public LoginFrame(AppServices app) {
        super("User Login");
        this.app = app;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Sign in", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.LINE_END;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridy = 1;
        form.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        form.add(usernameField, gbc);
        gbc.gridy = 1;
        form.add(passwordField, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton cancelBtn = new JButton("Cancel");
        JButton loginBtn = new JButton("Login");
        getRootPane().setDefaultButton(loginBtn);
        buttons.add(cancelBtn);
        buttons.add(loginBtn);

        root.add(title, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);

        Runnable tryLogin = this::attemptLogin;
        loginBtn.addActionListener(e -> tryLogin.run());
        cancelBtn.addActionListener(e -> dispose());

        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    tryLogin.run();
                }
            }
        };
        usernameField.addKeyListener(enterSubmit);
        passwordField.addKeyListener(enterSubmit);
    }

    private void attemptLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Missing fields",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        app.userDirectory.authenticate(user, pass).ifPresentOrElse(
                session -> {
                    dispose();
                    SwingOnEdt.openDashboard(session, app);
                },
                () -> {
                    JOptionPane.showMessageDialog(this,
                            "Invalid username or password.",
                            "Login failed",
                            JOptionPane.ERROR_MESSAGE);
                    passwordField.setText("");
                    passwordField.requestFocusInWindow();
                });
    }
}
