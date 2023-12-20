import re

from matplotlib import pyplot as plt

data = """
accuracy                           0.91    395892
accuracy                           0.91    395892
accuracy                           0.92    395892
accuracy                           0.91    395892
accuracy                           0.90    397339
accuracy                           0.89    395892
accuracy                           0.90    395892
"""

# Extract accuracy values using regular expression
accuracy_values = [float(match.group(1)) * 100 for match in re.finditer(r'accuracy\s+(\d+\.\d+)\s+\d+', data)]

# Visualize on a vertical bar chart
models = [f'Model {i + 1}' for i in range(len(accuracy_values))]
plt.bar(models, accuracy_values, color='blue')
plt.ylim(0, 100)
plt.xlabel('Model')
plt.ylabel('Dokładność (%)')
plt.title('Dokładność poszczególnych modeli (BERT)')

# Add accuracy values to the top of each bar
for i, value in enumerate(accuracy_values):
    plt.text(i, value + 1, f'{value:.0f}%', ha='center', va='bottom', color='black')

plt.show()
