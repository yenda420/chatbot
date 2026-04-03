# AI Test Generator

This is my high school final graduation (Maturita) project. It's a desktop application designed to help teachers effortlessly generate academic tests. The app abstracts away complex prompt engineering, allowing users to just input simple parameters while the system handles the heavy lifting with the OpenAI API. 

**Tech Stack:** Java, JavaFX, MySQL, and the OpenAI API.

## Key Features

* **Automated AI Generation:** Create tests with Yes/No, multiple-choice, or open-ended questions based on specific subjects, difficulty levels, or even custom source text files.
* **Self-Correcting Validation:** The standout feature is a multi-stage validation algorithm that checks the AI's output for structure, completeness, and factual accuracy. If it spots a mistake, it automatically forces the AI to revise and correct itself.
* **Database & Role Management:** Uses MySQL to safely store generated tests and handle access permissions for different user roles (Admin vs. Teacher).
* **Document Export:** Instantly exports the generated tests into formatted `.docx` files, ready for printing and classroom use.
