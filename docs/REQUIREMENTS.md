# Requirements specification

## Problem, goals and actors

Individuals need a clear view of their income, spending and remaining budget. Separate notes can omit purchases and make monthly comparisons difficult. This application keeps durable categorized transactions, calculates budgets and reports, and demonstrates Strategy, Adapter and Decorator with ordinary Java.

The primary actor is one **User** managing personal finances. The **Simulated Bank Service** supplies an incompatible external feed. The **Recurring Transaction Processor** automatically applies due monthly entries. There are no administrators, accounts or login roles.

## Functional requirements

| ID | Requirement | Acceptance evidence |
|---|---|---|
| FR-01 | Add positive income with category, description, date and unique ID | Salary 250000 persists as INCOME |
| FR-02 | Add positive expenses with matching expense category | Food 35000 persists as EXPENSE |
| FR-03 | View sorted history and transaction by ID | Date then ID order; missing ID gives message |
| FR-04 | Filter by category, month or type | Only matching records, friendly empty result |
| FR-05 | Create/update category-month spending limits | Same budget ID retained, separate months |
| FR-06 | Calculate spent, remaining and usage; notify on threshold crossing | 80%, exactly 100%, over 100%; highest crossed threshold only per entry |
| FR-07 | Create, list, disable and process monthly recurring records | Startup/manual processing; no future or repeated occurrences; clamp day 31 |
| FR-08 | Generate monthly category report | Totals, net, count and both category maps computed from transactions |
| FR-09 | Export TXT using Strategy | Readable financial report |
| FR-10 | Export CSV using Strategy | Escaped fields, header, representative amounts |
| FR-11 | Export JSON using Strategy | Valid JSON with all report fields |
| FR-12 | Import six fake bank records using Adapter | Minor units/date/direction mapped; repeat IDs skipped after reload |
| FR-13 | Display/log real events through Decorator | Timestamp, priority, persistent messages; view log in menu |
| FR-14 | Persist transactions, budgets and recurring state | Restart preserves entities, IDs, balance and reports |
| FR-15 | Calculate balance | All income minus all expenses, with BigDecimal |
| FR-16 | Validate input and report file errors | Bad amounts/categories/dates/menu/IDs/exports do not crash UI; corruption stops loading |

The predefined categories are the centralized `Category` enum: Salary, Scholarship, Gift, Other Income; Food, Transport, Shopping, Entertainment, Education, Rent, Utilities, Health, Other Expense. Sources are MANUAL, BANK and RECURRING. No conversion is performed; all money is KZT.

## Non-functional requirements

| Concern | Concrete requirement and check |
|---|---|
| Maintainability/readability | Separate `ui`, `service`, `repository`, `model` and pattern packages; constructor injection and focused classes |
| Reliability | Reject invalid money and mismatched categories before saving; validate CSV headers/records and preserve originals on error |
| Portability | Compile for Java 17; use relative/configurable `Path`, UTF-8, standard library; executable JAR, Windows/Linux launchers |
| Usability | Read whole input lines, display English menus and meaningful errors; EOF exits cleanly |
| Persistence | Missing files created, headers stable, changes written through sibling temporary files; reload test for all entities |
| Small dataset performance | Linear scans and full-file updates are acceptable for a personal dataset of hundreds/thousands of entries; no large-data throughput claim |
| Testability | Fixed dates/Clock and temporary directories; JUnit 5 via `mvn test`; independent pattern tests |
| Offline operation | No external API or runtime dependencies; after build, JAR runs without network |

## Use cases

| ID / goal | Actor | Preconditions | Main flow | Alternative flow | Postconditions |
|---|---|---|---|---|---|
| UC-01 Add expense | User | Writable valid data; app running | Choose 2; amount/category/description/date; validate; save; check budget; confirm | Invalid input shows error and returns to menu | Expense durable, balance reduced, relevant alert logged |
| UC-02 Add income | User | App running | Choose 1; supply salary data; validate; save; confirm | Wrong category/amount rejected | Income durable, balance increased |
| UC-03 Set budget | User | Expense category/month selected | Choose 5; category/month/limit; create or update; show status | Invalid limit rejected; existing key updates same ID | One budget per category/month |
| UC-04 Generate report | User | Any saved transactions, including none | Choose 8; enter YYYY-MM; calculate; display | Invalid month/year rejected | No stored data changed; computed report displayed |
| UC-05 Export report | User | Writable export location | Choose 9; month; select TXT/CSV/JSON; context delegates; confirm path | 0 returns; bad format rejected; I/O failure reported | Selected report written; finance data unchanged |
| UC-06 Import bank | User and simulated bank | Valid data files | Choose 10; fetch through adapter; convert; deduplicate; save; notify counts | Repeated IDs skipped; after I/O interruption retry resumes | New bank records durable, expenses affect budgets |
| UC-07 Process recurring | User or recurring processor | Active rules exist | At startup or 7→5, find due month; clamp day; save occurrence; update marker | Future/disabled/already processed skipped; stale marker recovered via occurrence key | Each due occurrence exists once |
| UC-08 Restart/reload | User | Previously saved files | Exit; relaunch with same data directory; validate files; process due; inspect history | Corrupt file stops startup clearly without overwriting it | Saved financial state restored |

See [actor/use-case diagram](diagrams/use-case-diagram.md) and [compliance matrix](COMPLIANCE.md) for traceability.
