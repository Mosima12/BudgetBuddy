# 📱 Ubuntu Budget App

## 🧾 Overview

Ubuntu Budget is a mobile budgeting application developed using Kotlin in Android Studio. The app is designed to help users manage their finances effectively by tracking expenses, organizing spending into categories, and visualizing financial habits through interactive graphs.

The application follows the principles of the Ubuntu philosophy: **balance, responsibility, and mindful living**, encouraging users to take control of their financial well-being.

---

## 🚀 Features

### 🔐 User Authentication

* Register and login functionality
* Secure password storage using hashing (SHA-256)
* Session management using SharedPreferences

### 💰 Expense Management

* Add, update, and delete expenses
* Categorize expenses (e.g., Food, Transport, Entertainment)
* Select date for each transaction

### 🧾 Digital Receipt Vault

* Capture and store receipt images using device camera
* Attach receipts to expenses for record-keeping

### 📊 Budget Tracking

* Set monthly budget goals (minimum and maximum)
* Track spending against budget
* Visual progress indicators

### 📈 Data Visualization

* Pie chart displaying spending by category
* Monthly spending analysis using graphs

### 🧮 Smart Budgeting (Envelope System)

* Divide budget into categories (spending envelopes)
* Monitor category-level spending

---

## 🛠 Technologies Used

* **Kotlin** – Primary programming language
* **Android Studio** – Development environment
* **Room Database** – Local data storage
* **Coroutines** – Asynchronous programming
* **ViewBinding** – UI binding
* **MPAndroidChart** – Graph and chart visualization
* **CameraX** – Camera integration for receipts

---

## 🏗 Architecture

The application follows a structured architecture with clear separation of concerns:

### 📂 Data Layer

* Entities (Category, Expense, BudgetGoal)
* DAO interfaces for database operations
* Repository pattern for data handling
* Room Database for persistence

### 📂 UI Layer

* Activities (Login, Dashboard, Add Expense, Graph)
* RecyclerView with adapters for lists
* XML layouts for UI design

### 📂 Utilities

* Password hashing (SHA-256)
* Date converters for Room database

---

## 🔄 Application Workflow

1. User logs in or registers
2. Dashboard displays spending summary
3. User adds expenses and assigns categories
4. Data is stored locally using Room Database
5. Graphs visualize spending patterns
6. Budget progress is calculated and displayed

---



## 🎥 Video Demonstration


The video showcases:

* User login and registration
* Adding expenses
* Capturing receipts
* Viewing dashboard and graphs
* Budget tracking features

---

## 📄 Documentation

This project includes the following supporting documents:

* Research Document (Part 1)
* System Design and Planning


---

## 🧪 Testing

* Unit testing using JUnit
* UI testing using Espresso
* Manual testing for user interactions

---

## 📚 References

* Android Developers. (2026). *Room Persistence Library*
* Android Developers. (2026). *Guide to App Architecture*
* Material Design Guidelines (m3.material.io)
* MPAndroidChart Documentation
* YNAB Budgeting Methodology

---

## 👩‍💻 Author

**Chantel Leboho**
Software Development Student

---

## 📌 Notes

* This application uses offline storage (Room Database)
* No internet connection is required
* Designed for educational purposes

---

## ⭐ Conclusion

Ubuntu Budget provides a simple yet powerful way for users to manage their finances. By combining modern Android development tools with user-friendly design, the app promotes financial awareness and responsible spending habits.

---
