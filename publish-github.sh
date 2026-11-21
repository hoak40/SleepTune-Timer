#!/usr/bin/env bash
set -euo pipefail
if ! command -v gh >/dev/null || ! command -v git >/dev/null; then
  echo 'Install Git and GitHub CLI first: https://cli.github.com/' >&2
  exit 1
fi
if ! gh auth status >/dev/null 2>&1; then
  echo 'Run: gh auth login' >&2
  exit 1
fi
cd "$(dirname "$0")"
if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  git init -b main
fi
git add .
if ! git diff --cached --quiet; then
  git commit -m 'Initial SleepTune Timer Android app'
fi
ACCOUNT="$(gh api user --jq '.login')"
if gh repo view "$ACCOUNT/SleepTune-Timer" >/dev/null 2>&1; then
  git remote remove origin 2>/dev/null || true
  git remote add origin "https://github.com/$ACCOUNT/SleepTune-Timer.git"
  git push -u origin main
else
  gh repo create "$ACCOUNT/SleepTune-Timer" --public --source=. --remote=origin --push \
    --description 'Offline Android timer to pause background music'
fi
echo "Repository: https://github.com/$ACCOUNT/SleepTune-Timer"
