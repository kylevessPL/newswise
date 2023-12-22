import matplotlib.pyplot as plt
from collections import defaultdict
from io import StringIO

# Read results from a text file
file_path = 'result.csv'  # Replace with the actual path to your file
with open(file_path, 'r') as file:
    file.readline()
    data = file.read()

# Convert the string to a list of tuples
results_list = [line.strip().split(',') for line in StringIO(data).readlines() if line.strip()]

# Calculate accuracy for each class
class_counts = defaultdict(int)
correct_counts = defaultdict(int)

for pred, true in results_list:
    class_counts[true] += 1
    if pred == true:
        correct_counts[true] += 1

# Calculate accuracy for each class
class_accuracies = {true: (correct_counts[true] / class_counts[true]) * 100 for true in class_counts}

# Create a bar chart
fig, ax = plt.subplots(figsize=(9, 7))
classes = list(class_accuracies.keys())
accuracies = list(class_accuracies.values())
ax.bar(classes, accuracies, color='green')

# Set axis labels and title
ax.set_ylabel('Dokładność (%)')
ax.set_title('Dokładność na zbiorze testowym')

# Display the percentage values on top of the bars
for i, (label, accuracy) in enumerate(class_accuracies.items()):
    ax.text(i, accuracy + 2, f'{accuracy:.2f}%', ha='center', va='bottom')

# Rotate x-axis labels for better readability
plt.xticks(rotation=90)
plt.subplots_adjust(top=0.9, bottom=0.22)

# Show the bar chart
plt.show()
