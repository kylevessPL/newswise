import re

import numpy as np
from matplotlib import pyplot as plt
from sklearn.metrics import ConfusionMatrixDisplay

data = """
  a       b       c       d       e       f       g       h       i | Accuracy | <-- classified as
<4632>     66      90     318     271      33     545     166     794 |   -100%  |     a = Business&Money
 73   <6693>    289     911      67     128     358     114     658 |   -100%  |     b = Crime&Legal
120     347  <20536>   1687     156     496     390     363    1693 |   -100%  |     c = Entertainment&Arts
419     275    1353  <23570>    238     409    1642     626     744 |   -100%  |     d = Lifestyle
572      81     265     620   <5858>    163     735     261     884 |   -100%  |     e = SciTech&Education
 65     306    1446     750     144   <8311>    396     121    1376 |   -100%  |     f = Society&Religion
196     215     650    1430     184     344  <23504>    543     830 |   -100%  |     g = Sports&Health
262      62     335     639     200      69     446  <15442>    270 |   -100%  |     h = Travel&Food
635     520     683     275    1169    1136     445     179  <38946>|   -100%  |     i = World&Politics
"""

# Parsed data
data_lines = data.strip().split('\n')[1:]
conf_matrix = []
labels = []

for line in data_lines:
    # Extract numeric values using regular expression
    values = [int(val) for val in re.findall(r'\b\d+\b', line)[:-1]]
    conf_matrix.append(values)

    # Extract labels from the last column
    label = line.split('=')[1].strip()
    labels.append(label)

# Convert to numpy array
conf_matrix = np.array(conf_matrix).T

# Display confusion matrix plots for each category vs. other category
for i, label in enumerate(labels):
    # Create binary confusion matrix for the selected category vs. other categories
    binary_conf_matrix = np.zeros((2, 2))
    binary_conf_matrix[0, 0] = conf_matrix[i, i]  # True positives for the selected category
    binary_conf_matrix[0, 1] = np.sum(conf_matrix[:, i]) - conf_matrix[i, i]  # False positives
    binary_conf_matrix[1, 0] = np.sum(conf_matrix[i, :]) - conf_matrix[i, i]  # False negatives
    binary_conf_matrix[1, 1] = np.sum(conf_matrix) - np.sum(binary_conf_matrix)  # True negatives

    # Display the confusion matrix
    fig, ax = plt.subplots(figsize=(8.5, 7))
    disp = ConfusionMatrixDisplay(confusion_matrix=binary_conf_matrix, display_labels=[f'{label}', 'Inna'])
    disp.plot(cmap='viridis', values_format='.0f', ax=ax)

    # Customize plot
    ax.set_xlabel('Przewidywana kategoria')
    ax.set_ylabel('Rzeczywista kategoria')
    plt.subplots_adjust(right=1.02)
    plt.title(f'Macierz pomyłek: {label}')
    plt.show()
