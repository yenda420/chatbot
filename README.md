# AI Test Generator

The "AI Test Generator" is a robust desktop application developed in Java using the JavaFX framework for its user interface. This project served as my long-term maturita (final high school examination) work, emphasizing concepts in AI integration, prompt engineering, and sophisticated output validation.

## Overview

This application is designed for high school teachers, providing a powerful tool to effortlessly generate academic tests. It streamlines the test creation process by leveraging Artificial Intelligence, handling all the complex prompt engineering internally. The core innovation lies in its comprehensive validation algorithm, which meticulously scrutinizes the AI's output for accuracy, completeness, and adherence to specified criteria, ensuring high-quality, reliable test materials.

## Key Features

* **AI-Powered Test Generation:**
    * Generates tests based on simple input parameters from the teacher.
    * Supports a wide range of OpenAI models (configured via `.env`), allowing for future extensibility.
    * **Automated Prompt Engineering:** Teachers do not need to craft complex prompts; the application manages all interactions with the AI.

* **Robust AI Output Validation:**
    * Implements a sophisticated, multi-stage validation algorithm to ensure the quality of generated tests.
    * **Checks for:**
        * Complete test structure (e.g., presence of all required sections like "Questions:", "Correct Answers:").
        * Fulfillment of all user-defined criteria (e.g., number of questions, question types, difficulty).
        * Clarity and conciseness of generated content, eliminating placeholders or incomplete sentences.
        * **Factual Accuracy:** The application leverages the AI itself to perform a fact-check on the generated content, identifying and flagging factual errors or misinformation within the answers and explanations.
    * **Self-Correction Mechanism:** If issues are detected, the application automatically provides corrective feedback to the AI and requests a revised output, repeating this process up to a defined maximum number of attempts (`MAX_ATTEMPTS`).

* **Flexible Question Types:**
    * Teachers can choose to generate tests with:
        * Yes/No questions
        * Multiple-choice questions
        * Open-ended questions

* **Customizable Test Parameters:**
    * Teachers can specify:
        * Test Name
        * Number of Questions
        * Difficulty Level
        * Question Type
        * Optional Custom Prompt
        * Optional Text File (for source material)
        * Subject
        * Topic Sections

* **Database Management (MySQL):**
    * Parses and stores the generated test data into a MySQL database.
    * Enables **test retrieval and download** for other users.
    * Includes comprehensive **management functionalities** for database objects, such as users, their roles, subjects, topics, and tests.

* **User Role Management:**
    * Supports distinct user roles: **Administrator** and **Teacher**, each with specific permissions and access levels.

* **Test Export:**
    * Exports the generated tests into a `*.docx` document format for easy printing and distribution.

## Technology Stack

* **Language:** Java
* **UI Framework:** JavaFX (`.fxml` for views)
* **Database:** MySQL
* **AI Integration:** OpenAI API (configurable for various models)

## Setup and Running the Application

To run this application, you will need Java Development Kit (JDK) and a MySQL server instance.

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/yenda420/chatbot.git
    cd chatbot
    ```
2.  **Database Setup:**
    * Ensure a MySQL server is running.
    * Create a database named `chatbot` (or as specified in your `.env` file).
3.  **Environment Configuration:**
    * Rename the `.env-template` file to `.env` in the project root.
    * Edit the `.env` file with your database credentials and OpenAI API key:

    ```ini
    # Database configuration
    DB_HOST=localhost
    DB_URL=jdbc:mysql://127.0.0.1:3306/
    DB_USER=root
    DB_PASSWORD=
    DB_NAME=chatbot

    # Application configuration
    ADMIN_EMAIL=admin
    ADMIN_PASSWORD=admin
    MAX_QUESTIONS=25

    # OpenAI configuration
    OPENAI_MODEL=gpt-3.5-turbo # Can be changed to other OpenAI models (e.g., gpt-4)
    OPENAI_API_KEY=<your_openai_api_key_here>

    OPENAI_API_URL=https://api.openai.com/v1/chat/completions 
    OPENAI_TEMPERATURE=0.7
    ```
    * Replace `your_openai_api_key_here` with your actual OpenAI API key.

4.  **Run with Maven:**
    * Build the project:
        ```bash
        mvn clean install
        ```
    * Run the application:
        ```bash
        mvn javafx:run
        ```
    * Alternatively, open the project in your IDE (e.g., IntelliJ IDEA) and run it directly.

## Project Documentation & Manual

As this project was developed as a Maturita work, comprehensive documentation and an application manual are available within the `project-documentation` directory. This includes detailed explanations of the algorithms, design choices, and user instructions.

## Future Enhancements & Extensibility

While the UI is kept simple and intuitive, the core strength of this application lies in its sophisticated AI integration, robust prompt engineering, and advanced validation capabilities. The architecture is designed for significant future enhancements, including:

* **Student Interface:** Developing a dedicated module for students to take tests directly within the application.
* **Automated Grading:** Implementing features for automatic correction and evaluation of student responses.
* **Performance Analysis:** Generating detailed statistics and analytical reports on student performance.
* **Additional AI Model Support:** Integrating with other AI models beyond OpenAI.
