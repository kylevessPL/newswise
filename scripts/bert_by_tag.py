import re

from matplotlib import pyplot as plt

data = """
                    precision    recall  f1-score   support

    World&Politics       0.88      0.91      0.90     43988
Entertainment&Arts       0.89      0.87      0.88     43988
         Lifestyle       0.87      0.85      0.86     43988
     Sports&Health       0.88      0.88      0.88     43988
  Society&Religion       0.94      0.93      0.93     43988
       Travel&Food       0.94      0.94      0.94     43988
    Business&Money       0.92      0.93      0.93     43988
 SciTech&Education       0.90      0.90      0.90     43988
       Crime&Legal       0.94      0.94      0.94     43988
"""

# Extracting labels and F1 scores from sklearn classification report
lines = data.split('\n')
lines = [line for line in lines if line.strip()]

labels = []
f1_scores = []

for line in lines[2:]:  # Skip the header line
    parts = re.split(r'\s+', line.strip())
    labels.append(parts[0])
    f1_scores.append(float(parts[-2]))

# Plotting the bar chart with F1 score values
plt.figure(figsize=(10, 6))
bars = plt.barh(labels, f1_scores, color='skyblue')
plt.xlabel('Dokładność F1')
plt.title('Dokładność F1 dla każdej kategorii')
plt.xlim(0, 1)

# Add F1 score values next to each bar
for bar, value in zip(bars, f1_scores):
    plt.text(bar.get_width() + 0.01, bar.get_y() + bar.get_height() / 2, f'{value:.2f}', ha='left', va='center')

# Adjust left padding
plt.subplots_adjust(left=0.2)

plt.show()
