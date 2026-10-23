# Assignment compliance matrix

Completion is supported by existing source, documentation or executed checks. **Complete** below describes the implementation/local verification; it does not assert a grade, remote CI status, Windows execution, multi-process safety or database-level transactions. Tests and limitations are recorded in TESTING.md and README.md. Requirements correspond to sections 1–23 of the supplied specification; the delivery report addresses section 24.

## Functional requirements

| Requirement | Implementation class/file | Test or demonstration | Status |
|---|---|---|---|
| Income recording | TransactionService; Transaction | TransactionServiceTest.addIncomeSavesTypeAndMoney | Complete |
| Expense recording | TransactionService; Transaction | TransactionServiceTest.addExpenseSavesTypeAndSource | Complete |
| Centralized typed categories | Category enum; Validation.category | TransactionServiceTest.serviceRejectsInvalidMoneyCategoryAndDescription | Complete |
| History sorted by date and ID | TransactionService.all; ConsoleUI | TransactionServiceTest.filtersAndHistorySortByDateThenId | Complete |
| Lookup by ID and missing ID | TransactionService.byId | TransactionServiceTest.missingTransactionHasClearError | Complete |
| Category/month/type filters and empty history | TransactionService.filter; ConsoleUI | TransactionServiceTest; ConsoleUITest | Complete |
| Balance = income minus expenses | TransactionService.balance | TransactionServiceTest.calculateBalance; packaged-JAR sample check | Complete |
| Monthly budgets | BudgetService; BudgetRepository | BudgetServiceTest.createAndUpdatePreserveIdentityAndPersist | Complete |
| Positive spending limits and expense-only categories | Budget; Money; Validation | BudgetServiceTest.rejectInvalidBudgetLimitAndIncomeCategory | Complete |
| Update limits; prevent duplicate category/month; retain ID | BudgetService.save; BudgetRepository | BudgetServiceTest.createAndUpdatePreserveIdentityAndPersist; PersistenceTest | Complete |
| Separate months; expenses only; income never offsets spent | BudgetService.spent | BudgetServiceTest.budgetsAreIndependentPerMonthAndExpenseOnly | Complete |
| Spent/remaining/usage and negative remaining | BudgetStatus; ConsoleUI.showBudgets | BudgetServiceTest; packaged-JAR 70/90/53.33% check | Complete |
| Budget alerts 80%, exactly 100%, above 100% | BudgetService.checkThreshold | BudgetServiceTest.thresholdsCrossOnceAcrossRestart | Complete |
| No repeated threshold; highest newly crossed level per entry | BudgetService.checkThreshold | BudgetServiceTest.jumpAcrossThresholdsEmitsHighestOnly | Complete |
| Monthly recurring income and expenses | RecurringTransactionService; RecurringTransaction | RecurringTransactionServiceTest; packaged-JAR check | Complete |
| Create/list/disable/process recurring rules | ConsoleUI.recurringMenu; RecurringTransactionService | RecurringTransactionServiceTest; USER_GUIDE demonstration | Complete |
| Automatic recurring startup and manual processing | Main; ConsoleUI; RecurringTransactionService | scripts/smoke_test.py startup/manual checks | Complete |
| Monthly idempotency including restart/stale marker | RecurringTransactionService occurrence keys; lastProcessedMonth | RecurringTransactionServiceTest.occurrenceKeyRecoversFromStaleMarker | Complete |
| Due dates only; month-end/leap clamping; catch-up | RecurringTransactionService.processDue | RecurringTransactionServiceTest monthly/future/leap cases | Complete |
| Reports by category | ReportService; Report | ReportServiceTest.reportGroupsCorrectMonthAndMatchesSample | Complete |
| Reports by selected year/month, totals/net/count | ReportService.generate; ConsoleUI | ReportServiceTest; ConsoleUITest invalid date/month/year | Complete |
| Immutable reports derived from transactions | Report; ReportService | ReportServiceTest immutable map assertion | Complete |
| TXT, CSV and JSON exports with genuine data | Three export strategies; ReportExportService | StrategyPatternTest; Python parses JAR-generated JSON | Complete |
| Exports directory and deterministic filenames | ConsoleUI.export; --export-dir | ConsoleUITest; scripts/smoke_test.py | Complete |
| Simulated bank import with at least five mixed operations | FakeBankService six operations | AdapterPatternTest.incompatibleBankBecomesUsableThroughTargetInterface | Complete |
| Minor-unit/date/type/category conversion, external IDs | BankAdapter; BankImportRecord | AdapterPatternTest conversion and fallback cases | Complete |
| Persistent duplicate bank prevention and skipped counts | BankImportService; TransactionRepository | AdapterPatternTest repeat imports after reload; packaged-JAR check | Complete |
| Imported expenses affect budgets and alerts | BankImportService → TransactionService | AdapterPatternTest.repeatImportsSkipDuplicatesAfterRestartAndAffectBudgets | Complete |
| Notifications for real additions/imports/recurrence/budgets | NotificationService; service calls | ConsoleUITest; packaged-JAR check | Complete |
| Persistent notification log and menu history | FileLoggingNotifierDecorator; NotificationService | DecoratorPatternTest; ConsoleUITest; USER_GUIDE | Complete |

## Design patterns

| Requirement | Implementation class/file | Test or demonstration | Status |
|---|---|---|---|
| Strategy interface | ReportExportStrategy | StrategyPatternTest | Complete |
| Three concrete strategies | TxtExportStrategy; CsvExportStrategy; JsonExportStrategy | StrategyPatternTest.sameContextSwitchesBetweenThreeRealFormats | Complete |
| Context reference/delegation and runtime switching | ReportExportService.setStrategy/exportReport; ConsoleUI.export | StrategyPatternTest; ConsoleUITest | Complete |
| Strategy extensibility without context edits | ReportExportStrategy implementations | StrategyPatternTest.newStrategyWorksWithoutChangingContext | Complete |
| Adapter target interface | BankTransactionProvider | AdapterPatternTest | Complete |
| Incompatible third-party component | FakeBankService; ExternalBankRecord | AdapterPatternTest asserts bank is not target | Complete |
| Adapter implementation and actual integration | BankAdapter; BankImportService | AdapterPatternTest; ConsoleUI option 10 | Complete |
| Original bank remains independent/unchanged by adaptation | FakeBankService has no application model/interface imports | SOFTWARE_DESIGN adapter explanation; source inspection | Complete |
| Decorator common component interface | Notifier | DecoratorPatternTest | Complete |
| Decorator base component | ConsoleNotifier | DecoratorPatternTest.baseNotifierPrintsOriginalMessage | Complete |
| Abstract wrapper and three concrete decorators | NotifierDecorator; Timestamp/Priority/FileLoggingNotifierDecorator | DecoratorPatternTest six cases | Complete |
| Each decorator wraps a Notifier | NotifierDecorator.wrapped and constructors | Source inspection; independent and combined decorator tests | Complete |
| Multiple decorator combinations | Main normal/important pipelines | DecoratorPatternTest.combinedDecoratorsProduceIdenticalConsoleAndLogContent | Complete |
| Actual notification integration | NotificationService injected into finance services | ConsoleUITest; scripts/smoke_test.py | Complete |

## Architecture and persistence

| Requirement | Implementation class/file | Test or demonstration | Status |
|---|---|---|---|
| Presentation layer | ui/ConsoleUI | ConsoleUITest | Complete |
| Business layer | service/ seven focused services | Functional service tests | Complete |
| Data access layer | repository/ three repositories; CsvFile | PersistenceTest | Complete |
| Domain models | model/ validated immutable records/enums | Functional tests and constructor validation | Complete |
| Input validation | Money; Validation; domain constructors; ConsoleUI | TransactionServiceTest; ConsoleUITest; BudgetServiceTest | Complete |
| Error handling | ConsoleUI catch blocks; Main startup handling | ConsoleUITest; corruption process check | Complete |
| Missing files created automatically with headers | CsvFile; NotificationService | PersistenceTest.missingFilesAreCreatedWithHeaders | Complete |
| Transactions, budgets, recurring state persist after restart | Three CSV repositories | PersistenceTest.allEntitiesReloadAndIdsContinue; packaged-JAR check | Complete |
| Separation of concerns; no circular service dependency | Main constructor wiring; package structure | SOFTWARE_DESIGN; architecture diagram | Complete |
| UTF-8 and stable schema/monetary format | Csv; repositories; Money | PersistenceTest Unicode/newline round-trip; DATABASE_DESIGN | Complete |
| Correct escaping and multiline CSV parsing | Csv.row/parse | PersistenceTest.csvRoundTripsCommasQuotesUnicodeAndNewlines | Complete |
| Corruption detection without silent discard | CsvFile.read; repository findAll | PersistenceTest corruption/syntax/flag/duplicate tests | Complete |
| Unique IDs and persistent keys | Repository nextId/saveAll/findAll | PersistenceTest duplicates and reload | Complete |
| Safe ordinary updates via temporary file replacement | Csv.write | Source inspection; persistence/reload tests; fallback documented | Complete |
| Configurable data directory; portable Paths | Main --data-dir and --export-dir; repository constructors | scripts/smoke_test.py isolated directories | Complete |
| No seed overwrite of personal data | Empty runtime initialization; separate examples | Initialization test; examples/sample-data/README.md | Complete |

## Documentation

| Requirement | Implementation class/file | Test or demonstration | Status |
|---|---|---|---|
| Requirements specification | docs/REQUIREMENTS.md | FR-01 through FR-16 acceptance table | Complete |
| Problem statement | docs/REQUIREMENTS.md | Problem, goals and actors | Complete |
| Project goals | docs/REQUIREMENTS.md | Problem, goals and actors | Complete |
| Target users and actors | docs/REQUIREMENTS.md | One User, simulated bank, recurring processor | Complete |
| Functional requirements | docs/REQUIREMENTS.md | Numbered requirements table | Complete |
| Concrete non-functional requirements | docs/REQUIREMENTS.md | Non-functional requirements table | Complete |
| At least six complete use cases | docs/REQUIREMENTS.md | UC-01 through UC-08 with alternatives/postconditions | Complete |
| UML class diagram | docs/diagrams/class-diagram.md and .mmd | Mermaid 11.4.1 parser passed | Complete |
| Architecture diagram | docs/diagrams/architecture-diagram.md and .mmd | Mermaid 11.4.1 parser passed | Complete |
| ER/data model diagram | docs/diagrams/er-diagram.md and .mmd | Mermaid 11.4.1 parser passed | Complete |
| Use-case diagram/equivalent | docs/diagrams/use-case-diagram.md and .mmd | Mermaid 11.4.1 parser passed | Complete |
| Pattern roles and reasons | docs/SOFTWARE_DESIGN.md | Strategy, Adapter and Decorator sections match source | Complete |
| Design decisions and SOLID | docs/SOFTWARE_DESIGN.md | Algorithms, tradeoffs and SOLID explanations | Complete |
| CSV entities, attributes/types/constraints/relationships | docs/DATABASE_DESIGN.md | Schemas, records, logical relationships, no invented SQL FKs | Complete |
| Loading/saving/updating/error process | docs/DATABASE_DESIGN.md | Recovery and per-file replacement discussion | Complete |
| Functional test cases separate from pattern tests | docs/TESTING.md | 35 functional/UI/persistence and 12 pattern executions | Complete |
| Invalid-input tests | docs/TESTING.md; ConsoleUITest; TransactionServiceTest | Actual input/expected/actual/pass tables | Complete |
| Actual executed test results | docs/TESTING.md | 47 passed, zero failures/errors/skips | Complete |
| User documentation | docs/USER_GUIDE.md | All menu choices, examples, errors, saved files | Complete |
| README and installation guide | README.md; docs/USER_GUIDE.md | Terminal and IntelliJ instructions | Complete |
| Realistic sample data | examples/sample-data/; TestEnvironment.sample | 250000−35000−18000−32000 = 165000 | Complete |
| Manual demonstration and safe reset | docs/USER_GUIDE.md | 21 steps and separate unused demo directories | Complete |
| Defense preparation: 15+ questions and short demo | docs/DEFENSE_GUIDE.md | Five concepts, 27 questions, timed demonstration | Complete |

## Final application and build

| Requirement | Implementation class/file | Test or demonstration | Status |
|---|---|---|---|
| Java 17; Maven; JUnit 5; no runtime dependencies | pom.xml | JDK 17.0.12 clean package; dependency scope=test only | Complete |
| Executable JAR with Main-Class | pom.xml maven-jar-plugin | Real java -jar invocation via smoke script | Complete |
| Clean/compile/test/package goals | pom.xml | Executed compile, test, clean package; see TESTING.md | Complete |
| Application compiles and runs; main menu works | Main; ConsoleUI | Java 17 Maven build and process-level check | Complete |
| Automated tests execute; at least five meaningful functional tests | src/test/java/com/personalfinance/ | 47 total; 35 functional executions across six suites | Complete |
| Pattern-specific tests | StrategyPatternTest; AdapterPatternTest; DecoratorPatternTest | 12 passing executions | Complete |
| Complete workflows A–F | Services; ConsoleUI; Main | JUnit end-to-end tests; packaged-JAR startup/restart check | Complete |
| Correct packages, names, access and readable code | com.personalfinance packages; immutable records/private collaborators | Source inspection; class/architecture diagrams | Complete |
| Instructor can build/run using README | README.md; scripts/run.sh and run.bat | Commands and IntelliJ SDK instructions provided | Complete |
| KZT, single local user; no unnecessary frameworks/services | Money; Category; application architecture | No runtime dependencies; fake bank only | Complete |
| Offline JAR operation | Standard Java runtime classes only | Packaged-JAR smoke invoked without network grants | Complete |
| Continuous integration configuration | .github/workflows/maven.yml | Java 17 Maven build and optional Python smoke check defined | Complete |

## Explicit limits of verification

All 47 JUnit executions and the real-JAR smoke check passed locally on Linux/JDK 17. Diagram syntax passed Mermaid 11.4.1 parsing; rendered layout was not visually assessed. Native Windows/macOS execution and performance benchmarks were not run. CSV replacement is per file, with a non-atomic fallback when the filesystem lacks atomic moves. There is no cross-file ACID transaction, locking, encryption or real bank integration; none is required for this assignment.
