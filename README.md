Gratuit-inator

An ad-free Android tip calculator with tax-aware calculation, one-tap rounding to an even dollar amount, and receipt photo scanning.

Features
Separate Subtotal and Tax entry — tip is calculated on the food/drink amount, not tax
"Use full bill amount" checkbox to tip on the whole receipt total instead, when you don't want the split
Round the total to an even dollar amount with one tap; fine-tune from there in $1 steps
Tip % slider for a fast starting point, independent of any rounding you've done
Split evenly across a party (subtotal, tax, and tip all divided)
Scan a receipt with the camera to pre-fill Subtotal and Tax (fully on-device — nothing leaves your phone)
No ads
Requirements

See tip-calculator-requirements.md for the full feature spec and the reasoning behind each design decision.

Tech stack
Kotlin + Jetpack Compose
Android SDK / Gradle
On-device text recognition (ML Kit) for receipt scanning
Building
Install a JDK (17+) and the Android SDK command-line tools.
Clone the repo and open it in your editor of choice.
./gradlew assembleDebug to build, or ./gradlew installDebug with a device connected via USB debugging to install directly.
Support

If you find this useful, there's a Buy Me a Coffee link in the app — totally optional.

License

TBD
