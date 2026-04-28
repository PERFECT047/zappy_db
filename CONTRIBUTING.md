# 🤝 Contributing to ZappyDB

Thanks for your interest in contributing!

## 🔁 Branching Strategy

We follow a structured branching model:

- `main` → Production-ready, stable code
- `dev-test` → Integration branch for all contributions
- Feature branches → Created from `dev-test`

---

## 🚨 Pull Request Rules

### 1. Target Branch
- All contributions MUST target: `dev-test`
- PRs directly to `main` will be rejected

### 2. Merge Policy
- `main` can ONLY be updated via:
  dev-test → main

### 3. Code Owner Approval
- Every PR requires approval from designated code owners before merging

---

## 📝 Pull Request Requirements

Each PR MUST include:

### ✔ Description
- Clear explanation of changes

### ✔ Linked Issue
- Reference the issue being resolved
  Fixes: #<issue-number> / Issue: #<issue-number>

### ✔ Changes Made
- Bullet list of key modifications
- Example:
    - Added TTL optimization
    - Refactored list storage locking
    - Improved RESP parsing performance

---

## 🧪 Testing

Before submitting a PR:

```bash
./run_tests.sh