# Airline Management and Reservation System

This project is a comprehensive Java application designed to manage airline flight operations, passenger information, and reservation processes. It focuses on simulating a real-world airline system with a robust data management structure.

## Core Features

- **Flight Management:** Create, list, and manage flight schedules and details.
- **Reservation System:** Complete ticketing process with seat selection for passengers.
- **Multi-Class Seating:** Different pricing and features for Business and Economy classes.
- **User Management:** Handling both Staff and Passenger profiles with specific authorizations.
- **Reporting:** Generation of flight status and reservation reports.
- **Persistent Data Storage:** Uses a file-based repository system to ensure data is saved and reloaded between sessions.

## Technical Overview

- **Language:** Java
- **Build Tool:** Maven (for dependency and project management)
- **Architecture:** Built using a layered approach (Model, Service, and Repository layers) to keep the code organized and maintainable.
- **Testing:** Core logic and pricing calculations are verified using JUnit 5.
- **Multi-threading:** Background processes for report generation and system simulations.

## Documentation

To understand the system's class structure and relationships, you can review the architectural diagram:
- [Architecture Diagram (UML)](uml.pdf)
