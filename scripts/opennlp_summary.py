import re

from matplotlib import pyplot as plt

data = """
=== Evaluation summary ===
  Number of sentences: 183233
    Min sentence size:      8
    Max sentence size:   7439
Average sentence size: 309.12
           Tags count:      9
             Accuracy: 80.49%
=== Evaluation summary ===
  Number of sentences: 395892
    Min sentence size:      8
    Max sentence size:   8310
Average sentence size: 329.69
           Tags count:      9
             Accuracy: 83.48%
Training started (OpenNLP).
=== Evaluation summary ===
  Number of sentences: 395892
    Min sentence size:      8
    Max sentence size:   8310
Average sentence size: 329.69
           Tags count:      9
             Accuracy: 80.82%
=== Evaluation summary ===
  Number of sentences: 395892
    Min sentence size:      8
    Max sentence size:   8310
Average sentence size: 329.69
           Tags count:      9
             Accuracy: 83.63%
=== Evaluation summary ===
  Number of sentences: 395892
    Min sentence size:      8
    Max sentence size:   8310
Average sentence size: 329.69
           Tags count:      9
             Accuracy: 80.76%
=== Evaluation summary ===
  Number of sentences: 395892
    Min sentence size:      8
    Max sentence size:   8310
Average sentence size: 329.69
           Tags count:      9
             Accuracy: 76.23%
"""

# Extract accuracy values using regular expression
accuracy_values = [float(match.group(1)) for match in re.finditer(r'Accuracy: (\d+\.\d+)%', data)]

# Visualize on a vertical bar chart
models = [f'Model {i + 1}' for i in range(len(accuracy_values))]
plt.bar(models, accuracy_values, color='blue')
plt.ylim(0, 100)
plt.xlabel('Model')
plt.ylabel('Dokładność (%)')
plt.title('Dokładność poszczególnych modeli (OpenNLP)')
for i, value in enumerate(accuracy_values):
    plt.text(i, value + 1, f'{value:.2f}%', ha='center', va='bottom', color='black')
plt.show()
