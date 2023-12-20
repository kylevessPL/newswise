from matplotlib import pyplot as plt

data = """
-----------------------------------------------------------------------------------
|                Tag | Errors |  Count |   % Err | Precision | Recall | F-Measure |
-----------------------------------------------------------------------------------
|          Lifestyle |   5706 |  29276 | 0.195   | 0.78      | 0.805  | 0.793     |
| Entertainment&Arts |   5252 |  25788 | 0.204   | 0.801     | 0.796  | 0.799     |
|     World&Politics |   5042 |  43988 | 0.115   | 0.843     | 0.885  | 0.864     |
|   Society&Religion |   4604 |  12915 | 0.356   | 0.749     | 0.644  | 0.692     |
|      Sports&Health |   4392 |  27896 | 0.157   | 0.826     | 0.843  | 0.834     |
|  SciTech&Education |   3581 |   9439 | 0.379   | 0.707     | 0.621  | 0.661     |
|        Crime&Legal |   2598 |   9291 | 0.28    | 0.781     | 0.72   | 0.75      |
|     Business&Money |   2283 |   6915 | 0.33    | 0.664     | 0.67   | 0.667     |
|        Travel&Food |   2283 |  17725 | 0.129   | 0.867     | 0.871  | 0.869     |
-----------------------------------------------------------------------------------
"""

# Extracting labels and counts
lines = data.split('\n')

# Filter out empty lines
lines = [line for line in lines if line.strip()]

# Assuming the data format is correct, extract labels and counts
labels = [line.split('|')[1].strip() for line in lines[3:-2]]
counts = [int(line.split('|')[3].strip()) for line in lines[3:-2]]

# Plotting the vertical bar chart with count values and added padding
plt.figure(figsize=(10, 6))
bars = plt.bar(labels, counts, color='skyblue')
plt.ylabel('Liczba próbek')
plt.title('Liczba próbek dla każdej kategorii')

# Add count values on top of each bar
for bar, value in zip(bars, counts):
    plt.text(bar.get_x() + bar.get_width() / 2, bar.get_height(), f'{value}', ha='center', va='bottom')

plt.xticks(rotation=45, ha='right')  # Rotate x-axis labels for better readability

# Adjust bottom padding
plt.subplots_adjust(bottom=0.2)

plt.show()
