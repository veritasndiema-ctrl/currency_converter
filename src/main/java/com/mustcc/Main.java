package com.mustcc;

import com.mustcc.exception.InvalidCurrencyException;
import com.mustcc.exception.RateUnavailableException;
import com.mustcc.model.Conversion;
import com.mustcc.service.ConversionService;

import java.math.BigDecimal;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ConversionService service = new ConversionService();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Currency Converter ===");
        System.out.print("From (e.g. USD): ");
        String from = scanner.next().toUpperCase();
        System.out.print("To (e.g. UGX): ");
        String to = scanner.next().toUpperCase();
        System.out.print("Amount: ");
        BigDecimal amount = scanner.nextBigDecimal();

        try {
            Conversion result = service.convert(null, from, to, amount);
            System.out.println("Result: " + result);
        } catch (InvalidCurrencyException | RateUnavailableException e) {
            // Checked exceptions from the service layer are caught and
            // shown as a clean message instead of a stack trace.
            System.out.println("Conversion failed: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
