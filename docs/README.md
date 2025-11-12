# Duka.AI Documentation

Welcome to the Duka.AI documentation! This directory contains comprehensive documentation for developers, LLMs, and contributors.

---

## 📚 Documentation Index

### Getting Started

| Document | Description | Audience |
|----------|-------------|----------|
| [../README.md](../README.md) | **Project Overview** - Start here for a high-level introduction | Everyone |
| [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md) | **Complete Context for LLMs** - Master reference with all key information | LLMs, New Developers |
| [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) | **Development Setup & Guidelines** - How to build and run the app | Developers |

### Architecture & Design

| Document | Description | Audience |
|----------|-------------|----------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | **System Architecture** - Clean architecture, MVVM, design patterns | Developers, Architects |
| [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) | **Database Design** - Room entities, DAOs, relationships, queries | Backend Developers |
| [AI_MODELS.md](AI_MODELS.md) | **AI/ML Implementation** - TFLite models, training, integration | ML Engineers |

### Planning & Roadmap

| Document | Description | Audience |
|----------|-------------|----------|
| [ROADMAP.md](ROADMAP.md) | **Development Timeline** - Phases, milestones, features | Everyone |
| User Stories (from main doc) | **Feature Specifications** - Detailed user stories and acceptance criteria | Product, Developers |

---

## 🚀 Quick Navigation by Role

### For New Developers
1. Read [../README.md](../README.md) - Understand what Duka.AI is
2. Read [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) - Set up your environment
3. Read [ARCHITECTURE.md](ARCHITECTURE.md) - Understand the codebase structure
4. Check [ROADMAP.md](ROADMAP.md) - See what's in progress
5. Start coding! 💻

### For LLM Assistants
1. **Primary Reference:** [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md) - Everything you need to know
2. **Deep Dives:** Reference specific docs as needed
   - Architecture questions → [ARCHITECTURE.md](ARCHITECTURE.md)
   - Database questions → [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md)
   - ML questions → [AI_MODELS.md](AI_MODELS.md)

### For Product Managers
1. Read [../README.md](../README.md) - Product overview
2. Read user stories (in main project description) - Feature details
3. Check [ROADMAP.md](ROADMAP.md) - Timeline and priorities

### For ML Engineers
1. Read [AI_MODELS.md](AI_MODELS.md) - Model specs and training
2. Check `../ml/` directory - Training scripts
3. Review [ARCHITECTURE.md](ARCHITECTURE.md) - ML layer integration

---

## 📖 Documentation Guidelines

### When to Update Documentation

- ✅ **Always** update docs when:
  - Adding new features
  - Changing architecture
  - Modifying database schema
  - Updating dependencies
  - Changing roadmap

- ⚠️ **Consider** updating docs when:
  - Fixing bugs (if it reveals design issues)
  - Refactoring code (if it changes patterns)
  - Learning lessons (add to best practices)

### How to Update Documentation

1. **Find the right document** - Use index above
2. **Make changes** - Update relevant sections
3. **Update "Last Updated" date** - At bottom of document
4. **Commit with clear message** - `docs: Update ARCHITECTURE.md with new ViewModel pattern`

---

## 🗂️ File Structure

```
docs/
├── README.md                    # This file (documentation index)
├── PROJECT_CONTEXT.md           # Master reference for LLMs
├── ARCHITECTURE.md              # System architecture
├── DATABASE_SCHEMA.md           # Database design
├── AI_MODELS.md                 # ML models & training
├── DEVELOPMENT_GUIDE.md         # Development setup
├── ROADMAP.md                   # Timeline & milestones
└── assets/                      # Documentation assets
    └── diagrams/                # Architecture diagrams
```

---

## 📝 Document Templates

### Feature Documentation Template

When adding a new feature, document it with:

```markdown
## Feature Name

### Overview
Brief description of what the feature does.

### User Story
As a [user type], I want to [action], so that [benefit].

### Implementation
- **UI Layer:** Path to Compose screens
- **ViewModel:** Path to ViewModel
- **Use Case:** Path to use case
- **Repository:** Path to repository
- **Database:** Relevant tables/DAOs

### Key Files
- `path/to/Screen.kt`
- `path/to/ViewModel.kt`
- `path/to/UseCase.kt`

### Testing
- Unit tests: `path/to/test`
- Instrumented tests: `path/to/androidTest`

### Known Issues
- [ ] Issue 1
- [ ] Issue 2
```

---

## 🔍 Finding Information

### "I want to understand..."

| Topic | Document | Section |
|-------|----------|---------|
| Overall project goals | ../README.md | Vision & Key Features |
| How to set up dev environment | DEVELOPMENT_GUIDE.md | Environment Setup |
| App architecture patterns | ARCHITECTURE.md | Layer Breakdown |
| Database tables and relationships | DATABASE_SCHEMA.md | Table Definitions |
| How ML models work | AI_MODELS.md | Product Recognition Model |
| What features are planned | ROADMAP.md | Phase 1-6 |
| Complete project context (LLMs) | PROJECT_CONTEXT.md | All sections |

### "I want to implement..."

| Feature | Start Here | Related Docs |
|---------|-----------|--------------|
| New screen | DEVELOPMENT_GUIDE.md → "Add a New Screen" | ARCHITECTURE.md |
| Database entity | DEVELOPMENT_GUIDE.md → "Add a New Database Entity" | DATABASE_SCHEMA.md |
| Business logic | ARCHITECTURE.md → "Domain Layer" | PROJECT_CONTEXT.md |
| ML model update | AI_MODELS.md → "Model Updates" | - |
| Localization | DEVELOPMENT_GUIDE.md → "Add Localization" | PROJECT_CONTEXT.md |

---

## 🤝 Contributing to Documentation

### Reporting Issues
- **Outdated info:** Open issue with `docs-outdated` label
- **Missing info:** Open issue with `docs-missing` label
- **Unclear sections:** Open issue with `docs-clarity` label

### Making Improvements
1. Fork repository
2. Update documentation
3. Submit pull request with:
   - Clear description of changes
   - Why the change is needed
   - Any related issues

---

## 📊 Documentation Status

| Document | Last Updated | Status | Completeness |
|----------|--------------|--------|--------------|
| README.md | Nov 11, 2024 | ✅ Complete | 100% |
| PROJECT_CONTEXT.md | Nov 11, 2024 | ✅ Complete | 100% |
| ARCHITECTURE.md | Nov 11, 2024 | ✅ Complete | 100% |
| DATABASE_SCHEMA.md | Nov 11, 2024 | ✅ Complete | 100% |
| AI_MODELS.md | Nov 11, 2024 | ✅ Complete | 100% |
| DEVELOPMENT_GUIDE.md | Nov 11, 2024 | ✅ Complete | 100% |
| ROADMAP.md | Nov 11, 2024 | ✅ Complete | 100% |

---

## 🔗 External Resources

### Android Development
- [Android Developer Guide](https://developer.android.com/)
- [Jetpack Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)
- [Kotlin Documentation](https://kotlinlang.org/docs/)

### Architecture
- [Guide to App Architecture](https://developer.android.com/topic/architecture)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)

### Machine Learning
- [TensorFlow Lite for Android](https://www.tensorflow.org/lite/android)
- [ML Kit for Android](https://developers.google.com/ml-kit)

---

## ❓ FAQ

### Q: Where do I start if I'm a new developer?
**A:** Start with [../README.md](../README.md), then [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md), then [ARCHITECTURE.md](ARCHITECTURE.md).

### Q: How can an LLM quickly understand this project?
**A:** Read [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md) - it's specifically designed as a master reference for LLMs.

### Q: Where are the user stories?
**A:** Currently in the main project description provided by the user. We'll create a dedicated FEATURE_SPECS.md in Phase 2.

### Q: How do I propose a new feature?
**A:** Check [ROADMAP.md](ROADMAP.md) to see if it's planned, then open a GitHub issue with the `feature-request` label.

### Q: The documentation is outdated, what should I do?
**A:** Open an issue with the `docs-outdated` label, or submit a PR with updates.

### Q: Can I improve the documentation?
**A:** Absolutely! We welcome documentation improvements. See "Contributing to Documentation" above.

---

## 📧 Contact

For documentation-related questions:
- **GitHub Issues:** [Open an issue](https://github.com/yourusername/dukaai/issues) with `docs` label
- **Email:** docs@dukaai.app
- **Team:** Contact the development team

---

**Happy coding! 🚀**

*Building the future of Zambian retail, one line of code at a time.*

---

**Last Updated:** November 11, 2024
**Maintained by:** Duka.AI Documentation Team
