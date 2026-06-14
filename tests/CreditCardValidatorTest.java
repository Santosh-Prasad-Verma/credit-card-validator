public class CreditCardValidatorTest {
    private static int assertions;

    public static void main(String[] args) {
        assertValid("4169161461575762", "Visa", 16, "************5762", "5762");
        assertValid("5500-0000-0000-0004", "Mastercard", 16, "************0004", "0004");
        assertValid("378282246310005", "American Express", 15, "***********0005", "0005");
        assertValid("6011000990139424", "Discover", 16, "************9424", "9424");
        assertValid("3530111333300000", "JCB", 16, "************0000", "0000");

        assertInvalid("", "empty", 0, false);
        assertInvalid("4111111111111121", "Luhn", 16, true);
        assertInvalid("4111abcd11111111", "digits", 0, false);
        assertInvalid("0000000000000000", "prefix", 16, false);

        System.out.println("All " + assertions + " assertions passed.");
    }

    private static void assertValid(
        String input,
        String scheme,
        int length,
        String maskedNumber,
        String lastFour
    ) {
        CreditCardValidator.ValidationResult result = CreditCardValidator.validateCardNumber(input);

        require(result.isValid(), input + " should be valid: " + result.getMessage());
        require(scheme.equals(result.getSchemeName()), input + " network mismatch.");
        require(length == result.getCardLength(), input + " length mismatch.");
        require(result.isLuhnChecked(), input + " should run Luhn.");
        require(result.isLuhnPassed(), input + " should pass Luhn.");
        require(maskedNumber.equals(result.getMaskedNumber()), input + " mask mismatch.");
        require(lastFour.equals(result.getLastFour()), input + " last-four mismatch.");
    }

    private static void assertInvalid(String input, String reason, int length, boolean luhnChecked) {
        CreditCardValidator.ValidationResult result = CreditCardValidator.validateCardNumber(input);

        require(!result.isValid(), input + " should be invalid.");
        require(result.getMessage().toLowerCase().contains(reason.toLowerCase()), input + " reason mismatch.");
        require(length == result.getCardLength(), input + " length mismatch.");
        require(result.isLuhnChecked() == luhnChecked, input + " Luhn state mismatch.");
        require(!result.isLuhnPassed(), input + " must not pass Luhn.");
    }

    private static void require(boolean condition, String message) {
        assertions++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
