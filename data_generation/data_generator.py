import json
import random
import string
from datetime import datetime, timedelta

NUM_CATEGORIES = 10
NUM_PRIORITIES = 10
NUM_TASKS = 50
DELAYED_TASKS = 3
SHORT_TERM_TASKS = 5
CURRENT_DATE = datetime.now()
FUTURE_DEADLINE_LIMIT = datetime(2026, 12, 31)

REMINDER_TYPES = ["ONE_DAY_BEFORE", "ONE_WEEK_BEFORE", "ONE_MONTH_BEFORE", "CUSTOM_DATE"]
TASK_STATUSES = ["OPEN", "IN_PROGRESS", "POSTPONED", "COMPLETED", "DELAYED"]

def generate_task_id():
    return ''.join(random.choices(string.ascii_lowercase + string.digits, k=8))

def generate_task_title():
    words = ["Project", "Meeting", "Update", "Plan", "Design", "Report", "Test", "Code", "Fix", "Develop"]
    return " ".join(random.choices(words, k=random.randint(1, 4)))

def generate_task_description():
    sentences = [
        "This task is critical for the project.",
        "Ensure that all requirements are met.",
        "Update the documentation accordingly.",
        "Coordinate with the team for progress.",
        "This needs to be reviewed before submission.",
        "Schedule a meeting to discuss updates.",
        "Fix all reported bugs before release.",
        "Prepare a report on the latest changes.",
        "Check compatibility with existing modules.",
        "Test thoroughly to avoid future issues."
    ]
    return " ".join(random.sample(sentences, random.randint(2, 5)))

categories = [
    {"name": "Work"}, {"name": "Personal"}, {"name": "Health"}, {"name": "Finance"},
    {"name": "Education"}, {"name": "Shopping"}, {"name": "Travel"}, {"name": "Home"},
    {"name": "Hobbies"}, {"name": "Projects"}
]

priorities = [
    {"name": "Default"}, {"name": "Low"}, {"name": "Medium"}, {"name": "High"},
    {"name": "Urgent"}, {"name": "Critical"}, {"name": "Optional"}, {"name": "Long-Term"},
    {"name": "Short-Term"}, {"name": "Backlog"}
]

category_list = [category["name"] for category in categories] * 5
priority_list = [priority["name"] for priority in priorities] * 5

random.shuffle(category_list)
random.shuffle(priority_list)

tasks = []

for i in range(NUM_TASKS):
    task_id = generate_task_id()
    title = generate_task_title()
    description = generate_task_description()
    category = category_list.pop()
    priority = priority_list.pop()

    if i < DELAYED_TASKS:
        deadline = CURRENT_DATE - timedelta(days=random.randint(1, 10))
        status = "DELAYED"
    elif i < DELAYED_TASKS + SHORT_TERM_TASKS:
        deadline = CURRENT_DATE + timedelta(days=random.randint(0, 7))
        status = random.choice(TASK_STATUSES[:-1])
    else:
        deadline = CURRENT_DATE + timedelta(days=random.randint(1, (FUTURE_DEADLINE_LIMIT - CURRENT_DATE).days))
        status = random.choice(TASK_STATUSES[:-1])

    reminders = []
    if status != "COMPLETED":
        for reminder_type in REMINDER_TYPES:
            if random.random() > 0.5:
                if reminder_type == "ONE_DAY_BEFORE" and deadline > CURRENT_DATE + timedelta(days=1):
                    reminders.append({"type": reminder_type, "date": (deadline - timedelta(days=1)).strftime("%Y-%m-%d")})
                elif reminder_type == "ONE_WEEK_BEFORE" and deadline > CURRENT_DATE + timedelta(weeks=1):
                    reminders.append({"type": reminder_type, "date": (deadline - timedelta(weeks=1)).strftime("%Y-%m-%d")})
                elif reminder_type == "ONE_MONTH_BEFORE" and deadline > CURRENT_DATE + timedelta(days=30):
                    reminders.append({"type": reminder_type, "date": (deadline - timedelta(days=30)).strftime("%Y-%m-%d")})
                elif reminder_type == "CUSTOM_DATE" and deadline > CURRENT_DATE:
                    custom_date = CURRENT_DATE + timedelta(days=random.randint(0, max(1, (deadline - CURRENT_DATE).days)))
                    reminders.append({"type": reminder_type, "date": custom_date.strftime("%Y-%m-%d")})

    tasks.append({
        "id": task_id,
        "title": title,
        "description": description,
        "category": category,
        "priority": priority,
        "deadline": deadline.strftime("%Y-%m-%d"),
        "status": status,
        "reminders": reminders
    })

def save_json(filename, data):
    with open(filename, "w") as f:
        json.dump(data, f, indent=4)

save_json("./data_generation/categories.json", {"categories": categories})
save_json("./data_generation/priorities.json", {"priorities": priorities})
save_json("./data_generation/tasks.json", {"tasks": tasks})

save_json("./medialab/categories.json", {"categories": categories})
save_json("./medialab/priorities.json", {"priorities": priorities})
save_json("./medialab/tasks.json", {"tasks": tasks})
