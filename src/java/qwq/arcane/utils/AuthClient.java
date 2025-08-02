package qwq.arcane.utils;

import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.yumegod.obfuscation.FlowObfuscate;
import com.yumegod.obfuscation.InvokeDynamic;
import com.yumegod.obfuscation.Rename;
import com.yumegod.obfuscation.StringObfuscate;
import net.minecraft.client.Minecraft;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.prefs.Preferences;

@Rename
@FlowObfuscate
@InvokeDynamic
@StringObfuscate
public class AuthClient extends JFrame {
    private static final String APP_NAME = "ArcaneClient";
    private static final String SERVER_BASE_URL = "http://2697v22.mc5173.cn:13765/api"; // 修改为您的服务器地址

    public static JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberCheckBox;
    private JCheckBox autoLoginCheckBox;
    private JProgressBar progressBar;

    // 当前用户信息
    public static String currentUser;
    private String authToken;

    public AuthClient() {
        setTitle("Login Arcane");
        setSize(450, 350); // 增加高度以适应进度条
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon("icon.png").getImage()); // 添加应用图标

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 创建标题
        JLabel titleLabel = new JLabel("Arcane Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 100, 200)); // 蓝色标题
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 创建表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("登录信息"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // 用户名标签和输入框
        JLabel userLabel = new JLabel("用户名:");
        usernameField = new JTextField(20);
        formPanel.add(userLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        // 密码标签和输入框
        JLabel passLabel = new JLabel("密码:");
        passwordField = new JPasswordField(20);
        formPanel.add(passLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        // 记住我复选框
        rememberCheckBox = new JCheckBox("记住我");
        formPanel.add(rememberCheckBox, gbc);
        gbc.gridx = 1;

        // 自动登录复选框
        autoLoginCheckBox = new JCheckBox("自动登录");
        formPanel.add(autoLoginCheckBox, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        // 进度条
        gbc.gridwidth = 2;
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        formPanel.add(progressBar, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10));

        // 登录按钮
        JButton loginButton = new JButton("登录");
        loginButton.setBackground(new Color(255, 255, 255)); // 蓝色按钮
        loginButton.setForeground(Color.BLACK);
        loginButton.addActionListener(this::loginAction);
        buttonPanel.add(loginButton);

        // 注册按钮
        JButton registerButton = new JButton("注册");
        registerButton.addActionListener(e -> showRegistrationInfo());
        buttonPanel.add(registerButton);

        // 打开KOOK机器人按钮
        JButton kookButton = new JButton("打开KOOK机器人");
        kookButton.addActionListener(e -> openKookBot());
        buttonPanel.add(kookButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 加载保存的配置
        loadConfig();

        // 如果配置了自动登录，尝试登录
        if (autoLoginCheckBox.isSelected()) {
            SwingUtilities.invokeLater(this::attemptAutoLogin);
        }
    }

    private void loginAction(ActionEvent e) {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            showMessage("用户名和密码不能为空", "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 保存配置
        saveConfig();

        // 显示进度条
        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setString("连接服务器...");

        // 在后台执行登录操作
        new Thread(() -> {
            boolean loginSuccess = realLogin(username, new String(password));

            SwingUtilities.invokeLater(() -> {
                progressBar.setVisible(false);

                if (loginSuccess) {
                    showMessage("登录成功!", "欢迎 " + username, JOptionPane.INFORMATION_MESSAGE);
                    startMainApplication();
                    Minecraft.resumeGame();
                } else {
                    showMessage("登录失败，请检查用户名和密码", "登录失败", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }

    private void attemptAutoLogin() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (!username.isEmpty() && password.length > 0) {
            progressBar.setVisible(true);
            progressBar.setValue(0);
            progressBar.setString("自动登录中...");

            new Thread(() -> {
                boolean loginSuccess = realLogin(username, new String(password));

                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(false);

                    if (loginSuccess) {
                        startMainApplication();
                    } else {
                        showMessage("自动登录失败", "登录错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).start();
        }
    }

    private boolean realLogin(String username, String password) {
        try {
            // 获取HWID
            String hwid = generateHWID();

            // 创建登录请求
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_BASE_URL + "/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            String.format("{\"username\":\"%s\",\"password\":\"%s\",\"hwid\":\"%s\"}",
                                    username, hashPassword(password), hwid)
                    ))
                    .build();

            // 更新进度
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(30);
                progressBar.setString("验证凭据...");
            });

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 解析JSON响应
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

            if (jsonResponse.get("success").getAsBoolean()) {
                authToken = jsonResponse.get("token").getAsString();
                currentUser = jsonResponse.get("username").getAsString();

                // 保存token
                Preferences.userRoot().node(APP_NAME).put("authToken", authToken);
                return true;
            } else {
                String errorMsg = jsonResponse.get("error").getAsString();
                SwingUtilities.invokeLater(() ->
                        showMessage("登录失败: " + errorMsg, "错误", JOptionPane.ERROR_MESSAGE));
                return false;
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() ->
                    showMessage("网络错误: " + e.getMessage(), "连接失败", JOptionPane.ERROR_MESSAGE));
            return false;
        }
    }

    private void startMainApplication() {
        // 启动主应用程序
        dispose(); // 关闭登录窗口

        // 创建主应用程序窗口
        JFrame mainFrame = new JFrame("Arcane - 主程序");
        mainFrame.setSize(900, 700);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setLayout(new BorderLayout());

        // 顶部信息栏
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel userLabel = new JLabel("当前用户: " + currentUser);
        JLabel timeLabel = new JLabel("有效期至: 2025-12-31");
        infoPanel.add(userLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(timeLabel);

        // 主内容区
        JTextArea contentArea = new JTextArea();
        contentArea.setText("欢迎使用Arcane系统！\n\n这是您的主应用程序界面。");
        contentArea.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        contentArea.setEditable(false);

        mainFrame.add(infoPanel, BorderLayout.NORTH);
        mainFrame.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        mainFrame.setVisible(true);
    }

    private void showRegistrationInfo() {
        JOptionPane.showMessageDialog(this,
                "请使用KOOK机器人注册账户:\n\n" +
                        "1. 打开KOOK机器人\n" +
                        "2. 发送: !注册 用户名 密码\n" +
                        "3. 注册成功后即可登录",
                "注册说明", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openKookBot() {
        try {
            Desktop.getDesktop().browse(URI.create("https://kook.top/"));
        } catch (Exception ex) {
            showMessage("无法打开浏览器: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    private String generateHWID() {
        // 生成基于系统特征的HWID
        String os = System.getProperty("os.name");
        String osVer = System.getProperty("os.version");
        String user = System.getProperty("user.name");
        String arch = System.getProperty("os.arch");

        String fingerprint = os + "|" + osVer + "|" + user + "|" + arch;
        return Base64.getEncoder().encodeToString(fingerprint.getBytes(StandardCharsets.UTF_8));
    }

    private String hashPassword(String password) {
        // 使用SHA-256哈希算法
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return password; // 如果出错返回原始密码
        }
    }

    private void loadConfig() {
        try {
            Preferences prefs = Preferences.userRoot().node(APP_NAME);

            String username = prefs.get("username", "");
            String password = prefs.get("password", "");
            boolean remember = prefs.getBoolean("remember", false);
            boolean autoLogin = prefs.getBoolean("autoLogin", false);

            usernameField.setText(username);
            passwordField.setText(password);
            rememberCheckBox.setSelected(remember);
            autoLoginCheckBox.setSelected(autoLogin);

            // 如果记住我未选中，清除密码
            if (!remember) {
                passwordField.setText("");
            }
        } catch (Exception e) {
            System.err.println("加载配置失败: " + e.getMessage());
        }
    }

    private void saveConfig() {
        try {
            Preferences prefs = Preferences.userRoot().node(APP_NAME);

            if (rememberCheckBox.isSelected()) {
                prefs.put("username", usernameField.getText());
                prefs.put("password", new String(passwordField.getPassword()));
            } else {
                prefs.remove("username");
                prefs.remove("password");
            }

            prefs.putBoolean("remember", rememberCheckBox.isSelected());
            prefs.putBoolean("autoLogin", autoLoginCheckBox.isSelected());
        } catch (Exception e) {
            System.err.println("保存配置失败: " + e.getMessage());
        }
    }

    public static void main() {
        // 设置UI风格为系统默认
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            AuthClient client = new AuthClient();
            client.setVisible(true);
        });
    }
}