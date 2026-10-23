# Use Case Diagram

Mermaid flowchart equivalent of a use-case diagram: rectangles name actors, rounded nodes name use cases. The recurring processor is a local process, not an external scheduled service. Detailed preconditions, alternatives and postconditions appear in REQUIREMENTS.md.

```mermaid
flowchart LR
    User[User: one local individual]
    Bank[Simulated Bank Service]
    Scheduler[Recurring Processor: startup or manual]
    subgraph App[Personal Finance and Expense Management System]
        Income([Record income])
        Expense([Record expense])
        History([View history, ID and filters])
        Balance([View balance])
        Budget([Create or update monthly budgets])
        Alerts([Track spending and alerts])
        Rules([Create, list or disable recurring rules])
        Due([Process due recurring transactions])
        Report([Generate monthly report])
        Export([Export TXT, CSV or JSON])
        Import([Import bank transactions])
        Log([View notifications])
        Persist([Save and reload financial data])
    end
    User --> Income
    User --> Expense
    User --> History
    User --> Balance
    User --> Budget
    User --> Rules
    User --> Due
    User --> Report
    User --> Export
    User --> Import
    User --> Log
    Expense --> Alerts
    Import --> Alerts
    Due --> Alerts
    Scheduler --> Due
    Bank --> Import
    Income --> Persist
    Expense --> Persist
    Budget --> Persist
    Rules --> Persist
    Due --> Persist
    Import --> Persist
```

[Standalone Mermaid source](use-case-diagram.mmd)
