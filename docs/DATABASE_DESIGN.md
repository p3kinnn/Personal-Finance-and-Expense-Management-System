# Persistent data model (CSV)

There is no database server. UTF-8 CSV files are the persistent data model, initialized with headers in the selected `--data-dir` (default `data/`). Enum names are stored, while friendly labels are displayed. Monetary strings have two decimal places. IDs are positive `long` values allocated from the maximum existing ID plus one.

## Transaction: transactions.csv

| Column | Java type | Constraint / meaning |
|---|---|---|
| id | long | Unique positive identifier |
| type | TransactionType | INCOME or EXPENSE |
| amount | BigDecimal | Positive, at most two fractional digits; KZT |
| category | Category | Enum name, must match type |
| description | String | Nonblank, may include quotes, commas, Unicode or newlines |
| date | LocalDate | ISO YYYY-MM-DD, year 0001–9999 |
| source | TransactionSource | MANUAL, BANK, RECURRING |
| externalId | String | Optional for manual; required for bank/recurring; unique with source when nonempty |

```csv
id,type,amount,category,description,date,source,externalId
1,INCOME,250000.00,SALARY,October salary,2026-10-01,MANUAL,
2,EXPENSE,35000.00,FOOD,"Groceries, weekly",2026-10-02,MANUAL,
```

## Budget: budgets.csv

| Column | Java type | Constraint / meaning |
|---|---|---|
| id | long | Positive unique ID, retained on update |
| category | Category | Expense category only |
| year | int | 1–9999 |
| month | int | 1–12; together with year forms YearMonth |
| limit | BigDecimal | Positive KZT amount with at most two fractional digits |

The `(category, year, month)` key is unique in repository validation.

```csv
id,category,year,month,limit
1,FOOD,2026,10,50000.00
```

## RecurringTransaction: recurring_transactions.csv

| Column | Java type | Constraint / meaning |
|---|---|---|
| id | long | Positive unique rule ID |
| type | TransactionType | INCOME or EXPENSE |
| amount | BigDecimal | Positive KZT, at most two fractional digits |
| category | Category | Matches type |
| description | String | Nonblank |
| startDate | LocalDate | Earliest occurrence; day defines monthly schedule |
| active | boolean | Exactly `true` or `false` |
| lastProcessedMonth | YearMonth or null | ISO YYYY-MM or empty; cannot precede start month |

```csv
id,type,amount,category,description,startDate,active,lastProcessedMonth
1,INCOME,250000.00,SALARY,Monthly salary,2026-10-01,true,2026-10
```

A recurring-generated transaction stores source RECURRING and `externalId=recurring:1:2026-10`. This is an application occurrence key, not a database foreign key. Even if the last-processed marker was not updated, the transaction's occurrence key prevents another save.

## Relationships and other files

Categories are static enum definitions, not a fourth CSV table. One category has many transactions and recurring rules, and at most one budget in each month. A recurring rule logically generates many transactions. CSV has no SQL foreign keys; the code enforces type/category and unique-key constraints. The [ER diagram](diagrams/er-diagram.md) labels these as logical relationships.

`notifications.log` appends timestamped events; important events also contain `[IMPORTANT]`. It is an audit aid rather than a source of financial totals. The menu reads the same log. Export snapshots go to `--export-dir` (default `exports/`), named `report-YYYY-MM.txt`, `.csv`, or `.json`. CSV exports use `period,section,category,value,currency`: the count row has no currency, other rows use KZT.

## Loading, saving, updating and recovery

Repositories receive a base `Path` and validate files on construction and each read. Missing files are initialized with headers. Existing empty files, wrong headers, wrong column counts, malformed quotes, invalid enum/money/date/flag values, duplicate IDs and duplicate keys produce a path/record error; they are not silently skipped. CSV records can span physical lines, so record numbers identify logical rows.

On save, the existing table is checked again. A complete UTF-8 replacement is written to a temporary file in the same directory and moved into place. Atomic replacement is used when supported, with replacement fallback otherwise. Temporary files are removed in `finally`. A budget edit writes the same budget ID with a new limit. Financial files are not rewritten by ordinary history/report reads.

Close the application before backing up the **whole** data directory. If corruption is reported, preserve the original, inspect the indicated logical record, and restore a known-good backup or carefully repair the CSV. Do not delete a financial file to suppress an error. Two concurrent instances sharing one folder are unsupported; there is no locking, encryption or multi-file transaction.

[Optional sample files](../examples/sample-data/) can be copied into a new empty demonstration directory, never over personal data. Runtime initialization itself never seeds or overwrites samples.
