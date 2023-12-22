import numpy as np
import re
from matplotlib import pyplot as plt
from sklearn.metrics import ConfusionMatrixDisplay

data = """
 a     b     c     d     e     f     g     h     i   <-- classified as
38988    10    68   672    34    23   999   105  3089 |     a = Business&Money
48 38882   301  1961    19    12   743    91  1931 |     b = Crime&Legal
414  1187 34634  1800   414  1630   511   397  3001 |     c = Entertainment&Arts
951  2913  1112 33237   561   967  1862   786  1599 |     d = Lifestyle
343    84   212   896 37492    38  1029   200  3694 |     e = SciTech&Education
60   145  1391  1150    74 36255   567   145  4201 |     f = Society&Religion
1393  1822   427  1078   725   737 35514   660  1632 |     g = Sports&Health
498   179   228  1013   174    75   668 40333   820 |     h = Travel&Food
12    13   314   236    57    45   273    56 42982 |     i = World&Politics
"""

# Parsed data
data_lines = data.strip().split('\n')[1:]
conf_matrix = []
labels = []

for line in data_lines:
    # Extract numeric values using regular expression
    values = [int(val) for val in re.findall(r'\b\d+\b', line)]
    conf_matrix.append(values)

    # Extract labels from the last column
    label = line.split('=')[1].strip()
    labels.append(label)

# Convert to numpy array
conf_matrix = np.array(conf_matrix).T

# Display confusion matrix plots for each category vs. other category
for i in range(len(labels)):
    # Create binary confusion matrix for the selected category vs. other categories
    binary_conf_matrix = np.zeros((2, 2))
    binary_conf_matrix[0, 0] = conf_matrix[i, i]  # True positives for the selected category
    binary_conf_matrix[0, 1] = np.sum(conf_matrix[:, i]) - conf_matrix[i, i]  # False positives
    binary_conf_matrix[1, 0] = np.sum(conf_matrix[i, :]) - conf_matrix[i, i]  # False negatives
    binary_conf_matrix[1, 1] = np.sum(conf_matrix) - np.sum(binary_conf_matrix)  # True negatives

    # Display the confusion matrix
    fig, ax = plt.subplots(figsize=(8.5, 7))
    disp = ConfusionMatrixDisplay(confusion_matrix=binary_conf_matrix, display_labels=[f'{labels[i]}', 'Inna'])
    disp.plot(cmap='viridis', values_format='.0f', ax=ax)

    # Customize plot
    ax.set_xlabel('Przewidywana kategoria')
    ax.set_ylabel('Rzeczywista kategoria')
    plt.subplots_adjust(right=1.02)
    plt.title(f'Macierz pomyłek: {labels[i]}')
    plt.show()
