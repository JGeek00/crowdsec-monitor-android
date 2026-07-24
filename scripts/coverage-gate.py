#!/usr/bin/env python3
"""coverage-gate.py — Parse JaCoCo XML, apply REQ-008 exclusions, report coverage.
Soft warning (exit 0) unless the report file is missing (exit 1).
Usage: ./scripts/coverage-gate.py [path-to-jacoco-xml]
"""
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

DEFAULT_REPORT = Path("app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
report_path = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_REPORT

if not report_path.exists():
    print(f"ERROR: Coverage report not found at {report_path}")
    print("Run ./gradlew jacocoTestReport first.")
    sys.exit(1)

EXCLUDE_PATTERNS = [
    'ui/screens/', 'ui/components/', 'ui/theme/',
    'ui/navigation/AppNavGraph', 'ui/navigation/NavigationAnimations',
    'CrowdSecApplication', 'MainActivity',
    'di/DatabaseModule', 'di/DataStoreModule',
    'data/db/AppDatabase', 'data/db/CSServerModel', 'data/db/CSServerDao',
    'data/repository/PreferencesRepository', 'data/repository/ServerRepository',
    'utils/Geocode', 'utils/InstalledOutsideGooglePlayCheck', 'utils/ThemePreferences',
    'constants/ChartColors', 'constants/Defaults', 'constants/StorageKeys',
    'utils/DashboardItemModels',
]

tree = ET.parse(report_path)
root = tree.getroot()
ns_prefix = ''
if root.tag.startswith('{'):
    ns_prefix = root.tag.split('}')[0] + '}'
tag_cls = ns_prefix + 'class'
tag_ctr = ns_prefix + 'counter'

print("=== Coverage Gate (REQ-008 exclusions applied) ===")

METRICS = {'INSTRUCTION': 'Instruction', 'BRANCH': 'Branch', 'LINE': 'Line', 'METHOD': 'Method'}
all_pass = True

for metric, label in METRICS.items():
    total_covered = 0
    total_missed = 0
    for cls in root.iter(tag_cls):
        name = cls.get('name', '')
        if any(p in name for p in EXCLUDE_PATTERNS):
            continue
        for counter in cls.iter(tag_ctr):
            if counter.get('type') == metric:
                total_covered += int(counter.get('covered', 0))
                total_missed += int(counter.get('missed', 0))
    total = total_covered + total_missed
    if total == 0:
        print(f"  {label}: No data (all excluded or zero)")
        continue
    pct = round(100.0 * total_covered / total, 1)
    if pct < 80.0:
        print(f"  ⚠ {label}: {pct}% ({total_covered}/{total}) — BELOW 80% threshold")
        all_pass = False
    elif pct < 90.0:
        print(f"  ✓ {label}: {pct}% ({total_covered}/{total}) — ✓ ≥80%, progress toward 90%")
    else:
        print(f"  ✓ {label}: {pct}% ({total_covered}/{total}) — ✓ ≥90% desirable target")

print()
if not all_pass:
    print("⚠ WARNING: Some metrics below 80% — soft warning per Clarifications.")
else:
    print("✓ All metrics ≥80% — coverage gate passed (soft check).")
print("Coverage gate exiting 0 (soft warning — does not block build).")
sys.exit(0)
