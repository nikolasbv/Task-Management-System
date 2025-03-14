# Task Management System

## Overview

This is a Task Management System developed as part of a university project for the "Technology of Multimedia" course at NTUA. The application allows users to create, manage, and monitor tasks with deadlines, priorities, categories, and reminders.

## Features

- **Task Management**: Add, edit, delete, and search for tasks.
- **Category Management**: Define and organize tasks into categories.
- **Priority Levels**: Assign different priority levels to tasks.
- **Reminders**: Set reminders for tasks with predefined or custom dates.
- **Persistence**: Stores tasks and configurations using JSON.
- **Graphical User Interface (GUI)**: Implemented using JavaFX for an interactive experience.
- **Data Generation**: Includes a script to generate dummy data for testing.

## Technologies Used

- **Java 20**
- **JavaFX**
- **Maven** for dependency management
- **JSON** for data storage
- **Python** (for data generation)

## Installation and Usage

### Prerequisites

- Java Development Kit (JDK 20 or higher)
- Maven
- Python (for data generation script)

### Clone Repository

```bash
git clone https://github.com/nikolasbv/Task-Management-System.git
cd Task-Management-System
```

### Build and Run

```bash
mvn clean install
mvn javafx:run
```

### Generate Dummy Data (Optional)

To generate sample JSON data for testing:

```bash
cd data_generation
python data_generator.py
```

## Project Structure

```
Task-Management-System/
│-- src/main/java/org/example/
│   ├── Main.java (Entry point)
│   ├── controller/ (Logic and data handling)
│   ├── model/ (Task, Category, Priority, Reminder models)
│   ├── view/ (JavaFX UI components)
│-- medialab/ (JSON storage directory)
│-- data_generation/
│   ├── data_generator.py (Generates dummy data for JSON files)
│-- pom.xml (Maven configuration)
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
