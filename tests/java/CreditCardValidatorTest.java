public class CreditCardValidatorTest {
    public static void main(String[] args) {
        assertValid("4111 1111 1111 1111", "Visa", "************1111", "1111");
        assertValid("5500-0000-0000-0004", "Mastercard", "************0004", "0004");
        assertValid("378282246310005", "American Express", "***********0005", "0005");
        assertValid("6011000990139424", "Discover", "************9424", "9424");
        assertValid("3530111333300000", "JCB", "************0000", "0000");
        assertValid("30569309025904", "Diners Club", "**********5904", "5904");
        assertValid("62123456789000003", "UnionPay", "*************0003", "0003");

        assertInvalid("", "empty");
        assertInvalid("4111111111111121", "Luhn");
        assertInvalid("4111abcd11111111", "digits");
        assertInvalid("0000000000000000", "prefix");

        System.out.println("All tests passed.");
    }

    private static void assertValid(String input, String scheme, String maskedNumber, String lastFour) {
        CreditCardValidator.ValidationResult result = CreditCardValidator.validateCardNumber(input);

        require(result.isValid(), input + " should be valid: " + result.getMessage());
        require(scheme.equals(result.getSchemeName()), input + " scheme mismatch: " + result.getSchemeName());
        require(maskedNumber.equals(result.getMaskedNumber()), input + " mask mismatch: " + result.getMaskedNumber());
        require(lastFour.equals(result.getLastFour()), input + " last-four mismatch: " + result.getLastFour());
    }

    private static void assertInvalid(String input, String reasonFragment) {
        CreditCardValidator.ValidationResult result = CreditCardValidator.validateCardNumber(input);

        require(!result.isValid(), input + " should be invalid.");
        require(result.getMessage().toLowerCase().contains(reasonFragment.toLowerCase()),
            input + " reason mismatch: " + result.getMessage());
        require(result.getMaskedNumber().isEmpty(), input + " should not expose a masked number.");
        require(result.getLastFour().isEmpty(), input + " should not expose last four digits.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
