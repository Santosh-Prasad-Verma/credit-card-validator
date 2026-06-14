import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CreditCardValidator {
    private static final int MIN_PAN_LENGTH = 12;
    private static final int MAX_PAN_LENGTH = 19;

    private static final List<CardScheme> CARD_SCHEMES = Arrays.asList(
        new CardScheme("American Express", lengths(15), prefixes("34", "37")),
        new CardScheme("Visa", lengths(13, 14, 15, 16, 17, 18, 19), prefixes("4")),
        new CardScheme("Mastercard", lengths(16), prefixes(), range(2, 51, 55), range(6, 222100, 272099)),
        new CardScheme("Discover", lengths(16, 17, 18, 19), prefixes("6011", "65"),
            range(3, 644, 649), range(6, 622126, 622925)),
        new CardScheme("Diners Club", lengths(14, 16, 17, 18, 19), prefixes("36", "309"),
            range(3, 300, 305), range(2, 38, 39)),
        new CardScheme("JCB", lengths(16, 17, 18, 19), prefixes(), range(4, 3528, 3589)),
        new CardScheme("UnionPay", lengths(16, 17, 18, 19), prefixes("62")),
        new CardScheme("RuPay", lengths(16), prefixes("60", "65", "81", "82", "353", "356", "508")),
        new CardScheme("Maestro", lengths(12, 13, 14, 15, 16, 17, 18, 19),
            prefixes("5018", "5020", "5038", "5893", "6304", "6759", "6761", "6762", "6763", "676770", "676774")),
        new CardScheme("Mir", lengths(16, 17, 18, 19), prefixes(), range(4, 2200, 2204))
    );

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Credit Card Validator");
            System.out.println("Accepts digits, spaces, or hyphens. Type 'exit' to quit.");
            System.out.println("Offline validation checks format, network, and Luhn checksum only.");

            while (true) {
                System.out.print("\nEnter card number: ");
                String input = scanner.nextLine();

                if (input.trim().equalsIgnoreCase("exit")) {
                    break;
                }

                ValidationResult result = validateCardNumber(input);

                if (result.isValid()) {
                    System.out.println("Result: Valid " + result.getSchemeName() + " card number.");
                    System.out.println("Masked: " + result.getMaskedNumber());
                } else {
                    System.out.println("Result: Invalid card number.");
                    System.out.println("Reason: " + result.getMessage());
                }
            }
        }

        System.out.println("\nGoodbye!");
    }

    public static ValidationResult validateCardNumber(String input) {
        NormalizedInput normalizedInput = normalize(input);

        if (!normalizedInput.isValid()) {
            return ValidationResult.invalid(normalizedInput.getMessage());
        }

        String number = normalizedInput.getNumber();
        int length = number.length();

        if (length < MIN_PAN_LENGTH || length > MAX_PAN_LENGTH) {
            return ValidationResult.invalid("Card numbers must contain " + MIN_PAN_LENGTH + " to "
                + MAX_PAN_LENGTH + " digits after removing spaces and hyphens.");
        }

        List<CardScheme> prefixMatches = findPrefixMatches(number);
        if (prefixMatches.isEmpty()) {
            return ValidationResult.invalid("Unsupported or unknown card network prefix.");
        }

        List<CardScheme> lengthMatches = filterByLength(prefixMatches, length);
        if (lengthMatches.isEmpty()) {
            return ValidationResult.invalid("The prefix looks like " + joinSchemeNames(prefixMatches)
                + ", but that network does not use " + length + "-digit card numbers.");
        }

        if (!passesLuhnCheck(number)) {
            return ValidationResult.invalid("The number fails the Luhn checksum.");
        }

        return ValidationResult.valid(number, joinSchemeNames(lengthMatches));
    }

    private static NormalizedInput normalize(String input) {
        if (input == null || input.trim().isEmpty()) {
            return NormalizedInput.invalid("Card number is empty.");
        }

        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            if (ch >= '0' && ch <= '9') {
                digits.append(ch);
            } else if (ch == '-' || Character.isWhitespace(ch)) {
                continue;
            } else {
                return NormalizedInput.invalid("Only digits, spaces, and hyphens are allowed.");
            }
        }

        if (digits.length() == 0) {
            return NormalizedInput.invalid("Card number is empty.");
        }

        return NormalizedInput.valid(digits.toString());
    }

    private static List<CardScheme> findPrefixMatches(String number) {
        List<CardScheme> matches = new ArrayList<>();
        int bestSpecificity = 0;

        for (CardScheme scheme : CARD_SCHEMES) {
            int specificity = scheme.matchSpecificity(number);

            if (specificity > bestSpecificity) {
                matches.clear();
                matches.add(scheme);
                bestSpecificity = specificity;
            } else if (specificity == bestSpecificity && specificity > 0) {
                matches.add(scheme);
            }
        }

        return matches;
    }

    private static List<CardScheme> filterByLength(List<CardScheme> schemes, int length) {
        List<CardScheme> matches = new ArrayList<>();

        for (CardScheme scheme : schemes) {
            if (scheme.supportsLength(length)) {
                matches.add(scheme);
            }
        }

        return matches;
    }

    private static boolean passesLuhnCheck(String number) {
        int sum = 0;
        boolean shouldDouble = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';

            if (shouldDouble) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            shouldDouble = !shouldDouble;
        }

        return sum % 10 == 0;
    }

    private static String maskCardNumber(String number) {
        int visibleDigits = Math.min(4, number.length());
        String lastDigits = number.substring(number.length() - visibleDigits);
        StringBuilder masked = new StringBuilder();

        for (int i = 0; i < number.length() - visibleDigits; i++) {
            masked.append('*');
        }

        masked.append(lastDigits);
        return masked.toString();
    }

    private static String joinSchemeNames(List<CardScheme> schemes) {
        StringBuilder joined = new StringBuilder();

        for (int i = 0; i < schemes.size(); i++) {
            if (i > 0) {
                joined.append(" / ");
            }
            joined.append(schemes.get(i).getName());
        }

        return joined.toString();
    }

    private static int[] lengths(int... values) {
        return values;
    }

    private static String[] prefixes(String... values) {
        return values;
    }

    private static BinRange range(int prefixLength, int start, int end) {
        return new BinRange(prefixLength, start, end);
    }

    public static final class ValidationResult {
        private final boolean valid;
        private final String maskedNumber;
        private final String lastFour;
        private final String schemeName;
        private final String message;

        private ValidationResult(boolean valid, String maskedNumber, String lastFour, String schemeName, String message) {
            this.valid = valid;
            this.maskedNumber = maskedNumber;
            this.lastFour = lastFour;
            this.schemeName = schemeName;
            this.message = message;
        }

        static ValidationResult valid(String normalizedNumber, String schemeName) {
            int visibleDigits = Math.min(4, normalizedNumber.length());
            String lastFour = normalizedNumber.substring(normalizedNumber.length() - visibleDigits);
            return new ValidationResult(true, maskCardNumber(normalizedNumber), lastFour, schemeName, "Valid card number.");
        }

        static ValidationResult invalid(String message) {
            return new ValidationResult(false, "", "", "", message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMaskedNumber() {
            return maskedNumber;
        }

        public String getLastFour() {
            return lastFour;
        }

        public String getSchemeName() {
            return schemeName;
        }

        public String getMessage() {
            return message;
        }
    }

    private static final class NormalizedInput {
        private final boolean valid;
        private final String number;
        private final String message;

        private NormalizedInput(boolean valid, String number, String message) {
            this.valid = valid;
            this.number = number;
            this.message = message;
        }

        static NormalizedInput valid(String number) {
            return new NormalizedInput(true, number, "");
        }

        static NormalizedInput invalid(String message) {
            return new NormalizedInput(false, "", message);
        }

        boolean isValid() {
            return valid;
        }

        String getNumber() {
            return number;
        }

        String getMessage() {
            return message;
        }
    }

    private static final class CardScheme {
        private final String name;
        private final int[] supportedLengths;
        private final String[] prefixes;
        private final BinRange[] ranges;

        CardScheme(String name, int[] supportedLengths, String[] prefixes, BinRange... ranges) {
            this.name = name;
            this.supportedLengths = supportedLengths;
            this.prefixes = prefixes;
            this.ranges = ranges;
        }

        String getName() {
            return name;
        }

        boolean supportsLength(int length) {
            for (int supportedLength : supportedLengths) {
                if (supportedLength == length) {
                    return true;
                }
            }

            return false;
        }

        int matchSpecificity(String number) {
            int bestSpecificity = 0;

            for (String prefix : prefixes) {
                if (number.startsWith(prefix)) {
                    bestSpecificity = Math.max(bestSpecificity, prefix.length());
                }
            }

            for (BinRange range : ranges) {
                if (range.matches(number)) {
                    bestSpecificity = Math.max(bestSpecificity, range.getPrefixLength());
                }
            }

            return bestSpecificity;
        }
    }

    private static final class BinRange {
        private final int prefixLength;
        private final int start;
        private final int end;

        BinRange(int prefixLength, int start, int end) {
            this.prefixLength = prefixLength;
            this.start = start;
            this.end = end;
        }

        int getPrefixLength() {
            return prefixLength;
        }

        boolean matches(String number) {
            if (number.length() < prefixLength) {
                return false;
            }

            int prefix = Integer.parseInt(number.substring(0, prefixLength));
            return prefix >= start && prefix <= end;
        }
    }
}
