# 🛡️ SignalGate-MultiPoint: Final Architecture Draft

## 1. Introduction
SignalGate-MultiPoint is envisioned as a robust Android call-blocking application designed for privacy, performance, and user control. It operates as a transparent overlay, providing advanced call screening capabilities without replacing the user's default dialer. This document outlines the finalized frontend and backend architecture, addresses key development considerations, and reviews the provided checklist.

## 2. Frontend Architecture: The Shield
The frontend, termed "The Shield," focuses on a seamless, non-intrusive user experience, acting as an intelligent overlay rather than a full dialer replacement.

### 2.1. User Interface (UI) & Experience (UX)
*   **Transparent Overlay:** A frosted glass effect will appear over the incoming call screen, providing a modern, sleek aesthetic. This overlay will occupy the top 30% of the screen, ensuring visibility of the underlying call interface.
*   **Branding & Customization:** The **SignalGate-MultiPoint Logo** (a stylized shield with a digital pulse line) will be prominently displayed. Users can customize its glow intensity (Soft to Neon) and color (e.g., Cyber Green, Electric Blue, Warning Red) via **Options > Personalization**.
*   **Interactive Status Badge:** A green "Verified" or red "SignalGate Blocked" badge will indicate the call's status. Tapping this badge will reveal the "Source Tag" (e.g., "Source: Private-Crowd-A") for 10 seconds, fading in and out. A subtle pulse animation will hint at its interactivity.
*   **Quad-Action Buttons:** A 2x2 grid of semi-transparent, rounded buttons will offer immediate control during an incoming call:
    *   **Allow Now:** Permits the current call to pass through.
    *   **Allow Always:** Adds the number to the local Whitelist permanently.
    *   **Block Now:** Silences or rejects the current call.
    *   **Block Always:** Adds the number to the local Block-list permanently.

### 2.2. Onboarding & Permissions Wizard
Upon initial installation or significant updates, SignalGate-MultiPoint will guide the user through essential Android permissions:
*   **Step 1: Default Screening App:** The app will prompt the user to set SignalGate as the "Caller ID & Spam" app, which is crucial for `CallScreeningService` functionality.
*   **Step 2: Display Over Other Apps:** A visual, step-by-step guide (with screenshots) will instruct the user on how to grant the "Display over other apps" permission, enabling the transparent overlay.
*   **Step 3: Battery Optimization:** The app will request the user to "Ignore Battery Optimization" to ensure uninterrupted background syncs and call screening services.

### 2.3. UI Navigation (4-Tab Layout)
*   **🛡️ Protection:** Master switch for SignalGate, current protection status, and a summary of blocked calls.
*   **📋 Call Log:** A list of recent calls with one-tap "Block/Allow" options and a manual "Undo" feature for recent actions.
*   **🔗 Sources:** The "Multipoint Manager" for adding/removing local files and URL sources, with a manual "Sync Now" button.
*   **⚙️ Advanced:** Access to Benchmark Engine, Storage Analysis, Sync Schedule, Prefix/Regex Rules, and Diagnostic Logs.

## 3. Backend Architecture: The Hub
The backend, referred to as "The Hub," is designed for high-performance, privacy-centric data management, operating entirely on-device.

### 3.1. Data Management & Multipoint Hub
*   **Local-Only Database:** All blocking and allowing logic, along with data sources, reside exclusively on the device using a **Room Database** (SQLite abstraction).
*   **Multipoint Hub:** The app aggregates data from multiple sources (local CSV/XLSX files, remote URLs) into a single, optimized database.
    *   **Conflict Resolution:** A strict hierarchy ensures manual Allow-list entries always override Block-list entries, regardless of source.
    *   **Incremental Updates:** Only changed sources are re-imported, optimizing performance.
*   **Multipoint Feature Toggle:** During initial setup or via **Settings > Data Management**, users can enable/disable the Multipoint Hub. Disabling it will trigger a warning and subsequent cleanup of external source data.

### 3.2. Data Schema (SignalGate-Standard)
To ensure data integrity and compatibility, a 4-column standard is adopted:
| Column Name | Type | Description | Example |
| :--- | :--- | :--- | :--- |
| `phone_number` | String | The full number in E.164 format (e.g., `+18005550199`). |
| `action` | Enum | `BLOCK` or `ALLOW`. |
| `category` | String | Optional label (e.g., "Scam", "Political"). |
| `priority` | Integer | Optional, 1 (Low) to 5 (High), for advanced conflict resolution. |

### 3.3. Transcoding & Integrity Protection
*   **Sanitizer Engine:** All incoming data (from files or URLs) will be processed through a "Sanitizer" engine. This ensures:
    *   **UTF-8 Encoding:** All text is converted to UTF-8 to prevent character encoding issues.
    *   **Number Cleaning:** Only numeric characters and the leading `+` sign are retained for `phone_number` fields.
*   **Error Correction:** The Sanitizer will gracefully skip malformed lines in CSV/XLSX files, logging the error without crashing the import process.

### 3.4. Performance Benchmarking & Triple-Tier System
*   **Benchmark Engine:** Accessible via **Options > Advanced > Run Performance Test**, this tool simulates lookups to assess device performance.
*   **Triple-Tier Levels:**
    *   **🚀 Full-Throttle:** For high-end devices (6GB+ RAM), optimized for 500k+ rules. Hardware-locked; greyed out on insufficient hardware.
    *   **⚖️ Center-Point:** Balanced performance for most modern phones (100k rules).
    *   **📉 FPP (Fewest Points Possible):** Minimalist mode for older hardware (<10k rules), ensuring maximum speed.
*   **Auto-Throttling:** The app will suggest optimal tiers based on benchmark results and hardware detection, preventing users from overloading their devices.

### 3.5. Sync Engine & Error Handling
*   **Default Schedule:** Weekly background sync for URL sources, configurable in **Advanced > Sync**.
*   **Manual Sync:** A "Sync Now" button in the Sources Manager for on-demand updates.
*   **Fail-Safe Mechanism:** If a sync fails (e.g., network error, corrupted file), the app retains the last successfully synced data. It will never delete active protection due to a sync failure.

### 3.6. Security & Regression Testing
*   **Privacy by Design:** No user accounts, no cloud backend for sensitive data, ensuring 100% anonymity.
*   **Data Encapsulation:** All data stored in the app's private internal storage.
*   **SQL Injection Protection:** All data inputs are sanitized to prevent malicious code injection.
*   **Automated Regression:** Post-update, automated tests (Conflict Test, Whitelist Priority Test) will run. If failures occur, the app will **automatically roll back to the last stable database state** and notify the user with a diagnostic log.

### 3.7. Logging & Diagnostics
Accessible via **Options > Advanced > System Logs**, providing transparency and troubleshooting capabilities:
*   **🛡️ Protection Log:** Detailed history of call screening decisions.
*   **🔄 Sync Log:** Records of all sync operations, including errors.
*   **⚡ Performance Log:** Benchmark results and lookup latencies.
*   **🛠️ Regression Log:** Outcomes of automated logic tests.

## 4. Checklist Review
This section addresses the provided app development checklist, detailing how SignalGate-MultiPoint incorporates or modifies each point.

### Core Development Phases
*   **Define MVP scope and prioritize features:** **Addressed.** The current design is the MVP, focusing on core blocking, multipoint data, and performance. AI/ML heuristics are explicitly excluded.
*   **Choose tech stack (native vs cross-platform, backend architecture):** **Addressed.** Native Android (Kotlin/Jetpack Compose) for frontend, Room Database for local backend. No external backend server.
*   **Set up development environment and CI/CD pipeline:** **Addressed.** Standard Android development practices will be followed, including CI/CD for automated builds and testing.
*   **Implement authentication and security from day one:** **Modified.** Authentication is **skipped** due to the anonymous, local-only nature. Security focuses on data encapsulation, input sanitization, and robust error handling.
*   **Build modular architecture for scalability:** **Addressed.** The Multipoint Hub and Rule Engine are designed modularly for future feature expansion (e.g., new data sources, blocking methods).
*   **Create comprehensive API documentation:** **Modified.** Internal API documentation for developers will be created. External API documentation is not applicable as there's no public API.
*   **Implement automated testing (unit, integration, UI):** **Addressed.** Comprehensive testing, including unit, integration, UI, and specific stress/regression tests (Conflict Test, Whitelist Priority Test, Crash Test with 500k+ rows).
*   **Set up crash reporting and analytics:** **Addressed.** Local crash reporting and anonymized analytics (opt-in) will be implemented to monitor stability and performance without compromising privacy.
*   **Plan for app store compliance and metadata:** **Addressed.** This will be a critical step before launch, ensuring adherence to Google Play Store policies, especially regarding `CallScreeningService` usage and privacy.

### User-Centric Considerations
*   **Design for accessibility (WCAG compliance):** **Addressed.** UI elements will follow Android's accessibility guidelines (e.g., sufficient contrast, scalable text, talkback support).
*   **Implement proper data backup/restore functionality:** **Addressed.** The app will support Android's built-in backup services for app data, allowing users to restore their block-lists and settings.
*   **Plan for localization/internationalization if needed:** **Deferred.** Not in the initial MVP scope but designed for future integration (e.g., using Android string resources).
*   **Design for offline functionality and sync capabilities:** **Addressed.** Core blocking works entirely offline. Sync capabilities are for updating external lists, with robust offline fallback.
*   **Include user feedback mechanisms (ratings, surveys, support):** **Addressed.** In-app links to app store ratings, a simple feedback form, and support contact information will be included.
*   **Plan for regular updates and feature rollouts:** **Addressed.** The modular architecture supports this, and the automated regression testing ensures stability with updates.

### Business & Legal
*   **Draft privacy policy and terms of service:** **Addressed.** A clear and concise privacy policy will be drafted, emphasizing the app's local-only data handling and anonymity.

### Post-Launch
*   **Implement proper logging and error tracking:** **Addressed.** Detailed diagnostic logs are accessible to advanced users and for debugging.
*   **Plan for scalability and load testing:** **Addressed.** The Triple-Tier Benchmark and Crash Tests specifically address load testing on various hardware profiles.
*   **Set up beta testing channels (TestFlight, Google Play Beta):** **Addressed.** Standard beta testing channels will be utilized for pre-release validation.
*   **Plan for app store optimization (ASO):** **Addressed.** ASO strategies will be developed closer to launch to maximize visibility.
*   **Plan for regular security audits:** **Addressed.** Post-launch, regular security audits will be conducted, especially for data handling and permission usage.
*   **Implement feature flagging for gradual rollouts:** **Deferred.** Not in MVP, but can be integrated for future complex feature rollouts.
*   **Plan for app store updates and compliance checks:** **Addressed.** Continuous monitoring of Google Play policies and timely updates will be a priority.

## 5. Pre-Testing & Prediction Modeling
Before core development begins, a dedicated phase will focus on pre-testing and prediction modeling:
*   **Lookup Latency Simulation:** Simulate database lookups on various hardware profiles (emulators representing low-end, mid-range, high-end devices) to predict the actual `CallScreeningService` response times.
*   **Import Stress Testing:** Simulate importing extremely large files (e.g., 1 million rows) to identify potential memory bottlenecks and validate the Sanitizer Engine's performance.
*   **Performance Tier Validation:** Use these simulations to fine-tune the thresholds for the Full-Throttle, Center-Point, and FPP tiers, ensuring they accurately reflect real-world device capabilities.

This pre-testing phase will provide critical data to optimize the core engine before significant UI development, ensuring the app meets its performance and reliability goals from the outset.
