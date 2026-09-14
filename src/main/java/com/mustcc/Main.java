package com.mustcc;

import com.mustcc.exception.AuthException;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * Menu-driven CLI. Replaces the old single-shot "read 3 values, print 1
 * result, exit" version — every feature the service layer exposes now has
 * a menu path, and the user can stay logged in across multiple actions
 * instead of the program exiting after one conversion.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final AuthService authService = new AuthService();
    private static final ConversionService conversionService = new ConversionService();
    private static final FavoritesService favoritesService = new FavoritesService();
    private static final RateHistoryService rateHistoryService = new RateHistoryService();

    private static User currentUser = null;   // null = guest
    private static int currentSessionId = -1;

    public static void main(String[] args) {
        System.out.println("=== Currency Converter ===");
        try {
            authMenu();
            mainMenu();
        } finally {
            if (currentSessionId != -1) {
                authService.logout(currentSessionId);
            }
            scanner.close();
        }
        System.out.println("Goodbye.");
    }

    // ---------- Auth ----------

    private static void authMenu() {
        while (currentUser == null) {
            System.out.println("\n1) Login  2) Register  3) Continue as guest");
            switch (prompt("Choose: ")) {
                case "1" -> login();
                case "2" -> register();
                case "3" -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void login() {
        String username = prompt("Username: ");
        String password = prompt("Password: ");
        try {
            AuthService.LoginResult result = authService.login(username, password, "127.0.0.1");
            currentUser = result.user;
            currentSessionId = result.sessionId;
            System.out.println("Welcome back, " + currentUser.getUsername() + "!");
        } catch (AuthException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private static void register() {
        String username = prompt("Choose a username: ");
        String email = prompt("Email: ");
        String password = prompt("Choose a password: ");
        try {
            User newUser = authService.register(username, email, password);
            System.out.println("Registered " + newUser.getUsername() + ". Please log in.");
        } catch (AuthException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    // ---------- Main menu ----------

    private static void mainMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Menu (" + (currentUser != null ? currentUser.getUsername() : "guest") + ") ---");
            System.out.println("1) Convert currency");
            System.out.println("2) Convert currency (with fee)");
            System.out.println("3) View rate history (last 30 days)");
            if (currentUser != null) {
                System.out.println("4) My favorites");
                System.out.println("5) My conversion history");
            }
            System.out.println("0) Exit");

            switch (prompt("Choose: ")) {
                case "1" -> convert();
                case "2" -> convertWithFee();
                case "3" -> viewRateHistory();
                case "4" -> { if (currentUser != null) favoritesMenu(); else invalid(); }
                case "5" -> { if (currentUser != null) conversionHistory(); else invalid(); }
                case "0" -> running = false;
                default -> invalid();
            }
        }
    }

    private static void invalid() {
        System.out.println("Invalid choice.");
    }

    // ---------- Conversion ----------

    private static void convert() {
        String from = prompt("From (e.g. USD): ").toUpperCase();
        String to = prompt("To (e.g. UGX): ").toUpperCase();
        BigDecimal amount = promptAmount();

        try {
            Conversion result = conversionService.convert(currentUser, from, to, amount);
            System.out.println("Result: " + result);
        } catch (InvalidCurrencyException | RateUnavailableException e) {
            System.out.println("Conversion failed: " + e.getMessage());
        }
    }

    private static void convertWithFee() {
        String from = prompt("From (e.g. USD): ").toUpperCase();
        String to = prompt("To (e.g. UGX): ").toUpperCase();
        BigDecimal amount = promptAmount();

        try {
            ConversionResult result = conversionService.convertWithFee(currentUser, from, to, amount);
            System.out.println("Result: " + result);
            if (result.getFeeApplied().signum() > 0) {
                BigDecimal netReceived = result.getConversion().getConvertedAmount().subtract(result.getFeeApplied());
                System.out.println("You receive after fee: " + netReceived + " " + to);
            }
        } catch (InvalidCurrencyException | RateUnavailableException e) {
            System.out.println("Conversion failed: " + e.getMessage());
        }
    }

    private static BigDecimal promptAmount() {
        while (true) {
            try {
                return new BigDecimal(prompt("Amount: "));
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // ---------- Rate history ----------

    private static void viewRateHistory() {
        String from = prompt("From (e.g. USD): ").toUpperCase();
        String to = prompt("To (e.g. UGX): ").toUpperCase();
        try {
            List<RateHistoryEntry> entries = rateHistoryService.recentHistory(from, to, 30);
            if (entries.isEmpty()) {
                System.out.println("No history recorded yet for " + from + " -> " + to +
                        " (history builds up as conversions happen).");
            } else {
                entries.forEach(System.out::println);
            }
        } catch (InvalidCurrencyException e) {
            System.out.println("Lookup failed: " + e.getMessage());
        }
    }

    // ---------- Favorites ----------

    private static void favoritesMenu() {
        System.out.println("\n1) List favorites  2) Add favorite  3) Remove favorite  0) Back");
        switch (prompt("Choose: ")) {
            case "1" -> listFavorites();
            case "2" -> addFavorite();
            case "3" -> removeFavorite();
            case "0" -> { }
            default -> invalid();
        }
    }

    private static void listFavorites() {
        List<Favorite> favorites = favoritesService.listFavorites(currentUser);
        if (favorites.isEmpty()) {
            System.out.println("No favorites yet.");
        } else {
            favorites.forEach(System.out::println);
        }
    }

    private static void addFavorite() {
        String from = prompt("From (e.g. USD): ").toUpperCase();
        String to = prompt("To (e.g. UGX): ").toUpperCase();
        try {
            favoritesService.addFavorite(currentUser, from, to);
            System.out.println("Added " + from + " -> " + to + " to favorites.");
        } catch (InvalidCurrencyException e) {
            System.out.println("Failed: " + e.getMessage());
        }
    }

    private static void removeFavorite() {
        String from = prompt("From (e.g. USD): ").toUpperCase();
        String to = prompt("To (e.g. UGX): ").toUpperCase();
        try {
            favoritesService.removeFavorite(currentUser, from, to);
            System.out.println("Removed " + from + " -> " + to + " from favorites.");
        } catch (InvalidCurrencyException e) {
            System.out.println("Failed: " + e.getMessage());
        }
    }

    // ---------- Conversion history ----------

    private static void conversionHistory() {
        List<Conversion> history = conversionService.history(currentUser, 10);
        if (history.isEmpty()) {
            System.out.println("No conversions yet.");
        } else {
            history.forEach(System.out::println);
        }
    }

    // ---------- Helpers ----------

    private static String prompt(String label) {
        System.out.print(label);
        return scanner.nextLine().trim();
    }
}
