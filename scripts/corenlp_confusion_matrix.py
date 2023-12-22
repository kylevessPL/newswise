import numpy as np
import re
from matplotlib import pyplot as plt
from sklearn.metrics import ConfusionMatrixDisplay

data = """
INFO 
INFO ### Fold 0
INFO Built this classifier: LinearClassifier with 418375 features, 9 classes, and 3765375 parameters.
INFO 
INFO 79178 examples in test set
INFO Cls Business&Money: TP=6911 FN=1970 FP=2023 TN=68274; Acc 0.950 P 0.774 R 0.778 F1 0.776
INFO Cls Travel&Food: TP=6810 FN=2109 FP=1229 TN=69030; Acc 0.958 P 0.847 R 0.764 F1 0.803
INFO Cls World&Politics: TP=8584 FN=224 FP=5927 TN=64443; Acc 0.922 P 0.592 R 0.975 F1 0.736
INFO Cls Entertainment&Arts: TP=5883 FN=2803 FP=2228 TN=68264; Acc 0.936 P 0.725 R 0.677 F1 0.700
INFO Cls SciTech&Education: TP=5263 FN=3319 FP=1426 TN=69170; Acc 0.940 P 0.787 R 0.613 F1 0.689
INFO Cls Crime&Legal: TP=6640 FN=2187 FP=3740 TN=66611; Acc 0.925 P 0.640 R 0.752 F1 0.691
INFO Cls Sports&Health: TP=4958 FN=3839 FP=1378 TN=69003; Acc 0.934 P 0.783 R 0.564 F1 0.655
INFO Cls Lifestyle: TP=5528 FN=3245 FP=3653 TN=66752; Acc 0.913 P 0.602 R 0.630 F1 0.616
INFO Cls Society&Religion: TP=5558 FN=3347 FP=1439 TN=68834; Acc 0.940 P 0.794 R 0.624 F1 0.699
INFO Accuracy/micro-averaged F1: 0.70897
INFO Macro-averaged F1: 0.70739
INFO 
INFO ### Fold 1
INFO Built this classifier: LinearClassifier with 418375 features, 9 classes, and 3765375 parameters.
INFO 
INFO 79178 examples in test set
INFO Cls Business&Money: TP=6695 FN=2035 FP=2057 TN=68391; Acc 0.948 P 0.765 R 0.767 F1 0.766
INFO Cls Travel&Food: TP=6680 FN=2024 FP=1215 TN=69259; Acc 0.959 P 0.846 R 0.767 F1 0.805
INFO Cls World&Politics: TP=8618 FN=226 FP=6086 TN=64248; Acc 0.920 P 0.586 R 0.974 F1 0.732
INFO Cls Entertainment&Arts: TP=5929 FN=2861 FP=2191 TN=68197; Acc 0.936 P 0.730 R 0.675 F1 0.701
INFO Cls SciTech&Education: TP=5531 FN=3345 FP=1321 TN=68981; Acc 0.941 P 0.807 R 0.623 F1 0.703
INFO Cls Crime&Legal: TP=6512 FN=2218 FP=3683 TN=66765; Acc 0.925 P 0.639 R 0.746 F1 0.688
INFO Cls Sports&Health: TP=5069 FN=3763 FP=1380 TN=68966; Acc 0.935 P 0.786 R 0.574 F1 0.663
INFO Cls Lifestyle: TP=5654 FN=3261 FP=3629 TN=66634; Acc 0.913 P 0.609 R 0.634 F1 0.621
INFO Cls Society&Religion: TP=5438 FN=3319 FP=1490 TN=68931; Acc 0.939 P 0.785 R 0.621 F1 0.693
INFO Accuracy/micro-averaged F1: 0.70886
INFO Macro-averaged F1: 0.70819
INFO 
INFO ### Fold 2
INFO Built this classifier: LinearClassifier with 418375 features, 9 classes, and 3765375 parameters.
INFO 
INFO 79178 examples in test set
INFO Cls Business&Money: TP=6831 FN=1956 FP=1970 TN=68421; Acc 0.950 P 0.776 R 0.777 F1 0.777
INFO Cls Travel&Food: TP=6929 FN=2005 FP=1161 TN=69083; Acc 0.960 P 0.856 R 0.776 F1 0.814
INFO Cls World&Politics: TP=8459 FN=230 FP=6135 TN=64354; Acc 0.920 P 0.580 R 0.974 F1 0.727
INFO Cls Entertainment&Arts: TP=5952 FN=2896 FP=2204 TN=68126; Acc 0.936 P 0.730 R 0.673 F1 0.700
INFO Cls SciTech&Education: TP=5457 FN=3314 FP=1372 TN=69035; Acc 0.941 P 0.799 R 0.622 F1 0.700
INFO Cls Crime&Legal: TP=6622 FN=2193 FP=3581 TN=66782; Acc 0.927 P 0.649 R 0.751 F1 0.696
INFO Cls Sports&Health: TP=4994 FN=3752 FP=1380 TN=69052; Acc 0.935 P 0.783 R 0.571 F1 0.661
INFO Cls Lifestyle: TP=5574 FN=3195 FP=3559 TN=66850; Acc 0.915 P 0.610 R 0.636 F1 0.623
INFO Cls Society&Religion: TP=5501 FN=3318 FP=1497 TN=68862; Acc 0.939 P 0.786 R 0.624 F1 0.696
INFO Accuracy/micro-averaged F1: 0.71130
INFO Macro-averaged F1: 0.71027
INFO 
INFO ### Fold 3
INFO Built this classifier: LinearClassifier with 418375 features, 9 classes, and 3765375 parameters.
INFO 
INFO 79178 examples in test set
INFO Cls Business&Money: TP=6740 FN=2023 FP=2094 TN=68321; Acc 0.948 P 0.763 R 0.769 F1 0.766
INFO Cls Travel&Food: TP=6745 FN=1970 FP=1173 TN=69290; Acc 0.960 P 0.852 R 0.774 F1 0.811
INFO Cls World&Politics: TP=8511 FN=253 FP=5874 TN=64540; Acc 0.923 P 0.592 R 0.971 F1 0.735
INFO Cls Entertainment&Arts: TP=5912 FN=2900 FP=2117 TN=68249; Acc 0.937 P 0.736 R 0.671 F1 0.702
INFO Cls SciTech&Education: TP=5512 FN=3329 FP=1387 TN=68950; Acc 0.940 P 0.799 R 0.623 F1 0.700
INFO Cls Crime&Legal: TP=6678 FN=2234 FP=3668 TN=66598; Acc 0.925 P 0.645 R 0.749 F1 0.694
INFO Cls Sports&Health: TP=5135 FN=3665 FP=1490 TN=68888; Acc 0.935 P 0.775 R 0.584 F1 0.666
INFO Cls Lifestyle: TP=5491 FN=3198 FP=3533 TN=66956; Acc 0.915 P 0.608 R 0.632 F1 0.620
INFO Cls Society&Religion: TP=5531 FN=3351 FP=1587 TN=68709; Acc 0.938 P 0.777 R 0.623 F1 0.691
INFO Accuracy/micro-averaged F1: 0.71049
INFO Macro-averaged F1: 0.70951
INFO 
INFO ### Fold 4
INFO Built this classifier: LinearClassifier with 418375 features, 9 classes, and 3765375 parameters.
INFO 
INFO 79180 examples in test set
INFO Cls Business&Money: TP=6797 FN=2030 FP=1970 TN=68383; Acc 0.949 P 0.775 R 0.770 F1 0.773
INFO Cls Travel&Food: TP=6690 FN=2026 FP=1214 TN=69250; Acc 0.959 P 0.846 R 0.768 F1 0.805
INFO Cls World&Politics: TP=8629 FN=254 FP=6051 TN=64246; Acc 0.920 P 0.588 R 0.971 F1 0.732
INFO Cls Entertainment&Arts: TP=6002 FN=2850 FP=2174 TN=68154; Acc 0.937 P 0.734 R 0.678 F1 0.705
INFO Cls SciTech&Education: TP=5564 FN=3354 FP=1304 TN=68958; Acc 0.941 P 0.810 R 0.624 F1 0.705
INFO Cls Crime&Legal: TP=6447 FN=2257 FP=3684 TN=66792; Acc 0.925 P 0.636 R 0.741 F1 0.685
INFO Cls Sports&Health: TP=5017 FN=3796 FP=1457 TN=68910; Acc 0.934 P 0.775 R 0.569 F1 0.656
INFO Cls Lifestyle: TP=5608 FN=3234 FP=3656 TN=66682; Acc 0.913 P 0.605 R 0.634 F1 0.619
INFO Cls Society&Religion: TP=5358 FN=3267 FP=1558 TN=68997; Acc 0.939 P 0.775 R 0.621 F1 0.690
INFO Accuracy/micro-averaged F1: 0.70866
INFO Macro-averaged F1: 0.70777
"""

# Split the log output into lines and extract TP, FP, FN, TN values
lines = data.split("\n")

class_labels = set()

# Initialize variables to accumulate TP, FP, FN, and TN values
all_TP = []
all_FP = []
all_FN = []
all_TN = []

for line in lines:
    if line.startswith("INFO Cls"):
        match = re.search(r'Cls ([A-Za-z0-9&]+):', line)
        if match:
            class_labels.add(match.group(1))
            all_TP.append(0)
            all_FP.append(0)
            all_FN.append(0)
            all_TN.append(0)

# Convert set to list for consistent indexing
class_labels = list(class_labels)
num_classes = len(class_labels)

for line in lines:
    if line.startswith("INFO Cls"):
        parts = line.split()
        match = re.search(r'Cls ([A-Za-z0-9&]+):', line)
        if match:
            class_name = match.group(1)
            class_index = class_labels.index(class_name)

            TP = int(parts[3].split("=")[1])
            FN = int(parts[4].split("=")[1])
            FP = int(parts[5].split("=")[1])
            TN = int(parts[6].split("=")[1][:-1])

            # Accumulate values
            all_TP[class_index] += TP
            all_FP[class_index] += FP
            all_FN[class_index] += FN
            all_TN[class_index] += TN

# Display confusion matrices for each class
for i in range(num_classes):
    conf_matrix = np.array([[all_TN[i], all_FP[i]], [all_FN[i], all_TP[i]]])
    fig, ax = plt.subplots(figsize=(8.5, 7))
    disp = ConfusionMatrixDisplay(confusion_matrix=conf_matrix, display_labels=['Inna', class_labels[i]])
    disp.plot(cmap='viridis', values_format='.0f', ax=ax)
    ax.set_xlabel('Przewidywana kategoria')
    ax.set_ylabel('Rzeczywista kategoria')
    plt.subplots_adjust(right=1.02)
    plt.title(f'Macierz pomyłek: {class_labels[i]}')
    plt.show()
