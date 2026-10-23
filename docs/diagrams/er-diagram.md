# Er Diagram

These are logical relationships, not SQL foreign keys. CATEGORY is a Java enum, not a stored table. Category/month uniquely identifies a budget; source/externalId uniquely identifies a nonempty imported/generated occurrence. Generated transactions encode rule ID and month in externalId; manual and bank entries have no recurring parent. The CSV layer validates IDs and keys but provides no database referential constraints.

```mermaid
erDiagram
    CATEGORY ||..o{ TRANSACTION : categorizes
    CATEGORY ||..o{ BUDGET : limits_per_month
    CATEGORY ||..o{ RECURRING_TRANSACTION : categorizes
    RECURRING_TRANSACTION o|..o{ TRANSACTION : generates_logically
    CATEGORY {
        string name PK
        string label
        string type
    }
    TRANSACTION {
        long id PK
        string type
        decimal amount
        string category
        string description
        date date
        string source
        string externalId
    }
    BUDGET {
        long id PK
        string category
        int year
        int month
        decimal limit
    }
    RECURRING_TRANSACTION {
        long id PK
        string type
        decimal amount
        string category
        string description
        date startDate
        boolean active
        string lastProcessedMonth
    }
```

[Standalone Mermaid source](er-diagram.mmd)
