# Software design

## Layers and dependency direction

`Main` is the composition root: reads paths, constructs repositories, notifiers, services and the UI, then runs startup recurrence processing. Dependencies use constructors; there is no framework or static global dataset.

| Layer | Classes / responsibilities |
|---|---|
| Presentation | `ConsoleUI`: menus, `Scanner.nextLine()`, parsing and displaying errors/results; no financial arithmetic or CSV operations |
| Business | `TransactionService` validates/adds/filters/calculates balance and coordinates budgets; `BudgetService` upserts and computes spending/status/thresholds; `RecurringTransactionService` manages monthly rules; `ReportService` derives immutable reports; `BankImportService` imports normalized data; `ReportExportService` delegates formats; `NotificationService` selects normal/important pipelines and reads history |
| Data access | `TransactionRepository`, `BudgetRepository`, `RecurringTransactionRepository`: map domain data to CSV; package-private `CsvFile` validates headers/width and manages files; `Csv` encodes/parses and replaces files |
| Domain | Immutable Java 17 records `Transaction`, `Budget`, `BudgetStatus`, `RecurringTransaction`, `Report`, `BankImportRecord`; enums `Category`, `TransactionType`, `TransactionSource` |

UI → services → repositories → files. Models are shared by these layers without depending on UI or repositories. `BudgetService` reads repositories directly, so `TransactionService` can depend on it without a circular service dependency. Pattern objects are additional service collaborators. Concrete repositories keep this small project understandable; no empty repository interfaces are introduced.

## Strategy: report exporting

- Common interface: `ReportExportStrategy.export(Report, Path)`.
- Concrete strategies: `TxtExportStrategy`, `CsvExportStrategy`, `JsonExportStrategy`.
- Context: `ReportExportService` holds a strategy, supports `setStrategy`, and delegates `exportReport` after creating the output directory.
- Client: `ConsoleUI` selects strategy objects at runtime; choosing option 9 again can switch formats in the same process.

TXT uses the shared `ReportFormatter`; CSV calls the shared escaping codec; JSON emits numeric amounts using `toPlainString`. JSON category keys come only from controlled enum labels. The context contains no formatting branches. A fourth strategy implements the interface and is passed to the existing context; only UI registration is needed to expose another menu choice. This illustrates Open/Closed: the context stays unchanged. `StrategyPatternTest` switches all three objects through one context and passes an additional implementation.

## Adapter: simulated banking

- Target: `BankTransactionProvider.fetchTransactions()` returns `BankImportRecord` objects.
- Adaptee: `FakeBankService.getBankOperations()` returns `ExternalBankRecord` values with long minor-unit amounts, `dd/MM/uuuu` dates, CREDIT/DEBIT strings and external merchant groups.
- Adapter: `BankAdapter` implements the target and holds the adaptee. It maps direction, converts `BigDecimal.valueOf(minorUnits, 2)`, parses strict dates, maps/falls back categories and retains operation IDs.
- Client: `BankImportService` accepts only `BankTransactionProvider`, validates through `TransactionService`, skips existing external IDs and reports counts.

The original third-party-style class has no application imports and does not implement the target. The adapter has no persistence responsibility. This isolates external vocabulary and enables a different provider without changing the import workflow. `AdapterPatternTest` proves incompatibility, conversion, budget integration and duplicate prevention after reopening.

## Decorator: notifications

`Notifier` is the component interface; `ConsoleNotifier` is the concrete component. Abstract `NotifierDecorator` contains another `Notifier`. `TimestampNotifierDecorator`, `PriorityNotifierDecorator`, and `FileLoggingNotifierDecorator` each extend it and add one responsibility before delegating.

Normal pipeline: timestamp → logging → console. Important pipeline: priority → timestamp → logging → console. Since each prefix is added before delegation, important output is `[timestamp] [IMPORTANT] message`. The log and console receive the same formatted single-line message for normal UI events. Embedded newlines are escaped in the log to preserve one event per line. All service successes use the normal pipeline; budget crossings use the important one.

Composition supports choosing and combining responsibilities at runtime. An inheritance-only design would need separate subclasses for each combination. Logging errors go to the error stream and the message still reaches the console: a successful financial save must not look unsuccessful because logging failed. `DecoratorPatternTest` tests each responsibility and their combination without modifying `ConsoleNotifier`.

## Decisions and algorithms

- **Money:** positive `BigDecimal`, at most two fractional digits, normalized to scale 2; percentages use HALF_UP to two places. No binary floating point enters finance calculations.
- **Dates:** `LocalDate` and `YearMonth`, years 1–9999. UI blank dates use the supplied clock. Business recurrence takes `today` explicitly.
- **Budgets:** unique category/month; updates retain ID. Only expenses in that category and month count. Before/after spending detects 80%, exactly 100%, and above 100%; a single entry crossing several levels emits the highest newly crossed level. Later entries in the same region do not repeat warnings. Limits can be edited; current status always reflects the new limit.
- **Recurrence:** process all due months from start or last marker, clamp scheduled day to month length and stop before future due dates. Save a transaction with `recurring:<ruleId>:<YYYY-MM>` before advancing the marker. If interrupted between files, retry sees the occurrence ID and repairs the marker without another transaction.
- **Identity:** numeric max ID + 1 after reading files; repositories reject duplicate IDs and duplicate source/external-ID keys. No deletions or concurrent instances are supported.
- **Persistence:** read and validate complete tables, refuse malformed data, write sibling temporary file and replace. Per-file atomic move when supported, safe replacement fallback otherwise. There is no cross-file transaction or process locking.
- **Derived data:** balances, budget usage and reports are recomputed; exports are snapshots, never input sources.
- **Errors:** UI catches input/date/I/O errors, prints a useful message and continues; startup refuses corrupted data. Single-record commits are durable independently. After an interrupted bank import, retry skips saved external IDs.

## SOLID in this project

Single Responsibility separates input, calculations, persistence, format conversion and notifications. Open/Closed is visible in export strategies and decorator composition. Liskov substitution permits any strategy/provider/notifier implementation with its interface contract. Small interfaces provide Interface Segregation. Bank and notification clients depend on interfaces for Dependency Inversion; repositories deliberately remain simple concrete collaborators. Records encapsulate validated immutable state; constructors enforce invariants even outside the UI.

See the [class](diagrams/class-diagram.md) and [architecture](diagrams/architecture-diagram.md) diagrams.
