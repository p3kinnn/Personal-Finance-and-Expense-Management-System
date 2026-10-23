# Personal Finance and Expense Management System

Option E university project for **Software Architecture and Design Patterns**. A single local user records income and expenses in KZT, tracks monthly budgets, processes monthly recurring transactions, imports a simulated bank feed, and exports monthly reports.

## Requirements and installation

Install **JDK 17** (a JRE alone cannot compile) and **Maven 3.8+**. No database, API keys, runtime libraries, or external services are needed. Maven downloads build plugins and JUnit 5 on the first build; subsequent builds can use the local cache.

```sh
git clone https://github.com/p3kinnn/Personal-Finance-and-Expense-Management-System.git
cd Personal-Finance-and-Expense-Management-System
mvn clean package
java -jar target/personal-finance-manager-1.0.0.jar
```

Open `pom.xml` in IntelliJ IDEA Community Edition as a Maven project, select JDK 17, let Maven import, then run `com.personalfinance.app.Main`. Set the working directory to the repository root. Alternatively run `package` in the Maven tool window and use the JAR command above. `scripts/run.sh` and `scripts/run.bat` start a previously built JAR.

## Features

- Positive two-decimal `BigDecimal` income and expenses, centralized typed categories, IDs, history sorted by date/ID, ID lookup, category/month/type filters, and balance.
- Monthly expense budgets with update-in-place, spent/remaining/usage, and threshold-crossing alerts at 80%, 100%, and above 100%.
- Monthly recurring income/expenses, disable/list/process controls, startup checks, month-end clamping, catch-up, and persistent duplicate prevention.
- Monthly income/expense totals, category breakdowns, balance and count, with TXT, CSV and JSON exports selected at runtime.
- Six stable October 2026 fake bank operations with persistent external IDs; repeat imports are skipped.
- Console notifications with composable timestamps, priority and persistent logging.
- UTF-8 CSV persistence, missing-file initialization, strict corruption errors and temporary-file replacement.

## Design and structure

Java 17, Maven, JUnit 5; no runtime dependencies. Presentation calls business services; repositories handle CSV; immutable domain records carry validated data. `Main` wires dependencies through constructors.

```text
pom.xml
src/main/java/com/personalfinance/
  app/          Main (composition root)
  model/        immutable records and category/type/source enums
  ui/           ConsoleUI
  service/      transactions, budgets, recurring, reports, exports, bank, notifications
  repository/   three CSV repositories and shared CsvFile
  strategy/     ReportExportStrategy, Txt/Csv/JsonExportStrategy
  adapter/      BankTransactionProvider, BankAdapter, FakeBankService, ExternalBankRecord
  decorator/    Notifier, ConsoleNotifier, abstract decorator and three decorators
  util/         Money, Validation, Csv, ReportFormatter
src/test/java/com/personalfinance/  functional, UI and pattern tests
data/          created automatically; only .gitkeep is committed
exports/       generated reports; only .gitkeep is committed
examples/sample-data/              optional fixed October 2026 fixtures
docs/          requirements, design, data model, testing, user and defense guides
scripts/       launchers and optional packaged-JAR smoke check
.github/workflows/maven.yml        Java 17 build and smoke check
```

**Strategy:** `ReportExportService` delegates to `ReportExportStrategy`; `TxtExportStrategy`, `CsvExportStrategy`, and `JsonExportStrategy` implement the formats. The UI changes the selected object.

**Adapter:** `BankAdapter` implements `BankTransactionProvider` and translates the incompatible `FakeBankService` feed; `BankImportService` knows only the target interface.

**Decorator:** `TimestampNotifierDecorator`, `PriorityNotifierDecorator`, and `FileLoggingNotifierDecorator` wrap `Notifier` objects around `ConsoleNotifier`. These objects handle real service events.

## Running and data

```sh
java -jar target/personal-finance-manager-1.0.0.jar --data-dir work/demo-data --export-dir work/demo-exports
java -jar target/personal-finance-manager-1.0.0.jar --help
```

Paths are relative to the working directory unless absolute. Defaults: `data/transactions.csv`, `data/budgets.csv`, `data/recurring_transactions.csv`, `data/notifications.log`, and `exports/report-YYYY-MM.{txt,csv,json}`. Files are initialized empty with headers and reloaded from disk. No samples are automatically inserted. Use one application process per data directory. Copy the entire data directory to back it up while the application is closed; do not commit personal data.

## Tests

```sh
mvn clean
mvn compile
mvn test
mvn package
# Optional: Python 3 standard library checks the built JAR in a temporary directory.
python3 scripts/smoke_test.py
```

The JUnit tests use temporary directories and a fixed clock; they do not read personal files. See [actual results and cases](docs/TESTING.md). GitHub Actions runs a Java 17 build and packaged-JAR check.

## Example walkthrough

Use an empty demo directory. Add Salary income 250000 dated 2026-10-01. Set October Food/Transport/Shopping budgets to 50000/20000/60000. Add expenses 35000/18000/32000 in those categories dated 2026-10-02. Balance is **165000.00 KZT**, with budget usages **70.00%, 90.00%, 53.33%**. Transport triggers an 80% alert. Generate October's report and choose each export format in turn. Add a further Food expense of 5000 to cross 80%. The [user guide](docs/USER_GUIDE.md) provides the full demonstration, including recurring transactions and restart.

## Documentation

- [Requirements and use cases](docs/REQUIREMENTS.md)
- [Architecture, patterns, design decisions and SOLID](docs/SOFTWARE_DESIGN.md)
- [CSV schemas, examples and relationships](docs/DATABASE_DESIGN.md)
- [Test cases and actual results](docs/TESTING.md)
- [Installation, menu instructions and full demo](docs/USER_GUIDE.md)
- [Defense questions and short demonstration](docs/DEFENSE_GUIDE.md)
- [Assignment compliance matrix](docs/COMPLIANCE.md)
- [Delivery report and full file tree](docs/DELIVERY_REPORT.md)
- [Class diagram](docs/diagrams/class-diagram.md), [architecture diagram](docs/diagrams/architecture-diagram.md), [ER diagram](docs/diagrams/er-diagram.md), [use cases](docs/diagrams/use-case-diagram.md)

## Limitations

Single user/process, KZT only, monthly recurrence only. CSV files are appropriate for small local datasets, without locking or encryption. A file replacement is atomic when supported; changes across multiple files are not a database transaction. Recurring occurrence IDs make interrupted processing safe to retry. Bank imports commit individual transactions; retry skips completed ones after an I/O interruption. Notification log failures are reported without undoing saved finance data. Fixed fake bank dates deliberately keep demonstrations repeatable. Exporting the same period/format replaces its report. Windows launch instructions are provided; automated checks were run on Linux.
