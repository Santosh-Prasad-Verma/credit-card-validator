#define CREDIT_CARD_VALIDATOR_NO_MAIN
#include "../../src/cpp/credit-card-validator.cpp"

#include <stdexcept>

void require(bool condition, const string& message) {
    if (!condition) {
        throw runtime_error(message);
    }
}

void assertValid(const string& input, const string& scheme, const string& maskedNumber, const string& lastFour) {
    ValidationResult result = validateCardNumber(input);

    require(result.valid, input + " should be valid: " + result.message);
    require(result.schemeName == scheme, input + " scheme mismatch: " + result.schemeName);
    require(result.maskedNumber == maskedNumber, input + " mask mismatch: " + result.maskedNumber);
    require(result.lastFour == lastFour, input + " last-four mismatch: " + result.lastFour);
}

void assertInvalid(const string& input, const string& reasonFragment) {
    ValidationResult result = validateCardNumber(input);

    string message = result.message;
    string fragment = reasonFragment;
    transform(message.begin(), message.end(), message.begin(), [](unsigned char ch) {
        return static_cast<char>(tolower(ch));
    });
    transform(fragment.begin(), fragment.end(), fragment.begin(), [](unsigned char ch) {
        return static_cast<char>(tolower(ch));
    });

    require(!result.valid, input + " should be invalid.");
    require(message.find(fragment) != string::npos, input + " reason mismatch: " + result.message);
    require(result.maskedNumber.empty(), input + " should not expose a masked number.");
    require(result.lastFour.empty(), input + " should not expose last four digits.");
}

int main() {
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

    cout << "All tests passed." << endl;
    return 0;
}
