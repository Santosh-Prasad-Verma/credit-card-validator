#include <algorithm>
#include <cctype>
#include <iostream>
#include <string>
#include <vector>

using namespace std;

const int MIN_PAN_LENGTH = 12;
const int MAX_PAN_LENGTH = 19;

struct BinRange {
    int prefixLength;
    int start;
    int end;

    bool matches(const string& number) const {
        if (number.length() < static_cast<size_t>(prefixLength)) {
            return false;
        }

        int prefix = stoi(number.substr(0, prefixLength));
        return prefix >= start && prefix <= end;
    }
};

struct CardScheme {
    string name;
    vector<int> supportedLengths;
    vector<string> prefixes;
    vector<BinRange> ranges;

    bool supportsLength(int length) const {
        return find(supportedLengths.begin(), supportedLengths.end(), length) != supportedLengths.end();
    }

    int matchSpecificity(const string& number) const {
        int bestSpecificity = 0;

        for (const string& prefix : prefixes) {
            if (number.rfind(prefix, 0) == 0) {
                bestSpecificity = max(bestSpecificity, static_cast<int>(prefix.length()));
            }
        }

        for (const BinRange& range : ranges) {
            if (range.matches(number)) {
                bestSpecificity = max(bestSpecificity, range.prefixLength);
            }
        }

        return bestSpecificity;
    }
};

struct ValidationResult {
    bool valid;
    string maskedNumber;
    string lastFour;
    string schemeName;
    string message;
};

struct NormalizedInput {
    bool valid;
    string number;
    string message;
};

const vector<CardScheme> CARD_SCHEMES = {
    {"American Express", {15}, {"34", "37"}, {}},
    {"Visa", {13, 14, 15, 16, 17, 18, 19}, {"4"}, {}},
    {"Mastercard", {16}, {}, {{2, 51, 55}, {6, 222100, 272099}}},
    {"Discover", {16, 17, 18, 19}, {"6011", "65"}, {{3, 644, 649}, {6, 622126, 622925}}},
    {"Diners Club", {14, 16, 17, 18, 19}, {"36", "309"}, {{3, 300, 305}, {2, 38, 39}}},
    {"JCB", {16, 17, 18, 19}, {}, {{4, 3528, 3589}}},
    {"UnionPay", {16, 17, 18, 19}, {"62"}, {}},
    {"RuPay", {16}, {"60", "65", "81", "82", "353", "356", "508"}, {}},
    {"Maestro", {12, 13, 14, 15, 16, 17, 18, 19},
        {"5018", "5020", "5038", "5893", "6304", "6759", "6761", "6762", "6763", "676770", "676774"}, {}},
    {"Mir", {16, 17, 18, 19}, {}, {{4, 2200, 2204}}}
};

string trim(const string& value) {
    size_t first = 0;
    while (first < value.length() && isspace(static_cast<unsigned char>(value[first]))) {
        first++;
    }

    size_t last = value.length();
    while (last > first && isspace(static_cast<unsigned char>(value[last - 1]))) {
        last--;
    }

    return value.substr(first, last - first);
}

string toLower(const string& value) {
    string lowered = value;
    for (char& ch : lowered) {
        ch = static_cast<char>(tolower(static_cast<unsigned char>(ch)));
    }
    return lowered;
}

NormalizedInput normalize(const string& input) {
    if (trim(input).empty()) {
        return {false, "", "Card number is empty."};
    }

    string digits;
    for (char ch : input) {
        unsigned char current = static_cast<unsigned char>(ch);

        if (isdigit(current)) {
            digits += ch;
        } else if (ch == '-' || isspace(current)) {
            continue;
        } else {
            return {false, "", "Only digits, spaces, and hyphens are allowed."};
        }
    }

    if (digits.empty()) {
        return {false, "", "Card number is empty."};
    }

    return {true, digits, ""};
}

vector<CardScheme> findPrefixMatches(const string& number) {
    vector<CardScheme> matches;
    int bestSpecificity = 0;

    for (const CardScheme& scheme : CARD_SCHEMES) {
        int specificity = scheme.matchSpecificity(number);

        if (specificity > bestSpecificity) {
            matches.clear();
            matches.push_back(scheme);
            bestSpecificity = specificity;
        } else if (specificity == bestSpecificity && specificity > 0) {
            matches.push_back(scheme);
        }
    }

    return matches;
}

vector<CardScheme> filterByLength(const vector<CardScheme>& schemes, int length) {
    vector<CardScheme> matches;

    for (const CardScheme& scheme : schemes) {
        if (scheme.supportsLength(length)) {
            matches.push_back(scheme);
        }
    }

    return matches;
}

bool passesLuhnCheck(const string& number) {
    int sum = 0;
    bool shouldDouble = false;

    for (int i = static_cast<int>(number.length()) - 1; i >= 0; i--) {
        int digit = number[i] - '0';

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

string joinSchemeNames(const vector<CardScheme>& schemes) {
    string joined;

    for (size_t i = 0; i < schemes.size(); i++) {
        if (i > 0) {
            joined += " / ";
        }
        joined += schemes[i].name;
    }

    return joined;
}

string maskCardNumber(const string& number) {
    size_t visibleDigits = min<size_t>(4, number.length());
    string masked(number.length() - visibleDigits, '*');
    masked += number.substr(number.length() - visibleDigits);
    return masked;
}

string getLastFour(const string& number) {
    size_t visibleDigits = min<size_t>(4, number.length());
    return number.substr(number.length() - visibleDigits);
}

ValidationResult validateCardNumber(const string& input) {
    NormalizedInput normalizedInput = normalize(input);

    if (!normalizedInput.valid) {
        return {false, "", "", "", normalizedInput.message};
    }

    string number = normalizedInput.number;
    int length = static_cast<int>(number.length());

    if (length < MIN_PAN_LENGTH || length > MAX_PAN_LENGTH) {
        return {false, "", "", "", "Card numbers must contain 12 to 19 digits after removing spaces and hyphens."};
    }

    vector<CardScheme> prefixMatches = findPrefixMatches(number);
    if (prefixMatches.empty()) {
        return {false, "", "", "", "Unsupported or unknown card network prefix."};
    }

    vector<CardScheme> lengthMatches = filterByLength(prefixMatches, length);
    if (lengthMatches.empty()) {
        return {false, "", "", "", "The prefix looks like " + joinSchemeNames(prefixMatches)
            + ", but that network does not use " + to_string(length) + "-digit card numbers."};
    }

    if (!passesLuhnCheck(number)) {
        return {false, "", "", "", "The number fails the Luhn checksum."};
    }

    return {true, maskCardNumber(number), getLastFour(number), joinSchemeNames(lengthMatches), "Valid card number."};
}

#ifndef CREDIT_CARD_VALIDATOR_NO_MAIN
int main() {
    cout << "Credit Card Validator" << endl;
    cout << "Accepts digits, spaces, or hyphens. Type 'exit' to quit." << endl;
    cout << "Offline validation checks format, network, and Luhn checksum only." << endl;

    string input;
    while (true) {
        cout << "\nEnter card number: ";

        if (!getline(cin, input)) {
            break;
        }

        if (toLower(trim(input)) == "exit") {
            break;
        }

        ValidationResult result = validateCardNumber(input);

        if (result.valid) {
            cout << "Result: Valid " << result.schemeName << " card number." << endl;
            cout << "Masked: " << result.maskedNumber << endl;
        } else {
            cout << "Result: Invalid card number." << endl;
            cout << "Reason: " << result.message << endl;
        }
    }

    cout << "\nGoodbye!" << endl;
    return 0;
}
#endif
