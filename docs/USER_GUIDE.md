# User guide

## Install and start

Install JDK 17 and Maven 3.8+; check `java -version` and `mvn -version`. Clone/extract the project and use its root as your working directory.

```sh
mvn clean package
java -jar target/personal-finance-manager-1.0.0.jar
```

In IntelliJ IDEA Community Edition: File → Open → select `pom.xml` → Open as Project; set Project SDK to JDK 17 and import Maven dependencies. Run `com.personalfinance.app.Main.main`. Set its working directory to the repository root and optional program arguments to `--data-dir work/demo-data --export-dir work/demo-exports`. Maven's Lifecycle → `package` also builds the executable JAR.

The JAR needs only Java 17 after building. No database, API keys or bank account is involved. `scripts/run.sh` (macOS/Linux) or `scripts\run.bat` (Windows Command Prompt) starts the existing JAR and forwards arguments. Paths containing spaces must be quoted.

```sh
java -jar target/personal-finance-manager-1.0.0.jar --data-dir "work/my demo/data" --export-dir "work/my demo/exports"
```

## Menu reference

| Option | Operation | Inputs / behavior |
|---|---|---|
| 1 | Add Income | Positive KZT amount, income category number, nonblank description, ISO date |
| 2 | Add Expense | Amount, expense category number, description, date; displays relevant monthly budgets |
| 3 | View Transaction History | Date/ID-sorted records, including source |
| 4 | Search / Filter | 1=ID, 2=month, 3=category, 4=income, 5=expenses, 0=back |
| 5 | Create or Update Monthly Budget | Expense category, YYYY-MM, positive limit; an existing category/month is updated in place |
| 6 | View Budget Status | YYYY-MM; limit, spent, remaining, usage and status for each budget |
| 7 | Manage Recurring Transactions | 1=income, 2=expense, 3=list, 4=disable by ID, 5=process due, 0=back |
| 8 | Generate Financial Report | YYYY-MM; monthly totals, income/expense categories, net and count |
| 9 | Export Financial Report | YYYY-MM then 1=TXT, 2=CSV, 3=JSON, 0=back; choose again to switch |
| 10 | Import Bank Transactions | Six fixed October 2026 mock records; imported/skipped counts |
| 11 | View Account Balance | Total income minus total expenses across all dates |
| 12 | View Notifications | Timestamped persistent log, including important budget alerts |
| 0 | Exit | All successful changes have already been saved |

Use decimal points, for example `15000.50`, without currency symbols or thousands separators. More than two fractional digits, zero and negative values are rejected. Category numbering is displayed for each operation. Income categories are Salary=1, Scholarship=2, Gift=3, Other Income=4; expense categories start Food=1, Transport=2, Shopping=3. The category filter shows the full enum list, with its own displayed numbering.

Dates use `YYYY-MM-DD`; a blank transaction/start date uses today according to the computer's local clock. Months use `YYYY-MM`; years must be 0001–9999. Invalid input returns to the main menu; start the operation again. Within recurring management, 0 returns to the main menu. Closing standard input also exits cleanly.

## Budgets and recurrence

A Food budget for 2026-10 is separate from 2026-11. Income never reduces spending. Remaining money becomes negative on overspend. Alerts occur when an expense crosses 80%, reaches exactly 100%, or moves above 100%. Only the highest newly crossed level is reported for a single expense, and subsequent expenses within the same region do not repeat it. Creating/editing a limit displays its current status without replaying historical alerts.

A recurring rule uses its start-date day each month. A start on January 31 creates February's occurrence on February 28 (29 in leap years). Missed due months are caught up; future occurrences are not created. Creation saves the rule; choose 7→5 to process now, or let the next startup process it. List the rules to see IDs, active state and the last processed month; disable by ID to stop future processing. Historical transactions remain. Persistent occurrence keys and monthly markers prevent repeated processing after restarts or an interrupted marker save.

## Full reproducible demonstration

Start in a **new unused** folder, for example `--data-dir work/demo-session-1/data --export-dir work/demo-session-1/exports`. Never point a reset/copy command at your normal data directory. The optional `python3 scripts/smoke_test.py` performs the packaged-JAR equivalent in a disposable temporary directory.

1. Start the application. Confirm the main menu and startup due count appear.
2. Choose 1; enter `250000`, category `1`, description `October salary`, date `2026-10-01`.
3. Choose 2; enter `35000`, category `1`, description `Groceries`, date `2026-10-02`.
4. Choose 11. Balance is `215000.00 KZT`.
5. Choose 5; expense category `1`, month `2026-10`, limit `50000`. Food usage is 70%.
6. Choose 2; add `5000`, Food `1`, description `More groceries`, date `2026-10-03`.
7. Observe Food's 80% alert and `10000.00 KZT` remaining. Another expense while below 100% will not repeat 80%.
8. Choose 12. Check the timestamped `[IMPORTANT]` budget message; it is also in the selected data directory's `notifications.log`.
9. Choose 7→1; amount `50`, Gift category `3`, description `Monthly gift`, **today's date** (or blank). This avoids historic catch-up during the demo.
10. Inside that submenu choose 5. Exactly one due transaction is processed.
11. Choose 5 again. Zero are processed. Choose 3 to see the marker, then 0 to return.
12. Choose 8 and `2026-10` to display the report. The known October base before the recurring gift is income 250000, expenses 40000, net 210000, count 3. If today is in October 2026, the gift adds 50 income and one transaction to this report.
13. Choose 9, `2026-10`, format 1. Inspect `report-2026-10.txt`.
14. Repeat option 9 with format 2. Inspect the CSV header and total/category rows.
15. Repeat with format 3. Inspect JSON totals, categories and count. These strategies switch without restarting.
16. Choose 10. Six operations are imported from the fixed mock feed; Food spending rises by 12500.50 and exceeds the demo budget.
17. Choose 10 again. Zero imported, six skipped. These IDs remain protected after restart.
18. Choose 3 to view all history. Try option 4 for ID/month/category/type filters.
19. Choose 0 to exit.
20. Relaunch using exactly the same data/export arguments.
21. Choose 3, 6 (`2026-10`), 7→3, and 11. Transactions, Food limit, recurring marker and balance remain. Processing due again on the same date makes no duplicate. Exit.

For the assignment's alternate sample totals, use a separate clean session: Salary=250000; Food=35000, Transport=18000, Shopping=32000. Set limits 50000, 20000 and 60000 before adding those expenses. Expected net is **165000 KZT**; usages are 70%, 90%, 53.33%, and Transport crosses 80%. [Optional sample CSV files](../examples/sample-data/) reproduce this dataset without an active recurring rule. Neither sample variant is inserted automatically.

## Saved files, backup and reset

Defaults are `data/transactions.csv`, `data/budgets.csv`, `data/recurring_transactions.csv`, and `data/notifications.log`. Export snapshots are in `exports/`. Both directories are configurable; input files have headers and UTF-8 encoding. No unsaved in-memory session remains when you exit.

Close the application and copy the whole data directory to back up. For another demonstration, select a **new empty folder** such as `work/demo-session-2/data`; this safely resets the demonstration without deleting personal data. Reusing an existing demo folder deliberately preserves its records. Never overwrite normal CSV files with sample data.

## Troubleshooting

| Symptom | Action |
|---|---|
| `mvn` not found | Install Maven and add its `bin` directory to PATH; restart the terminal |
| No compiler / wrong Java | Set IntelliJ SDK and JAVA_HOME/PATH to JDK 17; a JRE alone is insufficient |
| JAR missing | Run `mvn clean package` from the project root; resolve build errors first |
| First build cannot download dependencies | Check Maven network/proxy configuration; the JAR itself needs no network |
| Invalid amount/date/category | Use the displayed formats and numbers, then retry the operation |
| No bank transactions in this month's report | The simulated bank data is intentionally October 2026; request `2026-10` |
| Unexpected recurring catch-up | Rules starting in the past process every due month; use today's start for a one-occurrence demo |
| Export fails | Check `--export-dir` is a writable directory, not a regular file or invalid path |
| CSV corruption reported | Preserve files, inspect the named logical record and restore a backup or repair it; the app refuses to skip bad data |
| Log write fails | Console prints the event and an error; finance data is already saved, so inspect history before retrying |
| Data appears missing | Relaunch with the same working directory and `--data-dir`; do not run two instances on one folder |
