#!/bin/bash
# ─────────────────────────────────────────────────────────
# SchemaGuard — Diff Checker
# Checks if changed files affect any schema dependency tree
#
# Usage:
#   ./tools/check_diff.sh                    # uses git diff vs HEAD~1
#   ./tools/check_diff.sh file1.kt file2.kt  # manual file list
# ─────────────────────────────────────────────────────────

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TREE_FILE="$SCRIPT_DIR/schema_trees.json"

if [ ! -f "$TREE_FILE" ]; then
    echo "ERROR: schema_trees.json not found at $TREE_FILE"
    echo "Run: ./gradlew buildDependencyTree --args=\"--save --output=tools\""
    exit 1
fi

# Get changed files
if [ $# -gt 0 ]; then
    CHANGED_FILES=$(printf '%s\n' "$@")
else
    echo "Getting changed files from git diff..."
    cd "$SCRIPT_DIR/.."
    CHANGED_FILES=$(git diff --name-only HEAD 2>/dev/null)
    if [ -z "$CHANGED_FILES" ]; then
        CHANGED_FILES=$(git diff --name-only HEAD~1 2>/dev/null)
    fi
fi

if [ -z "$CHANGED_FILES" ]; then
    echo "No changed files found."
    exit 0
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Changed Files:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "$CHANGED_FILES" | while read -r f; do
    echo "  - $f"
done
echo ""

# Check against schema tree using python3
MATCH_RESULT=$(TREE_FILE="$TREE_FILE" CHANGED_FILES="$CHANGED_FILES" python3 << 'PYEOF'
import json, os

tree_file = os.environ["TREE_FILE"]
changed_raw = os.environ["CHANGED_FILES"]

with open(tree_file) as f:
    data = json.load(f)

changed = [c.strip() for c in changed_raw.strip().split('\n') if c.strip()]

found_any = False
for entry_name, entry in data.items():
    downstream = entry.get("downstream_files", [])
    matches = []
    for changed_file in changed:
        changed_short = changed_file.split('/')[-1]
        for dep in downstream:
            dep_short = dep.split('/')[-1]
            if changed_short == dep_short:
                matches.append((changed_file, dep))

    if matches:
        found_any = True
        print(f"  MATCH [{entry_name}]:")
        for ch, dep in matches:
            print(f"    {ch}  -->  {dep}")
        print()

if not found_any:
    print("  No schema trees affected.")
PYEOF
)

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Result:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "$MATCH_RESULT"
echo ""

if echo "$MATCH_RESULT" | grep -q "MATCH"; then
    echo "=> ACTION NEEDED: Run schema regeneration!"
    exit 2
else
    echo "=> All clear — no schema changes needed."
    exit 0
fi
