# Defense guide

The application lets one person record income and spending, set budgets, apply monthly recurring transactions, and produce reports. Personal finance is familiar enough to explain clearly while providing useful examples for three design patterns. CSV keeps installation simple and the persisted state visible.

## Five concepts to understand

1. **Layered architecture:** UI collects input, services make decisions, repositories persist, immutable models describe data. `Main` connects them.
2. **Strategy:** report formatting changes by replacing the object implementing `ReportExportStrategy`; the context delegates unchanged.
3. **Adapter:** the simulated bank has incompatible names, types and date formats. `BankAdapter` converts them into the client's target interface without changing the bank.
4. **Decorator:** each notification wrapper implements `Notifier` and contains a `Notifier`; combining objects adds timestamp, priority and logging without changing the base console component.
5. **Correct persistent finance:** BigDecimal avoids binary rounding, CSV parsing preserves quoted data, and persistent external/occurrence IDs make imports and recurring retries idempotent.

## Instructor questions

| Question | Short answer |
|---|---|
| What does the application do? | It tracks one local user's income, expenses, budgets, monthly rules and financial reports in KZT. |
| Why Strategy here? | TXT, CSV and JSON have different formatting algorithms behind the same export operation. |
| Which class is the Strategy context? | `ReportExportService`; it holds a `ReportExportStrategy` and delegates `exportReport`. |
| How would you add HTML export? | Implement the strategy interface, pass the new object to the context, and add a UI choice. The context stays unchanged. |
| How is runtime switching demonstrated? | Option 9 sets a different concrete strategy each time within the same application session; a test also switches all three. |
| How does the Adapter work? | It calls `getBankOperations`, converts minor-unit longs to BigDecimal and strict bank dates to LocalDate, maps direction/categories and keeps operation IDs. |
| Is FakeBankService changed to match the app? | No. It has its own record and method and knows nothing about BankTransactionProvider. |
| Who uses the Adapter? | BankImportService depends on BankTransactionProvider, implemented by BankAdapter. |
| Difference between Adapter and Decorator? | Adapter translates an incompatible interface. Decorator keeps the same interface and adds responsibilities. |
| Can decorators be combined? | Yes. Each wraps a Notifier; priority, timestamp, logging and console objects form a chain. |
| What is printed and logged? | Timestamped normal events and timestamped important budget events. Formatting reaches console and log consistently. |
| Why not subclasses for every notification style? | Combinations would multiply subclasses; composition lets us assemble the needed behavior. |
| Why CSV instead of a database? | It meets local persistence needs with no server or runtime dependencies and makes the assignment easy to inspect. |
| What happens on restart? | Repositories validate/load existing files, due recurring rules are processed, then the menu opens. IDs continue from stored maxima. |
| How do you prevent duplicate bank imports? | Store each external ID with source BANK and check it again after reopening. Repositories also enforce uniqueness. |
| How do you prevent recurring duplicates? | Store the monthly marker plus an occurrence key `recurring:ruleId:month` on the generated transaction. |
| Why keep an occurrence key as well as the marker? | A crash after transaction save but before marker save must not insert that transaction again. |
| What happens to a rule scheduled on day 31? | Use the smaller of 31 and that month's length; February is clamped correctly, including leap years. |
| How are budget alerts repeated or suppressed? | Compare spending before/after an expense; emit only the highest newly crossed threshold. Stored totals reconstruct the state after restart. |
| Why does income not reduce budget spending? | BudgetService filters EXPENSE records by matching category and month. Balance is a separate calculation. |
| Why BigDecimal? | Decimal money remains exact; the program rejects more than two fractional digits and rounds percentages explicitly. |
| Why separate services from repositories? | Calculations and policy can be tested independently of console input, while file parsing stays in one layer. |
| How is invalid input handled? | Whole lines are parsed explicitly, domain constructors validate, and the UI catches validation/date/I/O exceptions. |
| What if CSV is corrupted? | Loading stops with path and logical record information; existing content is preserved rather than silently discarded. |
| How was it tested? | JUnit 5 functional/UI and independent pattern tests, plus real JAR workflows with temporary files and JSON parsing. See TESTING.md for actual results. |
| What are the limits? | One user/process, monthly recurrence, small CSV datasets, fixed mock bank dates, no encryption or database transactions. |
| How could it be extended? | Add a strategy/provider through its interface; replace repositories if larger datasets need a database, preserving service responsibilities. |

## Three-to-five-minute demonstration

Prepare the JAR and a fresh `--data-dir work/defense-N/data --export-dir work/defense-N/exports` before presenting. Keep the [full 21-step walkthrough](USER_GUIDE.md#full-reproducible-demonstration) nearby.

- **0:00–0:45:** Start, add salary 250000 and Food expense 35000 dated October 2026; show balance/history. Point out the CSV file.
- **0:45–1:30:** Set Food budget 50000, add Food 5000, show the 80% message and notification log. Explain the decorator chain.
- **1:30–2:15:** Generate October report; export TXT, CSV, JSON through option 9 in the same session. Explain context/interface/strategies.
- **2:15–3:00:** Import bank twice: six new, then six skipped. Explain adapter conversion and persistent external IDs.
- **3:00–3:45:** Create a Gift recurring income of 50 starting today; process twice, showing one then zero. Mention day-31 handling and occurrence keys.
- **3:45–4:30:** Exit and restart with the same arguments. Show saved transactions, budget and rule marker. Present Maven/JUnit results and the architecture diagram.

For a reset, choose another unused demo path. Do not delete or copy over personal financial files. The demonstration script and sample fixtures do not seed the default data directory.
