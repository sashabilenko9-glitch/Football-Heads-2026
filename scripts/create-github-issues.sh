#!/usr/bin/env bash
# Bulk-creates one GitHub milestone per ROADMAP.md section and one issue per
# unchecked checklist item in it, so the roadmap doubles as a tracked backlog
# instead of just a markdown wishlist.
#
# Requires: `gh` CLI, authenticated (`gh auth login`), run from inside a clone
# whose `origin` remote is this repo on GitHub.
#
# Usage:
#   ./scripts/create-github-issues.sh          # create everything
#   ./scripts/create-github-issues.sh --dry-run  # print what would be created

set -euo pipefail

cd "$(dirname "$0")/.."

ROADMAP_FILE="ROADMAP.md"
DRY_RUN=false
if [[ "${1:-}" == "--dry-run" ]]; then
  DRY_RUN=true
fi

if ! command -v gh >/dev/null 2>&1; then
  echo "error: the GitHub CLI ('gh') is not installed. See https://cli.github.com/" >&2
  exit 1
fi

if ! $DRY_RUN && ! gh auth status >/dev/null 2>&1; then
  echo "error: gh is not authenticated. Run 'gh auth login' first (or pass --dry-run)." >&2
  exit 1
fi

# Parses ROADMAP.md into "<milestone>\t<issue title>" lines, one per unchecked
# checklist item. Continuation lines (wrapped text indented under a "- [ ]")
# are folded back into a single title.
parse() {
  awk '
    /^## / {
      milestone = $0
      sub(/^## /, "", milestone)
      next
    }
    /^- \[ \] / {
      if (item != "") print milestone "\t" item
      item = $0
      sub(/^- \[ \] /, "", item)
      next
    }
    /^      [^ ]/ {
      if (item != "") {
        line = $0
        sub(/^ +/, "", line)
        item = item " " line
      }
      next
    }
    {
      if (item != "") print milestone "\t" item
      item = ""
    }
    END {
      if (item != "") print milestone "\t" item
    }
  ' "$ROADMAP_FILE"
}

declare -A milestone_numbers

ensure_milestone() {
  local title="$1"
  if [[ -n "${milestone_numbers[$title]:-}" ]]; then
    return
  fi

  if $DRY_RUN; then
    echo "[dry-run] milestone: $title"
    milestone_numbers["$title"]="DRY"
    return
  fi

  local existing
  existing=$(gh api "repos/{owner}/{repo}/milestones?state=all" \
    --jq ".[] | select(.title == \"$title\") | .number" | head -n1)

  if [[ -n "$existing" ]]; then
    milestone_numbers["$title"]="$existing"
  else
    local number
    number=$(gh api "repos/{owner}/{repo}/milestones" -f "title=$title" --jq '.number')
    echo "Created milestone: $title (#$number)"
    milestone_numbers["$title"]="$number"
  fi
}

create_issue() {
  local milestone="$1"
  local title="$2"

  if $DRY_RUN; then
    echo "[dry-run] issue: [$milestone] $title"
    return
  fi

  local number="${milestone_numbers[$milestone]}"
  gh issue create \
    --title "$title" \
    --body "From ROADMAP.md ($milestone). See CONTRIBUTING.md for the dev workflow." \
    --milestone "$number" \
    >/dev/null
  echo "Created issue: [$milestone] $title"
}

while IFS=$'\t' read -r milestone title; do
  [[ -z "$milestone" || -z "$title" ]] && continue
  ensure_milestone "$milestone"
  create_issue "$milestone" "$title"
done < <(parse)
