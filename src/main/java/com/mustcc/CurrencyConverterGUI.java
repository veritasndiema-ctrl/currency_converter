package com.mustcc;

import com.mustcc.exception.AuthException;
import com.mustcc.exception.DataAccessException;
import com.mustcc.exception.InvalidCurrencyException;
import com.mustcc.exception.RateUnavailableException;
import com.mustcc.model.Conversion;
import com.mustcc.model.ConversionResult;
import com.mustcc.model.Favorite;
import com.mustcc.model.RateHistoryEntry;
import com.mustcc.model.User;
import com.mustcc.service.AuthService;
import com.mustcc.service.ConversionService;
import com.mustcc.service.FavoritesService;
import com.mustcc.service.RateHistoryService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public final class CurrencyConverterGUI extends JFrame {

    private static final Color INK = new Color(24, 35, 43);
    private static final Color MUTED = new Color(100, 113, 123);
    private static final Color TEAL = new Color(0, 137, 137);
    private static final Color CORAL = new Color(224, 103, 79);
    private static final Color PALE_TEAL = new Color(229, 246, 243);
    private static final Color PAGE = new Color(242, 246, 247);
    private static final Color LINE = new Color(218, 226, 228);
    private static final Color WHITE = Color.WHITE;
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm");

    private final AuthService authService = new AuthService();
    private final ConversionService conversionService = new ConversionService();
    private final FavoritesService favoritesService = new FavoritesService();
    private final RateHistoryService rateHistoryService = new RateHistoryService();
    private final CardLayout rootCards = new CardLayout();
    private final JPanel root = new JPanel(rootCards);
    private final JLabel status = new JLabel("Ready");
    private final JLabel userLabel = new JLabel();
    private final JTextField loginUsername = new JTextField();
    private final JPasswordField loginPassword = new JPasswordField();
    private final JTextField registerUsername = new JTextField();
    private final JTextField registerEmail = new JTextField();
    private final JPasswordField registerPassword = new JPasswordField();
    private final JTextField fromField = new JTextField("USD");
    private final JTextField toField = new JTextField("UGX");
    private final JTextField amountField = new JTextField();
    private final JLabel conversionHeadline = new JLabel("Enter an amount to begin");
    private final JLabel conversionDetail = new JLabel("Your latest result will appear here.");
    private JTabbedPane appTabs;
    private final JTable historyTable = table("Time", "Pair", "Rate");
    private final JTable favoritesTable = table("Pair", "Added");
    private final JTable conversionTable = table("Date", "Pair", "Amount", "Received", "Rate");
    private User currentUser;
    private int currentSessionId = -1;

    private CurrencyConverterGUI() {
        super("Currency Converter");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setSize(1120, 760);
        setLocationRelativeTo(null);
        root.add(buildAuthView(), "auth");
        root.add(buildAppView(), "app");
        setContentPane(root);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                logoutAndClose();
            }
        });
        rootCards.show(root, "auth");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new CurrencyConverterGUI().setVisible(true);
        });
    }

    private JPanel buildAuthView() {
        JPanel page = new JPanel(new GridLayout(1, 2));
        page.setBackground(PAGE);

        JPanel brand = new JPanel(new GridBagLayout());
        brand.setBackground(INK);
        GridBagConstraints brandC = constraints(0, 0);
        brandC.insets = new Insets(0, 48, 0, 48);
        JPanel brandCopy = new BoxLayoutCompat(28);
        JLabel kicker = label("EXCHANGE DESK  /  01", 12, new Color(114, 224, 219));
        JLabel title = label("Move money\nwith clarity.", 42, WHITE);
        JLabel copy = label("Rates, fees, history, and favorites in one focused workspace.", 16, new Color(205, 220, 225));
        copy.setMaximumSize(new Dimension(360, 60));
        JLabel note = label("LIVE RATE WORKSPACE", 11, new Color(255, 166, 142));
        brandCopy.add(kicker);
        brandCopy.add(title);
        brandCopy.add(copy);
        brandCopy.add(spacer(18));
        brandCopy.add(note);
        brand.add(brandCopy, brandC);
        page.add(brand);

        JPanel formSide = new JPanel(new GridBagLayout());
        formSide.setBackground(PAGE);
        GridBagConstraints sideC = constraints(0, 0);
        sideC.fill = GridBagConstraints.HORIZONTAL;
        sideC.weightx = 1;
        sideC.insets = new Insets(0, 64, 0, 64);
        JPanel form = new BoxLayoutCompat(14, WHITE);
        JLabel welcome = label("Welcome back", 28, INK);
        JLabel subhead = label("Sign in to your exchange workspace, or continue as a guest.", 14, MUTED);
        form.add(welcome);
        form.add(subhead);
        form.add(spacer(12));
        form.add(label("USERNAME", 11, MUTED));
        form.add(loginUsername);
        form.add(label("PASSWORD", 11, MUTED));
        form.add(loginPassword);
        JButton login = primaryButton("Sign in");
        login.addActionListener(event -> login());
        form.add(login);
        JButton guest = linkButton("Continue as guest");
        guest.addActionListener(event -> enterApp(null));
        form.add(guest);
        form.add(spacer(10));
        form.add(label("NEW ACCOUNT", 11, CORAL));
        form.add(label("USERNAME", 11, MUTED));
        form.add(registerUsername);
        form.add(label("EMAIL", 11, MUTED));
        form.add(registerEmail);
        form.add(label("PASSWORD", 11, MUTED));
        form.add(registerPassword);
        JButton register = secondaryButton("Create account");
        register.addActionListener(event -> register());
        form.add(register);
        sideC.anchor = GridBagConstraints.CENTER;
        formSide.add(form, sideC);
        page.add(formSide);
        styleFields(loginUsername, loginPassword, registerUsername, registerEmail, registerPassword);
        return page;
    }

    private JPanel buildAppView() {
        JPanel page = new JPanel(new BorderLayout(0, 0));
        page.setBackground(PAGE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(24, 32, 20, 32));
        header.setBackground(INK);
        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 3));
        heading.setOpaque(false);
        heading.add(label("CURRENCY / CONVERTER", 18, WHITE));
        heading.add(label("Rates that make sense at a glance", 12, new Color(184, 202, 207)));
        header.add(heading, BorderLayout.WEST);
        JPanel account = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        account.setOpaque(false);
        userLabel.setForeground(new Color(205, 220, 225));
        account.add(userLabel);
        JButton logout = linkButton("Sign out");
        logout.setBackground(INK);
        logout.setForeground(new Color(255, 166, 142));
        logout.addActionListener(event -> logoutAndShowAuth());
        account.add(logout);
        header.add(account, BorderLayout.EAST);
        page.add(header, BorderLayout.NORTH);

        appTabs = new JTabbedPane();
        appTabs.setBorder(BorderFactory.createEmptyBorder(18, 24, 12, 24));
        appTabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        appTabs.setForeground(INK);
        appTabs.setBackground(PAGE);
        appTabs.addTab("Convert", buildConvertTab());
        appTabs.addTab("Rate history", buildRateHistoryTab());
        appTabs.addTab("Favorites", buildFavoritesTab());
        appTabs.addTab("My conversions", buildConversionHistoryTab());
        page.add(appTabs, BorderLayout.CENTER);

        status.setBorder(BorderFactory.createEmptyBorder(8, 30, 14, 30));
        status.setForeground(MUTED);
        page.add(status, BorderLayout.SOUTH);
        return page;
    }

    private JPanel buildConvertTab() {
        JPanel page = panel(new BorderLayout(18, 18));
        JPanel form = card(new GridBagLayout());
        GridBagConstraints c = constraints(0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(5, 5, 5, 5);
        form.add(label("FROM", 11, MUTED), c);
        c.gridx++;
        form.add(label("TO", 11, MUTED), c);
        c.gridx++;
        form.add(label("AMOUNT", 11, MUTED), c);
        c.gridx = 0;
        c.gridy++;
        styleFields(fromField, toField, amountField);
        form.add(fromField, c);
        c.gridx++;
        form.add(toField, c);
        c.gridx++;
        form.add(amountField, c);
        c.gridx++;
        c.weightx = 0;
        JButton swap = secondaryButton("Swap");
        swap.addActionListener(event -> {
            String value = fromField.getText();
            fromField.setText(toField.getText());
            toField.setText(value);
        });
        form.add(swap, c);
        page.add(form, BorderLayout.NORTH);

        JPanel result = new RoundedPanel(new GridBagLayout(), PALE_TEAL);
        result.setBorder(BorderFactory.createEmptyBorder(34, 38, 34, 38));
        GridBagConstraints resultC = constraints(0, 0);
        resultC.fill = GridBagConstraints.HORIZONTAL;
        resultC.weightx = 1;
        resultC.anchor = GridBagConstraints.WEST;
        resultC.insets = new Insets(4, 0, 4, 0);
        resultC.gridwidth = 2;
        result.add(label("LATEST RESULT  /  READY", 11, TEAL), resultC);
        resultC.gridy++;
        conversionHeadline.setForeground(INK);
        conversionHeadline.setFont(new Font("SansSerif", Font.BOLD, 30));
        result.add(conversionHeadline, resultC);
        resultC.gridy++;
        conversionDetail.setForeground(MUTED);
        result.add(conversionDetail, resultC);
        page.add(result, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton convert = primaryButton("Convert");
        convert.addActionListener(event -> convert(false));
        JButton fee = secondaryButton("Convert with fee");
        fee.addActionListener(event -> convert(true));
        actions.add(convert);
        actions.add(fee);
        page.add(actions, BorderLayout.SOUTH);
        return page;
    }

    private JPanel buildRateHistoryTab() {
        JPanel page = panel(new BorderLayout(12, 12));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);
        JTextField base = compactField("USD");
        JTextField target = compactField("UGX");
        JComboBox<Integer> days = new JComboBox<>(new Integer[]{7, 14, 30, 90});
        days.setFont(new Font("SansSerif", Font.PLAIN, 13));
        days.setBackground(WHITE);
        JButton load = primaryButton("Refresh history");
        load.addActionListener(event -> loadHistory(base, target, days));
        controls.add(label("PAIR", 11, MUTED));
        controls.add(base);
        controls.add(label("TO", 11, MUTED));
        controls.add(target);
        controls.add(label("DAYS", 11, MUTED));
        controls.add(days);
        controls.add(load);
        page.add(controls, BorderLayout.NORTH);
        page.add(tableScroll(historyTable), BorderLayout.CENTER);
        return page;
    }

    private JPanel buildFavoritesTab() {
        JPanel page = panel(new BorderLayout(12, 12));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);
        JTextField base = compactField("USD");
        JTextField target = compactField("UGX");
        JButton add = primaryButton("Add favorite");
        JButton remove = secondaryButton("Remove selected");
        JButton refresh = linkButton("Refresh");
        add.addActionListener(event -> addFavorite(base, target));
        remove.addActionListener(event -> removeFavorite());
        refresh.addActionListener(event -> loadFavorites());
        controls.add(label("PAIR", 11, MUTED));
        controls.add(base);
        controls.add(label("TO", 11, MUTED));
        controls.add(target);
        controls.add(add);
        controls.add(remove);
        controls.add(refresh);
        page.add(controls, BorderLayout.NORTH);
        page.add(tableScroll(favoritesTable), BorderLayout.CENTER);
        favoritesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return page;
    }

    private JPanel buildConversionHistoryTab() {
        JPanel page = panel(new BorderLayout(12, 12));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(label("Your ten most recent conversions", 14, MUTED), BorderLayout.WEST);
        JButton refresh = linkButton("Refresh");
        refresh.addActionListener(event -> loadConversions());
        top.add(refresh, BorderLayout.EAST);
        page.add(top, BorderLayout.NORTH);
        page.add(tableScroll(conversionTable), BorderLayout.CENTER);
        return page;
    }

    private void login() {
        String username = loginUsername.getText().trim();
        String password = new String(loginPassword.getPassword());
        if (username.isBlank() || password.isBlank()) {
            showError("Username and password are required.");
            return;
        }
        runAsync(() -> authService.login(username, password, "127.0.0.1"), result -> {
            currentUser = result.user;
            currentSessionId = result.sessionId;
            enterApp(currentUser);
        });
    }

    private void register() {
        String username = registerUsername.getText().trim();
        String email = registerEmail.getText().trim();
        String password = new String(registerPassword.getPassword());
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            showError("Username, email, and password are required.");
            return;
        }
        runAsync(() -> authService.register(username, email, password), user -> {
            loginUsername.setText(user.getUsername());
            loginPassword.setText("");
            showInfo("Account created. Sign in to continue.");
        });
    }

    private void enterApp(User user) {
        currentUser = user;
        userLabel.setText(user == null ? "Guest mode" : "Signed in as " + user.getUsername());
        appTabs.setEnabledAt(2, user != null);
        appTabs.setEnabledAt(3, user != null);
        appTabs.setSelectedIndex(0);
        rootCards.show(root, "app");
        status.setText("Ready");
        if (user != null) {
            loadFavorites();
            loadConversions();
        }
    }

    private void convert(boolean withFee) {
        String from = fromField.getText().trim().toUpperCase();
        String to = toField.getText().trim().toUpperCase();
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim());
            if (amount.signum() <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Enter an amount greater than zero.");
            return;
        }
        final BigDecimal requestedAmount = amount;
        if (withFee) {
            runAsync(() -> conversionService.convertWithFee(currentUser, from, to, requestedAmount), result -> {
                showConversion(result.getConversion(), result.getFeeApplied(), to);
                loadConversionsIfLoggedIn();
            });
        } else {
            runAsync(() -> conversionService.convert(currentUser, from, to, requestedAmount), result -> {
                showConversion(result, BigDecimal.ZERO, to);
                loadConversionsIfLoggedIn();
            });
        }
    }

    private void showConversion(Conversion conversion, BigDecimal fee, String target) {
        BigDecimal gross = conversion.getConvertedAmount();
        BigDecimal net = gross.subtract(fee);
        conversionHeadline.setText(AMOUNT_FORMAT.format(net) + " " + target);
        String detail = AMOUNT_FORMAT.format(conversion.getAmount()) + " " + conversion.getBase().getCurrencyCode()
                + " at " + conversion.getRateUsed().toPlainString();
        if (fee.signum() > 0) detail += "  |  fee " + AMOUNT_FORMAT.format(fee);
        conversionDetail.setText(detail);
        status.setText("Conversion completed");
    }

    private void loadHistory(JTextField base, JTextField target, JComboBox<Integer> days) {
        String from = base.getText().trim().toUpperCase();
        String to = target.getText().trim().toUpperCase();
        int range = (Integer) days.getSelectedItem();
        runAsync(() -> rateHistoryService.recentHistory(from, to, range), entries -> {
            clear(historyTable);
            for (RateHistoryEntry entry : entries) {
                ((DefaultTableModel) historyTable.getModel()).addRow(new Object[]{DATE_FORMAT.format(entry.getRecordedAt()),
                        entry.getBase().getCurrencyCode() + " -> " + entry.getTarget().getCurrencyCode(),
                        entry.getRate()});
            }
            status.setText(entries.size() + " history entr" + (entries.size() == 1 ? "y" : "ies") + " loaded");
        });
    }

    private void loadFavorites() {
        if (currentUser == null) return;
        runAsync(() -> favoritesService.listFavorites(currentUser), favorites -> {
            clear(favoritesTable);
            for (Favorite favorite : favorites) {
                ((DefaultTableModel) favoritesTable.getModel()).addRow(new Object[]{favorite.getPair().toString(), DATE_FORMAT.format(favorite.getAddedAt())});
            }
            status.setText(favorites.size() + " favorite" + (favorites.size() == 1 ? "" : "s") + " loaded");
        });
    }

    private void addFavorite(JTextField base, JTextField target) {
        String from = base.getText().trim().toUpperCase();
        String to = target.getText().trim().toUpperCase();
        runAsync(() -> {
            favoritesService.addFavorite(currentUser, from, to);
            return Boolean.TRUE;
        }, ignored -> loadFavorites());
    }

    private void removeFavorite() {
        int row = favoritesTable.getSelectedRow();
        if (row < 0) {
            showError("Select a favorite first.");
            return;
        }
        String pair = String.valueOf(favoritesTable.getValueAt(row, 0));
        String[] codes = pair.split(" -> ");
        runAsync(() -> {
            favoritesService.removeFavorite(currentUser, codes[0], codes[1]);
            return Boolean.TRUE;
        }, ignored -> loadFavorites());
    }

    private void loadConversionsIfLoggedIn() {
        if (currentUser != null) loadConversions();
    }

    private void loadConversions() {
        if (currentUser == null) return;
        runAsync(() -> conversionService.history(currentUser, 10), conversions -> {
            clear(conversionTable);
            for (Conversion conversion : conversions) {
                ((DefaultTableModel) conversionTable.getModel()).addRow(new Object[]{conversion.getConversionDate().toLocalDate(),
                        conversion.getBase().getCurrencyCode() + " -> " + conversion.getTarget().getCurrencyCode(),
                        conversion.getAmount(), conversion.getConvertedAmount(), conversion.getRateUsed()});
            }
        });
    }

    private <T> void runAsync(CheckedSupplier<T> action, Consumer<T> success) {
        status.setText("Working...");
        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() {
                try {
                    return action.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void done() {
                try {
                    success.accept(get());
                } catch (Exception e) {
                    showError(rootCause(e).getMessage());
                    status.setText("Action failed");
                }
            }
        };
        worker.execute();
    }

    private void logoutAndShowAuth() {
        if (currentSessionId != -1) {
            try {
                authService.logout(currentSessionId);
            } catch (DataAccessException e) {
                showError("Could not close the session: " + e.getMessage());
            }
        }
        currentUser = null;
        currentSessionId = -1;
        rootCards.show(root, "auth");
    }

    private void logoutAndClose() {
        if (currentSessionId != -1) {
            try {
                authService.logout(currentSessionId);
            } catch (DataAccessException ignored) {
            }
        }
        dispose();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message == null ? "The operation could not be completed." : message,
                "Could not complete action", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Currency Converter", JOptionPane.INFORMATION_MESSAGE);
    }

    private static Throwable rootCause(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        return current;
    }

    private static JTable table(String... columns) {
        JTable table = new JTable(new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setBackground(PAGE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        table.setSelectionBackground(PALE_TEAL);
        table.setSelectionForeground(INK);
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    private static void clear(JTable table) {
        ((DefaultTableModel) table.getModel()).setRowCount(0);
    }

    private static JScrollPane tableScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(WHITE);
        return scroll;
    }

    private static JPanel panel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 14, 20, 14));
        panel.setBackground(PAGE);
        return panel;
    }

    private static JPanel card(java.awt.LayoutManager layout) {
        return new RoundedPanel(layout, WHITE);
    }

    private static JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text.replace("\n", "<br>"));
        if (text.contains("\n")) label.setText("<html>" + text.replace("\n", "<br>") + "</html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    private static GridBagConstraints constraints(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(4, 4, 4, 4);
        return c;
    }

    private static Component spacer(int height) {
        return javax.swing.Box.createVerticalStrut(height);
    }

    private static JTextField compactField(String value) {
        JTextField field = new JTextField(value, 5);
        styleFields(field);
        return field;
    }

    private static void styleFields(JTextField... fields) {
        for (JTextField field : fields) {
            field.setFont(new Font("SansSerif", Font.PLAIN, 15));
            field.setPreferredSize(new Dimension(180, 40));
            field.setBackground(WHITE);
            field.setForeground(INK);
            field.setCaretColor(TEAL);
            field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        }
    }

    private static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(WHITE);
        button.setBackground(TEAL);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBorderPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return button;
    }

    private static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(TEAL);
        button.setBackground(WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(TEAL),
                BorderFactory.createEmptyBorder(9, 16, 9, 16)));
        return button;
    }

    private static JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(TEAL);
        button.setBackground(PAGE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private static final class BoxLayoutCompat extends JPanel {
        private final Color fill;

        private BoxLayoutCompat(int gap) {
            this(gap, null);
        }

        private BoxLayoutCompat(int gap, Color fill) {
            this.fill = fill;
            setOpaque(false);
            setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(28, 30, gap + 28, 30));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            if (fill != null) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(20, 30, 37, 18));
                g.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 8, 22, 22);
                g.setColor(fill);
                g.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 10, 22, 22);
                g.dispose();
            }
            super.paintComponent(graphics);
        }

        @Override
        public Component add(Component component) {
            component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
            return super.add(component);
        }
    }

    private static final class RoundedPanel extends JPanel {
        private final Color fill;

        private RoundedPanel(java.awt.LayoutManager layout, Color fill) {
            super(layout);
            this.fill = fill;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(20, 30, 37, 16));
            g.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 8, 20, 20);
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 10, 20, 20);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}