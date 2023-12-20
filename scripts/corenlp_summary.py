import re

from matplotlib import pyplot as plt

data = """
INFO Average accuracy/micro-averaged F1: 0.75921
INFO Average accuracy/micro-averaged F1: 0.70879
INFO Average accuracy/micro-averaged F1: 0.70863
INFO Average accuracy/micro-averaged F1: 0.76103
INFO Average accuracy/micro-averaged F1: 0.76135
INFO Average accuracy/micro-averaged F1: 0.76078
INFO Average accuracy/micro-averaged F1: 0.76105
INFO Average accuracy/micro-averaged F1: 0.76081
INFO Average accuracy/micro-averaged F1: 0.75410
INFO Average accuracy/micro-averaged F1: 0.75421
INFO Average accuracy/micro-averaged F1: 0.78459
INFO Average accuracy/micro-averaged F1: 0.76557
INFO Average accuracy/micro-averaged F1: 0.80180
INFO Average accuracy/micro-averaged F1: 0.79731
INFO Average accuracy/micro-averaged F1: 0.80816
INFO Average accuracy/micro-averaged F1: 0.81666
INFO Average accuracy/micro-averaged F1: 0.81768
INFO Average accuracy/micro-averaged F1: 0.80940
INFO Average accuracy/micro-averaged F1: 0.77763
INFO Average accuracy/micro-averaged F1: 0.84025
INFO Average accuracy/micro-averaged F1: 0.86428
INFO Average accuracy/micro-averaged F1: 0.88285
INFO Average accuracy/micro-averaged F1: 0.88477
"""

# Extract F1 scores using regular expression
accuracy_values = [float(match.group(1)) * 100 for match in re.finditer(r'INFO Average accuracy/micro-averaged F1: ('
                                                                        r'\d+\.\d+)', data)]

# Visualize on a vertical bar chart
models = [f'Model {i + 1}' for i in range(len(accuracy_values))]
plt.figure(figsize=(9, 7))
plt.bar(models, accuracy_values, color='blue')
plt.ylim(0, 100)
plt.xlabel('Model')
plt.ylabel('Dokładność (%)')
plt.title('Dokładność poszczególnych modeli (CoreNLP)')

# Add F1 scores to the top of each bar with vertical text
for i, score in enumerate(accuracy_values):
    plt.text(i, score + 2, f'{score:.2f}%', ha='center', va='bottom', color='black', rotation='vertical')

plt.xticks(rotation=90)
plt.subplots_adjust(top=0.96)
plt.show()
