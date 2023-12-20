from matplotlib import pyplot as plt

data = """
-----------------------------------------------------------------------------------
|                Tag | Errors |  Count |   % Err | Precision | Recall | F-Measure |
-----------------------------------------------------------------------------------
|          Lifestyle |  11732 |  43988 | 0.267   | 0.746     | 0.733  | 0.739     |
|      Sports&Health |  10359 |  43988 | 0.235   | 0.84      | 0.765  | 0.801     |
| Entertainment&Arts |   9716 |  43988 | 0.221   | 0.866     | 0.779  | 0.82      |
|   Society&Religion |   8829 |  43988 | 0.201   | 0.894     | 0.799  | 0.844     |
|  SciTech&Education |   7297 |  43988 | 0.166   | 0.932     | 0.834  | 0.88      |
|        Crime&Legal |   5895 |  43988 | 0.134   | 0.823     | 0.866  | 0.844     |
|     Business&Money |   5516 |  43988 | 0.125   | 0.901     | 0.875  | 0.888     |
|        Travel&Food |   4273 |  43988 | 0.097   | 0.929     | 0.903  | 0.916     |
|     World&Politics |   1175 |  43988 | 0.027   | 0.683     | 0.973  | 0.803     |
-----------------------------------------------------------------------------------
"""

# Extracting labels and F1 scores
lines = data.split('\n')

# Filter out empty lines
lines = [line for line in lines if line.strip()]

# Assuming the data format is correct, extract labels and F1 scores
labels = [line.split('|')[1].strip() for line in lines[3:-2]]
f1_scores = [float(line.split('|')[-2].strip()) for line in lines[3:-2]]

# Plotting the bar chart with F1 score values and added left padding
plt.figure(figsize=(10, 6))
bars = plt.barh(labels, f1_scores, color='skyblue')
plt.xlabel('Dokładność F1')
plt.title('Dokładność F1 dla każdej kategorii')
plt.xlim(0, 1)  # Set x-axis limit to show F1 scores between 0 and 1

# Add F1 score values next to each bar
for bar, value in zip(bars, f1_scores):
    plt.text(bar.get_width() + 0.01, bar.get_y() + bar.get_height() / 2, f'{value:.3f}', ha='left', va='center')

# Adjust left padding
plt.subplots_adjust(left=0.2)

plt.show()
