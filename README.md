# Credit Card Validator

A Java and C++ command-line credit card number validator.

This project performs offline validation only. It checks whether a card number has a valid format, known card-network prefix, supported length, and Luhn checksum. It cannot prove that a card exists, is active, belongs to a real customer, or has available funds.

## Project Structure

```text
credit-card-validator/
├── .github/workflows/ci.yml
├── .vscode/settings.json
├── src/
│   ├── cpp/
│   │   └── credit-card-validator.cpp
│   └── java/
│       └── CreditCardValidator.java
├── tests/
│   ├── cpp/
│   │   └── credit-card-validator-test.cpp
│   └── java/
│       └── CreditCardValidatorTest.java
├── .gitignore
├── LICENSE
└── README.md
```

## Features

- Accepts digits, spaces, and hyphens
- Rejects empty input and unsupported characters
- Normalizes input before validation
- Checks card number length from 12 to 19 digits
- Detects common card networks:
  - American Express
  - Visa
  - Mastercard
  - Discover
  - Diners Club
  - JCB
  - UnionPay
  - RuPay
  - Maestro
  - Mir
- Uses the Luhn checksum
- Masks valid card numbers in output
- Returns only masked number and last four digits from validation results

## Requirements

- Java JDK 8 or newer
- A C++17 compiler such as `g++`

## Run

### Java

```bash
mkdir -p build/java
javac -d build/java src/java/CreditCardValidator.java
java -cp build/java CreditCardValidator
```

### C++

```bash
mkdir -p build/cpp
g++ -std=c++17 -Wall -Wextra -pedantic src/cpp/credit-card-validator.cpp -o build/cpp/validator
./build/cpp/validator
```

## Test

### Java

```bash
mkdir -p build/java
javac -Xlint:all -d build/java src/java/CreditCardValidator.java tests/java/CreditCardValidatorTest.java
java -cp build/java CreditCardValidatorTest
```

### C++

```bash
mkdir -p build/cpp
g++ -std=c++17 -Wall -Wextra -pedantic tests/cpp/credit-card-validator-test.cpp -o build/cpp/validator_test
./build/cpp/validator_test
```

## Usage

Type a card number and press Enter. Type `exit` to quit.

Input examples:

```text
4111 1111 1111 1111
5500-0000-0000-0004
378282246310005
```

Example output:

```text
Result: Valid Visa card number.
Masked: ************1111
```

## Test Numbers

These are public test numbers, not real payment cards:

| Network | Number |
| --- | --- |
| Visa | `4111111111111111` |
| Mastercard | `5500000000000004` |
| American Express | `378282246310005` |
| Discover | `6011000990139424` |
| JCB | `3530111333300000` |
| Diners Club | `30569309025904` |
| UnionPay | `62123456789000003` |

## GitHub CI

The workflow in `.github/workflows/ci.yml` builds and tests both implementations on every push and pull request.

## Validation Limits

Real card validation requires a payment processor, card issuer, or up-to-date BIN/IIN database. Network prefix ranges can change over time, so this project is best used for learning, form validation, and basic offline checks.

For production payment flows, use this only as a local pre-check before tokenizing or authorizing the card through a PCI-compliant payment provider. Do not log, persist, or expose full card numbers.
