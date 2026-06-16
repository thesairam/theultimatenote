# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

# Developer Guide

## Build & Run Commands

```bash
# Android debug build
./gradlew assembleDebug

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.theultimatenote.SomeTest"

# iOS simulator tests (Apple Silicon)
./gradlew iosSimulatorArm64Test

# Lint checks
./gradlew ktlintCheck
./gradlew detekt

# Clean build
./gradlew clean assembleDebug
```

## Project Structure

```
theultimatenote/
├── composeApp/          # Shared KMP module (Compose Multiplatform UI + shared logic)
│   └── src/
│       ├── commonMain/  # Shared code (models, repositories, viewmodels, UI screens)
│       ├── androidMain/ # Android-specific implementations (Firebase, notifications)
│       └── iosMain/     # iOS-specific implementations
├── androidApp/          # Android app entry point (MainActivity, Application class)
├── iosApp/              # iOS app entry point (Xcode project)
├── gradle/              # Gradle wrapper and version catalog (libs.versions.toml)
├── CLAUDE.md            # This file — product spec + developer guide
└── CLAUDE.original.md   # Backup of original product spec
```

## Architecture Overview

- **MVVM** pattern: ViewModels hold UI state, Repositories handle data
- **Shared UI** via Compose Multiplatform in `composeApp/src/commonMain/`
- **Platform-specific** Firebase/notification code in `androidMain/` and `iosMain/` via `expect`/`actual`
- **Navigation**: 4 bottom tabs — Home, Projects, Daily Dashboard, Notebooks
- **Special projects** (Daily, Learning) are system-created and non-deletable
- **Firestore** as primary DB, all data scoped under user UID
- **Offline-first**: Firestore persistence enabled by default

## Development Environment

- Project runs in **WSL Linux** on Windows
- Testing via **Android Studio emulator** on Windows host
- Dockerized for portability

## Key Conventions

- Kotlin Multiplatform with Compose Multiplatform for shared UI
- Firebase services accessed via platform `expect`/`actual` declarations
- Gemini API for AI features (notebook generation, chat)
- Color theme: Deep Forest Green (primary), Sage Green (secondary), Warm Beige (accent), Soft Cream (background), Muted Gold (highlights)

---

# Product Specification

> Everything below is the original product spec — the source of truth for what to build.

---

## Project Vision

I want to build a mobile app that is for Journelling and my TO-do planner.

Most of apps are either confusing, has too much or misses components.

I want a simple app that combines Kanban board and a scribble app allows to take notes.

---

# Core Features

The app should have the following features

---

# Home Page

User can create a task quickly and add it to one of projects they have with a + button.

This also displays a summarizes on-going tasks for the day from different projects and daily project.

### Additional Notes

* Home page should load fast and prioritize today's actions.
* Quick-add task flow should require minimal taps.
* Home page acts as the user's command center.

---

# Project Page

Here is where people can see their projects, create teir projects. Once project is created, people can build a custom kanban board, assign and move tasks.

This is what i was referring to the home page + button to add tasks, acting as shortcuts

Each project creation auto-creates a notebook.

Notebook is simply a book with pages, each page has a headr and content and give basic formatting options that's enough.

### Basic Formatting Options

* Headings
* Bold
* Italics
* Bullet Lists
* Numbered Lists
* Checklists
* Links
* Code Block (optional)

---

# Special Project: Daily

There is a special project called Daily where it focusses on tasks that are temporary only today or recurring.

It is created by default.

A default kanban board with two sections:

* recurring
* temporary

Users cant delete it.

This has a notebook by default that is for journelling.

### Daily Task Behaviour

People can add quick tasks, view their recurring and temp tasks and mark them if complete for the day.

If recurring tasks, it shoudl re-appear the next day.

People can set specific time for these tasks, and app should notify people when the time comes.

Example:

* reminder for your daily task- go to gym!

### Important

These should reflect automatically in respective kanban default boards.

---

# Special Project: Learning

There is another special project called 'Learning'

this focusses on managing their learning stuffs- like a course or something.

This is also there by default.

Here people can view default kanban with Learning cards for multiple learning paths (default to 2) and card to mark completed.

So 3 cards in total:

* 2 for default exampel fo 2 learning path (for example AI course etc)
* one for completed

This will have a default notebook as well and sections per card (or learning path).

### Learning Behaviour

Similarly for learning, they can view what thez need tolearn today and mark completed.

### Important

These should reflect automatically in respective kanban default boards.

---

# Daily vs Learning

I am thinking of combing daily and learning project or may be not.

### Recommendation

Keep them separate initially.

Reasons:

* Different use cases
* Easier UX
* Cleaner notifications
* Easier future scaling

Can be merged later if user feedback suggests it.

---

# Third Section: Daily Dashboard

Third section is Daily, where they can view about their daily project and learning project.

it should not be visible in project page as this is more active and need quick access.

people can add quick tasks, view their recurring and temp tasks and mark them if complete for the day.

If recurring tasks, it shoudl re-appear the next day.

People can set specific time for these tasks, and app should notify people when the time comes.

Simialry for learning, they can view what thez need tolearn today and mark completed.

### Importantlz

these should reflect automatically in respective kanban default boards.

---

# Notebook Section

Finally, notebook section

Where people can journel their ideas, stories life etc.

every project has its own notebook, hwoever people can create more notebooks and journal if they want.

a cool feautre is to when creating a notebook with project, auto populate with right sections as per the project kanban creation.

### Auto Population Examples

Project:

* Planning
* In Progress
* Review
* Done

Notebook Auto Sections:

* Planning Notes
* In Progress Notes
* Review Notes
* Done Notes

---

# Authentication Features

Other features must have

Signup, signin feature to login into the app

ability to login/signgup with gmail

every transition and error should be handled correctly here between:

* sign in
* login
* logout
* signup
* acconunt creation from google
* login with google

and other features like forgot password etc should exist and work properly with correct flow and error handling and simplify messages to users like if user exists or not etc.

Refer to sSSSNL for these features.

### Authentication Requirements

* Secure authentication
* Session persistence
* Token refresh handling
* Proper logout
* Password reset
* Email verification
* Account recovery
* Friendly user messaging
* Edge case handling

---

# User Profile

User can fill up a cool profile with many optional stuff

### Profile Fields

Name, contact details

Hobbies

Areas of focus

* professional
* personal
* industrial areas
* etc

Skillset

* show off

Favourtie inspiration people

* idols

Depending on idol, give daily push notification with a famous quote by idol in morning!

motivate people.

### Additional Profile Ideas

* Profile picture
* Personal bio
* Goals
* Current learning focus
* Achievement badges

---

# AI Features

Probably you need AI API to create notebooks with prefileld content?

or can it be rule based?

if yes, gemini is free lets use it.

### AI Notebook Creation

When project is created:

* Generate notebook structure
* Generate suggested sections
* Generate starter content
* Generate planning templates

### AI Chat

We should have chat option in home page as well,

so people can chat with AI a bit for ideation,

and default it to gemini.

May be in future, we can use this chat to be agentic and can auto create projects, tasks etc.

but not now.

### Future Agentic Features

* Create projects from conversations
* Create tasks from conversations
* Create notebooks from conversations
* Weekly planning assistant
* Learning path generation

---

# Notifications

### Daily Notifications

* Task reminders
* Recurring task reminders
* Learning reminders
* Morning motivation quotes

### Future Notifications

* Weekly review reminder
* Project deadline reminder
* Learning streak reminder

---

# Tech

I want both androd and iphone app, so kotlin would be preffered

### Recommended Stack

Frontend:

* Kotlin Multiplatform (KMP)

Android:

* Jetpack Compose

iOS:

* Compose Multiplatform or Native Swift UI bridge

Backend:

* Firebase

Authentication:

* Firebase Auth
* Google Sign-In

Database:

* Firestore

Storage:

* Firebase Storage

Notifications:

* Firebase Cloud Messaging

AI:

* Gemini API

Analytics:

* Firebase Analytics

Crash Reporting:

* Firebase Crashlytics


---

# UI / UX

Keep the interface really simple, cool and modern stylish.

Choose theme that is cozy colour vibes and trust vibes.

### Design Principles

* Minimal clicks
* Clean typography
* Fast interactions
* Smooth animations
* Modern cards
* Kanban-first workflow
* Journaling-first experience
* Mobile-first design

### Suggested Color Direction

Primary:

* Deep Forest Green

Secondary:

* Sage Green

Accent:

* Warm Beige

Background:

* Soft Cream

Highlights:

* Muted Gold

Feeling:

* Cozy
* Trustworthy
* Calm
* Productive
* Modern

### General good practices

* Dockerize the application for ease. 
* Use the empty repo I created for version  control and repo management
https://github.com/thesairam/theultimatenote
* I will test the app in my android studio emulator in windows machine. And the project is in wsl linux environment on windows.


---

# MVP Scope

Version 1 should include:

* Authentication
* Home Page
* Projects
* Kanban Boards
* Daily Project
* Learning Project
* Notebook System
* User Profiles
* Notifications
* Gemini Chat
* Firebase Backend

Everything else can be added after MVP validation.

---

# Success Criteria

The app should feel like:

"Notion is too much."

"Trello is too little."

'To do microsoft is too little'

"This app is exactly enough."

A simple combination of:

* Kanban
* Journelling
* Learning
* Daily Planning
* Motivation
* AI Assistance

all in one clean mobile experience.


# Integration & Security Reference Rule

## SSSSNL Reference Principle

SSSSSNL is a reference project only for mature implementation patterns related to integrations, security, authentication, error handling, and user experience flows.

It should NOT be used as a reference for:

* Business logic
* Product features
* UI/UX design
* Project architecture
* Database schema
* Kanban implementation
* Notebook implementation
* Learning workflows
* Daily planning workflows
* AI features

These should be designed specifically for this application.

## Areas Where SSSNL Can Be Referenced

### Authentication

* Signup
* Signin
* Logout
* Forgot Password
* Reset Password
* Email Verification
* Google Login
* Google Signup
* Session Management
* Token Handling
* Account Recovery

### Security

* Secure API communication
* Authentication middleware
* Authorization patterns
* Secure credential storage
* Secrets management
* Rate limiting
* Security best practices

### Integrations

* Firebase integration patterns
* Google OAuth flows
* Push notification setup
* Third-party API integration patterns
* Analytics integration patterns
* Monitoring and logging practices

### Error Handling & UX Flows

* User-friendly error messages
* Authentication edge cases
* Network failure handling
* Session expiry handling
* Retry mechanisms
* Loading states
* Success and failure flows

## Implementation Guidance

When implementing integrations or security-sensitive features:

1. Use SSSNL as a reference for proven integration patterns and security practices.
2. Adapt the implementation to the needs of this application.
3. Do not copy business logic or architecture from SSSNL.
4. Keep the implementation lightweight and appropriate for a mobile-first productivity application.
5. If a better modern approach exists, prefer the modern approach while maintaining the same level of security and reliability.

The goal is to leverage lessons learned from SSSNL for integrations, security, and reliability while designing all product functionality specifically for this application.
