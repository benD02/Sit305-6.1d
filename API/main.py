import socket
import argparse
from flask import Flask, request, jsonify
from datetime import datetime
import os
from gradientai import Gradient

token = 'LXkZBp5a1nGCP16xM7QieYKNz2Ns0tOW'
workspace_id = 'b5f958d9-ffd4-41bb-a492-704114e02c8e_workspace'

os.environ['GRADIENT_ACCESS_TOKEN'] = token
os.environ['GRADIENT_WORKSPACE_ID'] = workspace_id

app = Flask(__name__)

# Global variable for new_model_adapter
new_model_adapter = None

import re
import json

def process_quiz(quiz_text):
    questions = []
    pattern = re.compile(r'QUESTION: (.+?)\n(?:OPTION A: (.+?)\n)+(?:OPTION B: (.+?)\n)+(?:OPTION C: (.+?)\n)+(?:OPTION D: (.+?)\n)+ANS: (.+?)', re.DOTALL)
    matches = pattern.findall(quiz_text)

    for match in matches:
        question = match[0].strip()
        options = match[1].strip(), match[2].strip(), match[3].strip(), match[4].strip()
        correct_ans = match[-1].strip()
        
        question_data = {
            "question": question,
            "options": options,
            "correct_answer": correct_ans
        }
        questions.append(question_data)
    
    return  questions

def fetchQuizFromLlama(student_topic, new_model_adapter):
    query = (
        f"[INST] Generate a quiz with 3 questions to test students on the provided topic. "
        f"For each question, generate 4 options where only one of the options is correct. "
        f"Format your response as follows:\n"
        f"QUESTION: [Your question here]?\n"
        f"OPTION A: [First option]\n"
        f"OPTION B: [Second option]\n"
        f"OPTION C: [Third option]\n"
        f"OPTION D: [Fourth option]\n"
        f"ANS: [Correct answer letter]\n\n"
        f"Ensure text is properly formatted. It needs to start with a question, then the options, and finally the correct answer."
        f"Follow this pattern for all questions."
        f"Here is the student topic:\n{student_topic}"
        f"[/INST]"
    )
    response = new_model_adapter.complete(query=query, max_generated_token_count=500).generated_output
    return response


@app.route('/')
def index():
    return "Welcome to the Flask API!"



@app.route('/getQuiz', methods=['GET'])
def get_quiz():
    global new_model_adapter
    if new_model_adapter is None:
        return jsonify({'error': 'Model adapter not initialized'}), 500
    student_topic = request.args.get('topic')
    if student_topic is None:
        return jsonify({'error': 'Missing topic parameter'}), 400
    quiz = fetchQuizFromLlama(student_topic, new_model_adapter)
    return jsonify({'quiz': process_quiz(quiz)}), 200

def prepareLlamaBot(name):
    gradient = Gradient()
    base_model = gradient.get_base_model(base_model_slug="llama2-7b-chat")
    global new_model_adapter
    new_model_adapter = base_model.create_model_adapter(name=name)



if __name__ == '__main__':
    default_name = f"Llama_{datetime.now().strftime('%Y%m%d_%H%M%S')}_{socket.gethostname()}"
    
    parser = argparse.ArgumentParser()
    parser.add_argument('--name', default=default_name, help='Specify a name')
    args = parser.parse_args()

    port_num = 5000
    print(f"Starting Llama bot with name {args.name}...\n This may take a while.")
    prepareLlamaBot(args.name)
    print(f"App running on port {port_num}")
    app.run(port=port_num)
