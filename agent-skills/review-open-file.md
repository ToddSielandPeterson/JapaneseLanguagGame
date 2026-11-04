# Review the currently open file

Fill the sections, then press **Execute**.

## Goal
Actionable review of the file that is open in the editor: bugs, missing tests, risky APIs.

## Scope
Only the currently open editor file. Do not rewrite other files.

## Constraints
- Do not rewrite the file unless a defect is clear.
- Prefer comments in the response over drive-by style changes.
- Match existing style.

## Steps
1. Read the open file.
2. List findings by severity (blocker / should-fix / nit).
3. Suggest a small fix only where a defect is clear.

## Done when
- [ ] Findings are listed by severity with file/line references
