package qwq.arcane.module;

import qwq.jnic.JNICInclude;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.Date;

@JNICInclude
public class ClientApplication extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField cardField;
    private JButton loginBtn;
    private JButton registerBtn;
    private JButton renewBtn;
    private JTextArea statusArea;
    private String hwid = getHwid();
    private JTabbedPane tabbedPane;
    public static Timer heartbeatTimer;
    public static boolean Hwid = true;
    public static boolean validationPassed = false;
    private JCheckBox autoLoginCheckbox;
    // 简约的2D颜色方案
    private static final Color DARK_BG = new Color(245, 245, 245);   // 浅灰背景
    private static final Color MEDIUM_BG = new Color(255, 255, 255); // 纯白内容区
    private static final Color LIGHT_BG = new Color(230, 230, 230);  // 输入框背景
    private static final Color ACCENT_BLUE = new Color(66, 133, 244);   // Google蓝
    private static final Color ACCENT_GREEN = new Color(52, 168, 83);   // Google绿
    private static final Color ACCENT_YELLOW = new Color(251, 188, 5);  // Google黄
    private static final Color TEXT_COLOR = new Color(60, 60, 60);   // 深灰文字
    private static final Color BORDER_COLOR = new Color(220, 220, 220); // 边框颜色

    // 简约字体
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font MONO_FONT = new Font("Consolas", Font.PLAIN, 12);

    public ClientApplication() {
        super("Arcane-Auth");
        setSize(400, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setupUI();
        checkAutoLogin();
    }
    private void checkAutoLogin() {
        File configFile = new File("autologin.cfg");
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                String username = reader.readLine();
                String encryptedPassword = reader.readLine();

                if (username != null && encryptedPassword != null) {
                    // 解密密码
                    String password = new String(Base64.getDecoder().decode(encryptedPassword));

                    // 填充到输入框
                    usernameField.setText(username);
                    passwordField.setText(password);
                    autoLoginCheckbox.setSelected(true);

                    // 延迟执行自动登录
                    SwingUtilities.invokeLater(() -> {
                        login();
                    });
                }
            } catch (Exception ex) {
                logError("自动登录配置读取失败: " + ex.getMessage());
            }
        }
    }
    private void saveAutoLoginConfig() {
        try (PrintWriter writer = new PrintWriter("autologin.cfg")) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            // 加密存储密码
            String encryptedPassword = Base64.getEncoder().encodeToString(password.getBytes());

            writer.println(username);
            writer.println(encryptedPassword);
        } catch (Exception ex) {
            logError("保存自动登录配置失败: " + ex.getMessage());
        }
    }

    // 添加：清除自动登录配置
    private void clearAutoLoginConfig() {
        File configFile = new File("autologin.cfg");
        if (configFile.exists()) {
            configFile.delete();
        }
    }

    // 添加：记录错误日志
    private void logError(String message) {
        statusArea.append("[" + new Date() + "] ERROR: " + message + "\n");
    }
    // 添加：在认证面板创建自动登录复选框
    private void addAutoLoginCheckbox(JPanel authPanel) {
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkboxPanel.setOpaque(false);

        autoLoginCheckbox = new JCheckBox("remember password");
        autoLoginCheckbox.setFont(REGULAR_FONT);
        autoLoginCheckbox.setForeground(TEXT_COLOR);

        checkboxPanel.add(autoLoginCheckbox);
        authPanel.add(checkboxPanel, BorderLayout.NORTH); // 放在输入区域上方
    }
    private void setupUI() {
        // 设置全局UI样式
        UIManager.put("TabbedPane.background", DARK_BG);
        UIManager.put("TabbedPane.foreground", TEXT_COLOR);
        UIManager.put("TabbedPane.selected", ACCENT_BLUE);

        // 创建主容器
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(DARK_BG);

        // 标题标签
        JLabel titleLabel = new JLabel("Arcane-Auth", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 初始化标签页
        initTabbedPane();
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(mainPanel);

        addAutoLoginCheckbox(mainPanel);
        // 添加事件监听器
        loginBtn.addActionListener(e -> login());
        registerBtn.addActionListener(e -> register());
        renewBtn.addActionListener(e -> renew());
    }

    private void initTabbedPane() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(HEADER_FONT);
        tabbedPane.setBackground(DARK_BG);
        tabbedPane.setForeground(TEXT_COLOR);

        // 自定义标签页UI - 简约风格
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                              int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g;
                if (isSelected) {
                    g2d.setColor(ACCENT_BLUE);
                } else {
                    g2d.setColor(DARK_BG);
                }
                g2d.fillRect(x, y, w, h);
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
                // 无边框
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // 内容区域边框
                g.setColor(BORDER_COLOR);
                g.drawRect(0, 0, tabbedPane.getWidth() - 1, tabbedPane.getHeight() - 1);
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement,
                                               Rectangle[] rects, int tabIndex,
                                               Rectangle iconRect, Rectangle textRect,
                                               boolean isSelected) {
                // 不绘制焦点指示器
            }
        });

        // 创建标签页内容
        tabbedPane.addTab("Login", createAuthPanel());
        tabbedPane.addTab("Info", createStatusPanel());
        tabbedPane.addTab("Help", createHelpPanel());
    }

    private JPanel createAuthPanel() {
        JPanel authPanel = new JPanel(new BorderLayout(10, 10));
        authPanel.setOpaque(false);
        authPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 输入区域
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 8, 12));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        addStyledLabel(inputPanel, "Username:");
        usernameField = createStyledTextField();
        inputPanel.add(usernameField);

        addStyledLabel(inputPanel, "Password:");
        passwordField = createStyledPasswordField();
        inputPanel.add(passwordField);

        addStyledLabel(inputPanel, "Card:");
        cardField = createStyledTextField();
        inputPanel.add(cardField);

        // 按钮区域
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        loginBtn = createFlatButton("Login", ACCENT_GREEN);
        registerBtn = createFlatButton("Register", ACCENT_BLUE);
        renewBtn = createFlatButton("Renew", ACCENT_YELLOW);

        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        buttonPanel.add(renewBtn);

        authPanel.add(inputPanel, BorderLayout.CENTER);
        authPanel.add(buttonPanel, BorderLayout.SOUTH);

        return authPanel;
    }

    private JPanel createStatusPanel() {
        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(MONO_FONT);
        statusArea.setBackground(MEDIUM_BG);
        statusArea.setForeground(TEXT_COLOR);
        statusArea.setCaretColor(TEXT_COLOR);
        statusArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        // 添加滚动条
        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        statusScrollPane.getViewport().setBackground(MEDIUM_BG);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.add(statusScrollPane, BorderLayout.CENTER);

        return statusPanel;
    }

    private JPanel createHelpPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(REGULAR_FONT);
        infoArea.setBackground(null);
        infoArea.setForeground(TEXT_COLOR);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setText("Info:\n- ID: " + hwid +
                "\n\nInstructions:\n1. Register an account first\n2. Log in after registration\n3. Renew your subscription when needed\n4. Contact support for assistance");

        infoPanel.add(infoArea, BorderLayout.CENTER);
        JButton logoutBtn = createFlatButton("注销", new Color(220, 100, 100));
        logoutBtn.addActionListener(e -> logout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(logoutBtn);

        infoPanel.add(buttonPanel, BorderLayout.SOUTH);

        return infoPanel;
    }

    private void addStyledLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setForeground(TEXT_COLOR);
        label.setFont(REGULAR_FONT);
        panel.add(label);
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(REGULAR_FONT);
        field.setBackground(LIGHT_BG);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(REGULAR_FONT);
        field.setBackground(LIGHT_BG);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private JButton createFlatButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(HEADER_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 0, 10, 0));

        // 简单的悬停效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(brightenColor(bgColor, 1.15));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private Color brightenColor(Color color, double factor) {
        int r = Math.min(255, (int)(color.getRed() * factor));
        int g = Math.min(255, (int)(color.getGreen() * factor));
        int b = Math.min(255, (int)(color.getBlue() * factor));
        return new Color(r, g, b);
    }

    // 以下方法保持不变（sendRequest, processResponse, startHeartbeat, sendHeartbeat, login, register, renew, getHwid, main）
    // 功能与原始代码完全一致

    private void sendRequest(String request) {
        new SwingWorker<Void, Void>() {
            String response = null;

            @Override
            protected Void doInBackground() {
                try (Socket socket = new Socket("43.248.188.15", 7070);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    // 加密请求
                    String encryptedRequest = AESUtil.encrypt(request);
                    out.println(encryptedRequest);

                    // 接收加密响应
                    String encryptedResponse = in.readLine();
                    if (encryptedResponse != null) {
                        response = AESUtil.decrypt(encryptedResponse);
                    }
                } catch (Exception ex) {
                    response = "ERROR:通信失败";
                }
                return null;
            }

            @Override
            protected void done() {
                if (response != null) {
                    statusArea.append("[" + new Date() + "] 服务器响应: " + response + "\n");
                    processResponse(response);
                }
            }
        }.execute();
    }

    private void processResponse(String response) {
        if (response.startsWith("SUCCESS")) {
            String message = response.substring(8); // Remove "SUCCESS:"
            String operation = response.split(":")[0]; // 获取操作类型

            if (message.contains("2025") || message.contains("2026") || message.contains("2027") || message.contains("2028") || message.contains("2029") || message.contains("2030") || message.contains("2031") || message.contains("2032") || message.contains("2033") || message.contains("2034") || message.contains("2035") || message.contains("2036") || message.contains("2037") || message.contains("2038")) {
                // 恢复游戏
                try {
                    Hwid = false;
                    Mine.resumeGame();
                    validationPassed = true;
                    statusArea.append("[" + new Date() + "] Game resumed\n");
                } catch (Exception e) {
                    statusArea.append("[" + new Date() + "] Failed to resume game: " + e.getMessage() + "\n");
                }
                if (autoLoginCheckbox.isSelected()) {
                    saveAutoLoginConfig();
                } else {
                    clearAutoLoginConfig();
                }
                // 启动心跳
                startHeartbeat();

                // 切换到状态面板
                tabbedPane.setSelectedIndex(1);

                // 延迟关闭窗口
                new Timer(2000, e -> {
                    ((Timer)e.getSource()).stop();
                    dispose(); // 关闭认证窗口
                }).start();
            }

            // 显示成功消息（所有成功操作）
            JOptionPane.showMessageDialog(this, message, "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } else if (response.startsWith("ERROR")) {
            String errorMessage = response.substring(6); // Remove "ERROR:"
            JOptionPane.showMessageDialog(this, errorMessage, "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startHeartbeat() {
        heartbeatTimer = new Timer(30000, e -> {
            String obfUsername = Base64.getEncoder().encodeToString(usernameField.getText().getBytes());
            String obfPassword = Base64.getEncoder().encodeToString(new String(passwordField.getPassword()).getBytes());
            sendRequest("HEARTBEAT:" + obfUsername + ":" + obfPassword + ":" + hwid);
        });
        heartbeatTimer.start();
    }
    private void logout() {
        clearAutoLoginConfig();
        usernameField.setText("");
        passwordField.setText("");
        statusArea.append("[" + new Date() + "] 已注销\n");
    }
    private void sendHeartbeat() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        new SwingWorker<Void, Void>() {
            String response = null;

            @Override
            protected Void doInBackground() {
                try (Socket socket = new Socket("43.248.188.15", 7070);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    out.println("VERIFY:" + username + ":" + password + ":" + hwid);
                    response = in.readLine();
                } catch (Exception ex) {
                    response = "ERROR:Verification failed";
                }
                return null;
            }

            @Override
            protected void done() {
                if (response != null && response.startsWith("ERROR")) {
                    statusArea.append("[" + new Date() + "] Verification error: " + response + "\n");
                    JOptionPane.showMessageDialog(ClientApplication.this,
                            "Login verification failed",
                            "Verification Error",
                            JOptionPane.WARNING_MESSAGE);
                    if (heartbeatTimer != null) {
                        heartbeatTimer.stop();
                    }
                }
            }
        }.execute();
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password are required", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        sendRequest("LOGIN:" + username + ":" + password + ":" + hwid);
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password are required", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        sendRequest("REGISTER:" + username + ":" + password + ":" + hwid);
    }

    private void renew() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String card = cardField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password are required", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (card.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Card number is required", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        sendRequest("RENEW:" + username + ":" + password + ":" + card);
    }

    private String getHwid() {
        try {
            // Improved command execution
            Process p = Runtime.getRuntime().exec("wmic csproduct get uuid");
            p.waitFor();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), "GBK"))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("UUID")) continue;
                    String hwid = line.trim();
                    if (!hwid.isEmpty()) return hwid;
                }
            }
            return "UNKNOWN_HWID";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR_HWID";
        }
    }

    public static void main() {
        SwingUtilities.invokeLater(() -> {
            ClientApplication client = new ClientApplication();
            client.setVisible(true);
        });
    }
}