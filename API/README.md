# Teaching 305 708 Flask API

This Flask API leverages the GradientAI service with the Llama model to generate educational quizzes on various topics.

## Prerequisites

- Python 3.x installed on your system.

## Installation

1. Clone the repository.
2. Navigate to the directory.
3. Setup a virtual environment and install dependencies:

### MacOS and Linux

    ```bash
    python3 -m venv myenv
    source myenv/bin/activate
    pip install -r pip_install_commands.txt
    ```

### Windows

    ```bash
    python3 -m venv myenv
    myenv\Scripts\activate
    pip install -r pip_install_commands.txt
    ```

## Running the API

Execute the `main.py` script:

```bash
python main.py
```

The server will start on the default port 5000.

## API Endpoints

The API has two primary routes:

### Home Route: /

This route returns a welcome message and confirms that the API is operational.

Example Request:

```
curl http://localhost:5000/
```

Example Response:

```
Welcome to the Flask API!
```

### Get Quiz Route: /getQuiz

This route generates a quiz on a specified topic when provided with the topic as a query parameter. The API aims to return a set of three questions, each accompanied by multiple-choice options and the correct answer. Since Llama is a conversational AI model, there may be occasional discrepancies in its responses. If you encounter an empty quiz, please attempt to run the API again.

Example Request:

```
curl http://localhost:5000/getQuiz?topic=Android%20Development
```

Example Response:

```
{
    "quiz": [
        {
            "correct_answer": "C",
            "options": [
                "Android apps are designed for entertainment purposes only, while Android games are designed for both entertainment and productivity.",
                "Android apps are developed using Java, while Android games are developed using C++.",
                "Android apps are installed on the device's internal storage, while Android games are installed on the device's external storage.",
                "Android apps are designed for use on a single device, while Android games are designed for use on multiple devices."
            ],
            "question": "What is the main difference between a Android app and a Android game?"
        },
        {
            "correct_answer": "A",
            "options": [
                "A toolkit for developing Android apps that includes a set of pre-written code and tools for debugging and testing.",
                "A platform for developing Android games that includes a set of pre-written code and tools for debugging and testing.",
                "A tool for creating Android apps that are optimized for use on a single device.",
                "A tool for creating Android games that are optimized for use on a single device."
            ],
            "question": "What is the Android Software Development Kit (SDK)?"
        },
        {
            "correct_answer": "B",
            "options": [
                "An activity is a user interface that represents a single task or function, while a service is a background process that performs a specific task.",
                "An activity is a background process that performs a specific task, while a service is a user interface that represents a single task or function.",
                "An activity is a single instance of an app that runs on a single device, while a service is a single instance of an app that runs on multiple devices.",
                "An activity is a single instance of an app that runs on multiple devices, while a service is a single instance of an app that runs on a single device."
            ],
            "question": "What is the difference between a Android activity and a Android service?"
        }
    ]
}
```

Replace "Android Development" with your desired topic to generate questions related to that field. The response will be in JSON format, containing an array of questions, each with multiple-choice options and the correct answer.

```

```
