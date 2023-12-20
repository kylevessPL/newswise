from matplotlib import pyplot as plt

data = """
TP Rate  FP Rate  Precision  Recall   F-Measure  MCC      ROC Area  PRC Area  Class
                 0.886    0.011    0.913      0.886    0.899      0.887    0.956     0.860     Business&Money
                 0.884    0.018    0.860      0.884    0.872      0.855    0.963     0.829     Crime&Legal
                 0.787    0.012    0.895      0.787    0.838      0.821    0.969     0.854     Entertainment&Arts
                 0.756    0.025    0.791      0.756    0.773      0.745    0.936     0.727     Lifestyle
                 0.852    0.006    0.948      0.852    0.898      0.887    0.950     0.876     SciTech&Education
                 0.824    0.010    0.911      0.824    0.866      0.851    0.950     0.845     Society&Religion
                 0.807    0.019    0.842      0.807    0.824      0.803    0.956     0.792     Sports&Health
                 0.917    0.007    0.943      0.917    0.930      0.921    0.984     0.925     Travel&Food
                 0.977    0.057    0.683      0.977    0.804      0.791    0.970     0.690     World&Politics
"""

# Extracting labels and F1 scores
lines = data.split('\n')

# Filter out empty lines
lines = [line for line in lines if line.strip()]

# Extract headers and data separately
headers = lines[0].split()
data_lines = lines[1:]

# Assuming the data format is correct, extract labels and F1 scores
labels = [line.split()[-1] for line in data_lines]
f1_scores = [float(line.split()[-5]) for line in data_lines]  # F-Measure is at the 5th from the last position

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
