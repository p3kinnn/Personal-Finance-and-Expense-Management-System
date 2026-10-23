#!/usr/bin/env python3
"""Optional packaged-JAR workflow check; uses isolated temporary files, never data/."""
import csv
import io
import json
import os
from pathlib import Path
import subprocess
import tempfile
from datetime import date

ROOT = Path(__file__).resolve().parents[1]
JAR = ROOT / 'target/personal-finance-manager-1.0.0.jar'
JAVA = str(Path(os.environ['JAVA_HOME']) / 'bin/java') if 'JAVA_HOME' in os.environ else 'java'


def main():
    if not JAR.exists():
        raise SystemExit('Build the JAR first: mvn clean package')
    with tempfile.TemporaryDirectory(prefix='finance-smoke-') as temporary:
        base = Path(temporary)
        data = base / 'data'
        exports = base / 'exports'
        command = [JAVA, '-jar', str(JAR), '--data-dir', str(data), '--export-dir', str(exports)]
        def run(lines):
            result = subprocess.run(command, input='\n'.join(lines) + '\n', text=True,
                                    capture_output=True, cwd=base, timeout=30, encoding='utf-8')
            assert result.returncode == 0, result.stderr
            assert not result.stderr, result.stderr
            return result.stdout
        # Sample month and categories are stable, independent of the machine's current month.
        lines = ['1','250000','1','October salary','2026-10-01']
        for category, limit in [('1','50000'), ('2','20000'), ('3','60000')]:
            lines += ['5',category,'2026-10',limit]
        for amount, category, description in [('35000','1','Groceries, market'),
                                               ('18000','2','Transport'),('32000','3','Shopping')]:
            lines += ['2',amount,category,description,'2026-10-02']
        lines += ['11','8','2026-10']
        for format_number in ['1','2','3']:
            lines += ['9','2026-10',format_number]
        lines += ['2','5000','1','Cross Food threshold','2026-10-03',
                  '7','1','50','3','Monthly gift',date.today().isoformat(),'5','5','3','0',
                  '10','10','12','3','0']
        output = run(lines)
        assert 'Current balance: 165000.00 KZT' in output
        for expected in ['Usage: 70.00%', 'Usage: 90.00%', 'Usage: 53.33%',
                         'Food 2026-10 budget has reached 80%',
                         '1 due transactions processed.', '0 due transactions processed.',
                         '6 bank transactions imported. 0 duplicates skipped.',
                         '0 bank transactions imported. 6 duplicates skipped.']:
            assert expected in output, expected
        report = json.loads((exports / 'report-2026-10.json').read_text(encoding='utf-8'))
        assert report == {'period':'2026-10','year':2026,'month':10,'currency':'KZT',
                          'totalIncome':250000,'totalExpenses':85000,'netBalance':165000,
                          'transactionCount':4,'incomeByCategory':{'Salary':250000},
                          'expensesByCategory':{'Food':35000,'Transport':18000,'Shopping':32000}}
        rows = list(csv.DictReader(io.StringIO((exports / 'report-2026-10.csv').read_text(encoding='utf-8'))))
        assert any(r['section']=='netBalance' and r['value']=='165000.00' for r in rows)
        assert 'Food: 35000.00 KZT' in (exports / 'report-2026-10.txt').read_text(encoding='utf-8')
        before = (data / 'transactions.csv').read_bytes()
        recurring_before = (data / 'recurring_transactions.csv').read_bytes()
        reopened = run(['11','6','2026-10','7','5','0','10','0'])
        assert '0 due recurring transactions processed at startup.' in reopened
        assert '0 bank transactions imported. 6 duplicates skipped.' in reopened
        assert 'Food | 2026-10 | Limit: 50000.00 KZT' in reopened
        assert (data / 'transactions.csv').read_bytes() == before
        assert (data / 'recurring_transactions.csv').read_bytes() == recurring_before
        # Startup automatically processes a due recurring transaction created in an earlier run.
        run(['7','1','10','3','Startup gift',date.today().isoformat(),'0','0'])
        startup = run(['0'])
        assert '1 due recurring transactions processed at startup.' in startup
        assert '0 due recurring transactions processed at startup.' in run(['0'])
        # Corruption must stop startup and preserve the file.
        path = data / 'transactions.csv'
        broken = path.read_text(encoding='utf-8') + 'corrupt\n'
        path.write_text(broken, encoding='utf-8')
        failed = subprocess.run(command, input='0\n', text=True, capture_output=True, cwd=base, timeout=30)
        assert failed.returncode != 0 and 'corrupted CSV record' in failed.stderr
        assert path.read_text(encoding='utf-8') == broken
    print('PASS: packaged JAR, sample totals, budgets, all exports (JSON parsed), bank/recurring idempotency, restart, startup processing, corruption preservation.')

if __name__ == '__main__':
    main()
