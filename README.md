# Credit Card Validator

A Java command-line application that performs basic offline validation of payment card numbers.

It validates input format, card-network prefixes, supported lengths, and the Luhn checksum. Valid numbers are displayed in masked form so the full number is not returned in the result.

## Project Structure

```text
credit-card-validator/
├── .github/workflows/ci.yml
├──  screenshots/
├── src/
│   └── CreditCardValidator.java
├── tests/
│   └── CreditCardValidatorTest.java
├── build/
├── .gitignore
├── LICENSE
└── README.md
```

## Features

- Accepts digits, spaces, and hyphens
- Rejects empty or malformed input
- Supports card numbers from 12 to 19 digits
- Applies the Luhn checksum
- Detects common networks such as Visa, Mastercard, American Express, Discover, JCB, UnionPay, RuPay, Maestro, Diners Club, and Mir
- Returns only the masked number and last four digits

## Requirements

- Java JDK 8 or newer

## Build

```bash
mkdir -p build/java
javac -Xlint:all -d build/java src/CreditCardValidator.java
```

## Run

```bash
java -cp build/java CreditCardValidator
```

Type a card number and press Enter. Type `exit` to quit.

## Test

```bash
javac -Xlint:all -cp build/java -d build/java tests/CreditCardValidatorTest.java
java -cp build/java CreditCardValidatorTest
```

## Public Test Numbers

Use only public test numbers:

| Network | Number |
| --- | --- |
| Visa | `4111111111111111` |
| Mastercard | `5500000000000004` |
| American Express | `378282246310005` |
| Discover | `6011000990139424` |
| JCB | `3530111333300000` |

## Important Limitation

This application does not confirm that a card exists, is active, has available funds, or belongs to a particular customer. It is an offline validation utility, not a payment processor.

For production payments, collect and tokenize card details through a PCI-compliant provider such as Stripe, Adyen, Braintree, Razorpay, or another approved gateway. Never log or store complete card numbers.

## License

Licensed under the [MIT License](LICENSE).
