#!/usr/bin/env bash
# coverage-gate.sh — Parse JaCoCo XML, apply REQ-008 exclusions, report coverage.
# Soft warning (exit 0) unless the report file is missing (exit 1).
# Usage: ./scripts/coverage-gate.sh [path-to-jacoco-xml]

set -euo pipefail

DEFAULT_REPORT="app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
REPORT="${1:-$DEFAULT_REPORT}"

if [ ! -f "$REPORT" ]; then
  echo "ERROR: Coverage report not found at $REPORT"
  echo "Run ./gradlew jacocoTestReport first."
  exit 1
fi

# REQ-008 exclusion patterns — files excluded from the coverage denominator.
# These match the class name attributes in the JaCoCo XML.
EXCLUDE_PATTERNS=(
  'ui/screens/'
  'ui/components/'
  'ui/theme/'
  'ui/navigation/AppNavGraph'
  'ui/navigation/NavigationAnimations'
  'CrowdSecApplication'
  'MainActivity'
  'di/DatabaseModule'
  'di/DataStoreModule'
  'data/db/AppDatabase'
  'data/db/CSServerModel'
  'data/db/CSServerDao'
  'data/repository/PreferencesRepository'
  'data/repository/ServerRepository'
  'utils/Geocode'
  'utils/InstalledOutsideGooglePlayCheck'
  'utils/ThemePreferences'
  'constants/ChartColors'
  'constants/Defaults'
  'constants/StorageKeys'
  'utils/DashboardItemModels'
)

# Build an XPath expression to exclude all patterns.
XPATH_EXCLUDE=""
for pattern in "${EXCLUDE_PATTERNS[@]}"; do
  if [ -n "$XPATH_EXCLUDE" ]; then
    XPATH_EXCLUDE+=" and "
  fi
  XPATH_EXCLUDE+="not(contains(@name, '$pattern'))"
done

# Extract per-metric coverage from the XML, excluding REQ-008 patterns.
# The JaCoCo XML has <counter type="LINE" missed="..." covered="..."/> elements inside <class> elements.
get_metric() {
  local metric="$1"
  # Use xmlstarlet or python to parse the XML.
  if command -v xmlstarlet &>/dev/null; then
    xmlstarlet sel -t \
      -m "//class[$XPATH_EXCLUDE]/counter[@type='$metric']" \
      --var missed='sum(@missed)' \
      --var covered='sum(@covered)' \
      -o "" -v "sum(//class[$XPATH_EXCLUDE]/counter[@type='$metric']/@covered)" \
      -o " " -v "sum(//class[$XPATH_EXCLUDE]/counter[@type='$metric']/@missed)" \
      -n "$REPORT"
  elif command -v python3 &>/dev/null; then
    python3 -c "
import xml.etree.ElementTree as ET
import sys

tree = ET.parse('$REPORT')
root = tree.getroot()

ns = {'ns': 'http://www.jacoco.org/ns' if root.tag.startswith('{') else ''}
# Handle both namespaced and non-namespaced XML
if root.tag.startswith('{'):
    ns_prefix = root.tag.split('}')[0] + '}'
else:
    ns_prefix = ''

exclude_patterns = $(printf '%s\n' "${EXCLUDE_PATTERNS[@]}" | python3 -c 'import sys,json; print(json.dumps([l.strip() for l in sys.stdin if l.strip()]))')

total_covered = 0
total_missed = 0
tag = ns_prefix + 'class' if ns_prefix else 'class'
counter_tag = ns_prefix + 'counter' if ns_prefix else 'counter'

for cls in root.iter(tag):
    name = cls.get('name', '')
    excluded = False
    for pat in exclude_patterns:
        if pat in name:
            excluded = True
            break
    if excluded:
        continue
    for counter in cls.iter(counter_tag):
        if counter.get('type') == '$metric':
            total_covered += int(counter.get('covered', 0))
            total_missed += int(counter.get('missed', 0))

if total_covered + total_missed == 0:
    print('0 0')
else:
    pct = round(100.0 * total_covered / (total_covered + total_missed), 1)
    print(f'{pct} {total_covered} {total_covered + total_missed}')
"
  else
    echo "ERROR: Need xmlstarlet or python3 to parse XML"
    exit 1
  fi
}

echo "=== Coverage Gate (REQ-008 exclusions applied) ==="

declare -A metrics
metrics=(
  ["LINE"]="Line"
  ["BRANCH"]="Branch"
  ["METHOD"]="Method"
  ["STATEMENT"]="Statement"
)

all_pass=true
for type in LINE BRANCH METHOD STATEMENT; do
  label="${metrics[$type]}"
  result=$(get_metric "$type" 2>/dev/null)
  
  if [ -z "$result" ] || [ "$result" = "0 0" ]; then
    echo "  $label: No data (all excluded or zero)"
    continue
  fi
  
  read -r pct covered total <<< "$result"
  
  if (( $(echo "$pct < 80.0" | bc -l 2>/dev/null || echo 1) )); then
    echo "  ⚠ $label: ${pct}% (${covered}/${total}) — BELOW 80% threshold"
    all_pass=false
  elif (( $(echo "$pct < 90.0" | bc -l 2>/dev/null || echo 1) )); then
    echo "  ✓ $label: ${pct}% (${covered}/${total}) — ✓ ≥80%, progress toward 90%"
  else
    echo "  ✓ $label: ${pct}% (${covered}/${total}) — ✓ ≥90% desirable target"
  fi
done

echo ""
if [ "$all_pass" = false ]; then
  echo "⚠ WARNING: Some metrics below 80% — soft warning per Clarifications."
else
  echo "✓ All metrics ≥80% — coverage gate passed (soft check)."
fi
echo "Coverage gate exiting 0 (soft warning — does not block build)."
exit 0
