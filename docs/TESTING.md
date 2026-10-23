# Testing and actual results

Verified on **2026-10-23**, Linux, **Oracle JDK 17.0.12**, Maven **3.9.9**, JUnit Jupiter **5.11.4**, Surefire **3.5.2**. The initially installed runtime was Java 21 without a compiler or Maven; JDK 17 and Maven were downloaded into workspace tooling. Tests and JAR execution used Java 17.

## Executed verification

- Direct `javac --release 17` compilation: passed.
- `mvn compile`: passed.
- `mvn test`: initially 46 passed and one test expected the wrong order of decorator prefixes. The expectation was corrected to the actual, consistent `[timestamp] [IMPORTANT]` chain.
- `mvn clean package` and final offline `mvn -o -B clean package`: passed after that correction; includes clean, compilation, all tests and executable JAR creation.
- `JAVA_HOME=<JDK-17> python3 scripts/smoke_test.py`: passed against the packaged JAR. This uses isolated temporary files and starts real Java processes.

The environment requires an HTTP proxy and separate dependency cache. The executed Maven invocation used `JAVA_HOME=<workspace-jdk17> MAVEN_OPTS=-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts <workspace-maven>/bin/mvn -s <workspace-settings> -B clean package`. The settings select the session proxy and workspace cache, with no project dependency on those absolute paths. Normal local installations use the plain commands in README.

All four standalone Mermaid diagrams also passed `mermaid.parse` using Mermaid 11.4.1. A link-target check passed for Markdown documents, and embedded diagrams match their standalone sources. The executable JAR manifest identifies Main, and all class files use Java 17 bytecode version 61. Diagram layout was not visually assessed.

The packaged-JAR check verifies the main menu, salary/expenses, sample balance 165000, budget usages 70/90/53.33%, threshold logs, real TXT/CSV exports, JSON decoded with Python's standard `json` parser, recurring processing twice, bank imports twice, entity preservation after restart, startup automatic processing, and nonzero failure plus original-file preservation on corrupt input. It does not modify `data/` or `exports/`.

**Final JUnit result: 47 executed, 47 passed, 0 failures, 0 errors, 0 skipped.** Of these, 35 executions cover functional/persistence/UI behavior and 12 cover the three patterns. Parameterized invalid-money cases count separately.

| Suite | Executed | Failed | Errors | Skipped |
|---|---:|---:|---:|---:|
| AdapterPatternTest | 3 | 0 | 0 | 0 |
| BudgetServiceTest | 5 | 0 | 0 | 0 |
| ConsoleUITest | 3 | 0 | 0 | 0 |
| DecoratorPatternTest | 6 | 0 | 0 | 0 |
| PersistenceTest | 7 | 0 | 0 | 0 |
| RecurringTransactionServiceTest | 4 | 0 | 0 | 0 |
| ReportServiceTest | 2 | 0 | 0 | 0 |
| StrategyPatternTest | 3 | 0 | 0 | 0 |
| TransactionServiceTest | 14 | 0 | 0 | 0 |

## Functional, persistence and UI cases

All cases use JUnit `@TempDir`; dates and clocks are fixed except the optional process-level smoke test, which schedules its demo rule for its current local date.

| ID / test method | Objective | Input | Expected | Actual result | Status |
|---|---|---|---|---|---|
| TC-01: `BudgetServiceTest.budgetsAreIndependentPerMonthAndExpenseOnly` | Calculate budget spending | October Food 35000/50000, November 1000; salary income | October spent 35000, remaining 15000, usage 70%; November 1000 | Verified: October spent 35000, remaining 15000, usage 70%; November 1000 | PASS |
| TC-02: `BudgetServiceTest.thresholdsCrossOnceAcrossRestart` | Threshold persistence | Food limit 100; cumulative spending 79,80,81,100,101,102 with reopen | One 80%, one 100%, one exceeded alert; remaining -2.00 | Verified: One 80%, one 100%, one exceeded alert; remaining -2.00 | PASS |
| TC-03: `BudgetServiceTest.rejectInvalidBudgetLimitAndIncomeCategory` | Validate budgets | Zero limit; Salary budget | Both rejected with no saved budget | Verified: Both rejected with no saved budget | PASS |
| TC-04: `BudgetServiceTest.createAndUpdatePreserveIdentityAndPersist` | Create/update budget | Food October limit 50000 then 60000; reopen | One budget, same ID, updated limit retained | Verified: One budget, same ID, updated limit retained | PASS |
| TC-05: `BudgetServiceTest.jumpAcrossThresholdsEmitsHighestOnly` | Handle threshold jump | Food expense 105 against limit 100 | Only exceeded alert | Verified: Only exceeded alert | PASS |
| TC-06: `ConsoleUITest.endOfInputExitsCleanly` | Console EOF | Empty input stream | Input closed. Data is saved. | Verified: Input closed. Data is saved. | PASS |
| TC-07: `ConsoleUITest.completeConsoleFlowAndRuntimeExportSwitching` | Console end-to-end operations | Salary, budget, expense, report, three exports, two imports, log/history | Balance 210000; alert; all files; imports 6 then 0 | Verified: Balance 210000; alert; all files; imports 6 then 0 | PASS |
| TC-08: `ConsoleUITest.invalidInputsReturnToMenuWithoutCrashing` | Console validation | Bad menu/money/category/date/month/year/ID/export choice | Clear messages; goodbye; no transaction created | Verified: Clear messages; goodbye; no transaction created | PASS |
| TC-09: `PersistenceTest.corruptRecordIsReportedAndNeverOverwritten` | Corruption preservation | CSV row with nonnumeric amount | IOException names record 2; exact original content retained | Verified: IOException names record 2; exact original content retained | PASS |
| TC-10: `PersistenceTest.allEntitiesReloadAndIdsContinue` | Persistence after restart | Sample transactions, budget and processed rule; reopen | All entities/balance equal, next transaction ID 6, repeat recurrence zero | Verified: All entities/balance equal, next transaction ID 6, repeat recurrence zero | PASS |
| TC-11: `PersistenceTest.csvRoundTripsCommasQuotesUnicodeAndNewlines` | CSV escaping and parsing | Comma/quote/multiline/Unicode description, CRLF and empty fields | Exact transaction and fields round-trip | Verified: Exact transaction and fields round-trip | PASS |
| TC-12: `PersistenceTest.duplicateBudgetsAndInvalidActiveFlagRejected` | Persistent key/boolean validation | Same category/month twice; active=maybe | Both invalid datasets rejected | Verified: Both invalid datasets rejected | PASS |
| TC-13: `PersistenceTest.duplicateIdsAndExternalIdsAreRejected` | Reject duplicate persistent identity | Same transaction twice; different IDs with same BANK external ID | Both rejected; original empty table retained | Verified: Both rejected; original empty table retained | PASS |
| TC-14: `PersistenceTest.malformedCsvAndHeaderRejected` | CSV syntax/header validation | Unclosed/embedded/after-quote errors and wrong header | All rejected with IOException | Verified: All rejected with IOException | PASS |
| TC-15: `PersistenceTest.missingFilesAreCreatedWithHeaders` | Initialize missing storage | New temporary directory | All required files created; transaction header verified | Verified: All required files created; transaction header verified | PASS |
| TC-16: `RecurringTransactionServiceTest.leapYearAndInvalidRecurringId` | Leap year and ID validation | Rule Jan 31 2024; process Feb 29; disable ID 999 | Feb 29 generated; missing rule rejected | Verified: Feb 29 generated; missing rule rejected | PASS |
| TC-17: `RecurringTransactionServiceTest.monthlyCatchUpClampsDayAndNeverCreatesFutureTransactions` | Monthly recurrence | Rent starts 2026-01-31; process March 30 then 31; reopen | Jan 31, Feb 28, then Mar 31 once each; repeat zero | Verified: Jan 31, Feb 28, then Mar 31 once each; repeat zero | PASS |
| TC-18: `RecurringTransactionServiceTest.futureStartAndDisabledRecurrenceAreNotApplied` | Respect due/active state | Future rule and disabled due rule | No generated transactions | Verified: No generated transactions | PASS |
| TC-19: `RecurringTransactionServiceTest.occurrenceKeyRecoversFromStaleMarker` | Recover interrupted recurrence | Save occurrence, restore old empty marker, reopen/process | No duplicate, marker repaired to 2026-10 | Verified: No duplicate, marker repaired to 2026-10 | PASS |
| TC-20: `ReportServiceTest.emptyReportIsZeroAndInvalidYearRejected` | Empty report and year validation | Empty dataset; year 0 | Zero totals/count; invalid year rejected | Verified: Zero totals/count; invalid year rejected | PASS |
| TC-21: `ReportServiceTest.reportGroupsCorrectMonthAndMatchesSample` | Monthly report | Assignment sample plus November expense | October income 250000, expenses 85000, net 165000, count 4, immutable category maps | Verified: October income 250000, expenses 85000, net 165000, count 4, immutable category maps | PASS |
| TC-22: `TransactionServiceTest.addIncomeSavesTypeAndMoney` | Add income | Salary 250000 on 2026-10-01 | INCOME with amount 250000.00 saved and retrievable | Verified: INCOME with amount 250000.00 saved and retrievable | PASS |
| TC-23: `TransactionServiceTest.missingTransactionHasClearError` | Handle missing ID | Lookup ID 99 | Transaction not found. | Verified: Transaction not found. | PASS |
| TC-24: `TransactionServiceTest.serviceRejectsInvalidMoneyCategoryAndDescription` | Validate business inputs | Negative amount, income/Food mismatch, blank description | All rejected; no records saved | Verified: All rejected; no records saved | PASS |
| TC-25: `TransactionServiceTest.calculateBalance` | Calculate balance | Income 200000, expense 50000 | Balance 150000.00 | Verified: Balance 150000.00 | PASS |
| TC-26: `TransactionServiceTest.filtersAndHistorySortByDateThenId` | Filter and sort history | Three transactions in two months and types | Date/ID order and exact category/month/type subsets | Verified: Date/ID order and exact category/month/type subsets | PASS |
| TC-27: `TransactionServiceTest.addExpenseSavesTypeAndSource` | Add expense | Food 12.50 on 2026-10-02 | EXPENSE with MANUAL source persisted | Verified: EXPENSE with MANUAL source persisted | PASS |
| TC-28: `TransactionServiceTest.invalidAmountsAreRejectedWithoutSaving(String)[1]` | Reject invalid money | invalidAmountsAreRejectedWithoutSaving(String)[1]; values: -1, 0, abc, 1.234, blank, 1e3, NaN, 12,50 | Validation error and no transactions | Verified: Validation error and no transactions | PASS |
| TC-29: `TransactionServiceTest.invalidAmountsAreRejectedWithoutSaving(String)[2]` | Reject invalid money | invalidAmountsAreRejectedWithoutSaving(String)[2]; values: -1, 0, abc, 1.234, blank, 1e3, NaN, 12,50 | Validation error and no transactions | Verified: Validation error and no transactions | PASS |
| TC-30: `TransactionServiceTest.invalidAmountsAreRejectedWithoutSaving(String)[3]` | Reject invalid money | invalidAmountsAreRejectedWithoutSaving(String)[3]; values: -1, 0, abc, 1.234, blank, 1e3, NaN, 12,50 | Validation error and no transactions | Verified: Validation error and no transactions | PASS |
| TC-31: `TransactionServiceTest.invalidAmountsAreRejectedWithoutSaving(String)[4]` | Reject invalid money | invalidAmountsAreRejectedWithoutSaving(String)[4]; values: -1, 0, abc, 1.234, blank, 1e3, NaN, 12,50 | Validation error and no transactions | Verified: Validation error and no transactions | PASS |
| TC-32: `TransactionServiceTest.invalidAmountsAreRejectedWithoutSaving(String)[5]` | Reject invalid money | invalidAmountsAreRejectedWithoutSaving(String)[5]; values: -1, 0, abc, 1.234, blank, 1e3, NaN, 12,50 | Validation error and no transactions | Verified: Validation error and no transactions | PASS |
| TC-33: `TransactionServiceTest.invalidAmountsAreRejectedWithoutSaving(String)[6]` | Reject invalid money | invalidAmountsAreRejectedWithoutSaving(String)[6]; values: -1, 0, abc, 1.234, blank, 1e3, NaN, 12,50 | Validation error and no transactions | Verified: Validation error and no transactions | PASS |
| TC-34: `TransactionServiceTest.invalidAmountsAreRejectedWithoutSaving(String)[7]` | Reject invalid money | invalidAmountsAreRejectedWithoutSaving(String)[7]; values: -1, 0, abc, 1.234, blank, 1e3, NaN, 12,50 | Validation error and no transactions | Verified: Validation error and no transactions | PASS |
| TC-35: `TransactionServiceTest.invalidAmountsAreRejectedWithoutSaving(String)[8]` | Reject invalid money | invalidAmountsAreRejectedWithoutSaving(String)[8]; values: -1, 0, abc, 1.234, blank, 1e3, NaN, 12,50 | Validation error and no transactions | Verified: Validation error and no transactions | PASS |

## Independent design-pattern cases

All cases use JUnit `@TempDir`; dates and clocks are fixed except the optional process-level smoke test, which schedules its demo rule for its current local date.

| ID / test method | Objective | Input | Expected | Actual result | Status |
|---|---|---|---|---|---|
| PT-01: `AdapterPatternTest.repeatImportsSkipDuplicatesAfterRestartAndAffectBudgets` | Adapter integration and idempotency | Import six, reopen, import again; Food limit 15000 | 6/0 then 0/6; six transactions; spent 12500.50 and 80% log | Verified: 6/0 then 0/6; six transactions; spent 12500.50 and 80% log | PASS |
| PT-02: `AdapterPatternTest.unknownMerchantUsesAppropriateFallbackAndBadDirectionIsRejected` | Adapter edge cases | Unknown group, invalid direction, February 31 | OTHER_EXPENSE fallback; bad direction/date rejected | Verified: OTHER_EXPENSE fallback; bad direction/date rejected | PASS |
| PT-03: `AdapterPatternTest.incompatibleBankBecomesUsableThroughTargetInterface` | Adapter conversion | Six external records including minor-unit 1250050 | Bank is not target; adapter produces correct types, 12500.50, date/category/external ID | Verified: Bank is not target; adapter produces correct types, 12500.50, date/category/external ID | PASS |
| PT-04: `DecoratorPatternTest.loggingDecoratorAppendsAndDelegates` | Logging decorator | First then Second | Both appended in order and delegated | Verified: Both appended in order and delegated | PASS |
| PT-05: `DecoratorPatternTest.loggingFailureStillNotifiesUserAndReportsError` | Logging failure behavior | Log parent is a regular file | Console receives Saved, error reported, existing file retained | Verified: Console receives Saved, error reported, existing file retained | PASS |
| PT-06: `DecoratorPatternTest.timestampDecoratorAddsDeterministicTime` | Timestamp decorator | Saved at fixed clock | [2026-10-23T10:00:00] Saved | Verified: [2026-10-23T10:00:00] Saved | PASS |
| PT-07: `DecoratorPatternTest.baseNotifierPrintsOriginalMessage` | Base notifier | Saved | Exact console message | Verified: Exact console message | PASS |
| PT-08: `DecoratorPatternTest.priorityDecoratorAddsMarker` | Priority decorator | Limit | [IMPORTANT] Limit | Verified: [IMPORTANT] Limit | PASS |
| PT-09: `DecoratorPatternTest.combinedDecoratorsProduceIdenticalConsoleAndLogContent` | Decorator combination | Priority wrapping timestamp wrapping logging wrapping console | [timestamp] [IMPORTANT] Budget exceeded identical on both outputs | Verified: [timestamp] [IMPORTANT] Budget exceeded identical on both outputs | PASS |
| PT-10: `StrategyPatternTest.newStrategyWorksWithoutChangingContext` | Strategy extensibility | Additional strategy implementation | Existing context writes Count=0 | Verified: Existing context writes Count=0 | PASS |
| PT-11: `StrategyPatternTest.sameContextSwitchesBetweenThreeRealFormats` | Strategy switching | Same sample report; TXT→CSV→JSON context strategies | Readable TXT, CSV schema/category/count, JSON fields and values correct | Verified: Readable TXT, CSV schema/category/count, JSON fields and values correct | PASS |
| PT-12: `StrategyPatternTest.unwritableExportLocationIsReported` | Export error handling | Regular file used as parent directory | IOException; existing file unchanged | Verified: IOException; existing file unchanged | PASS |

## Reproduce and inspect

```sh
mvn clean package
python3 scripts/smoke_test.py
```

JUnit details are generated in `target/surefire-reports/` (XML and text). Build artifacts are ignored by Git. The GitHub Actions workflow uses Temurin Java 17, Maven and the same packaged-JAR check. Local test results do not establish remote CI results; consult the workflow run for that status.

[Full manual demonstration](USER_GUIDE.md#full-reproducible-demonstration) provides a reproducible 21-step instructor session, plus safe demo reset instructions. All required menu paths have implementation; JUnit and the process check exercise the major workflows. Windows/macOS native execution and large-dataset performance were not measured. No unresolved failures remain in the executed Java tests or packaged-JAR checks.
