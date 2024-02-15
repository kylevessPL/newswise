import re

from matplotlib import pyplot as plt

data = """
Correctly Classified Instances      339969               85.8742 %
Correctly Classified Instances      338317               85.4569 %
Correctly Classified Instances      334780               84.5635 %
"""

# Extract accuracy values using regular expression
accuracy_values = [float(match.group(2)) for match in
                   re.finditer(r'Correctly Classified Instances\s+(\d+)\s+([\d.]+) %', data)]

# Visualize on a vertical bar chart
models = [f'Model {i + 1}' for i in range(len(accuracy_values))]
plt.bar(models, accuracy_values, color='blue')
plt.ylim(0, 100)
plt.xlabel('Model')
plt.ylabel('Dokładność (%)')
plt.title('Dokładność poszczególnych modeli (Weka)')

# Add percentage values to the top of each bar
for i, value in enumerate(accuracy_values):
    plt.text(i, value + 1, f'{value:.2f}%', ha='center', va='bottom', color='black')

plt.show()
