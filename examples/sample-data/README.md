# Optional sample data

These fixed October 2026 CSV files give income 250000, expenses 85000 and balance 165000 KZT. Budget usages are Food 70%, Transport 90%, Shopping 53.33%. There is no active recurring rule, so loading these fixtures never generates historical catch-up.

Copy the three `.csv` files into a **new empty directory** chosen only for demonstration. Do not overwrite an existing data directory. For example on macOS/Linux, from the project root:

```sh
mkdir -p work
mkdir work/sample-session-1
cp examples/sample-data/*.csv work/sample-session-1/
java -jar target/personal-finance-manager-1.0.0.jar --data-dir work/sample-session-1 --export-dir work/sample-exports-1
```

Use a fresh unused session name; `mkdir` without `-p` for the session intentionally fails if it already exists. Only continue the copy after it succeeds. On Windows create a new empty folder in File Explorer, copy the three CSV files there and pass its path using `--data-dir`.

The notification log is created automatically. Loaded samples display status but do not replay old threshold notifications; add expenses through the UI to demonstrate crossing alerts. Normal startup never inserts samples.
