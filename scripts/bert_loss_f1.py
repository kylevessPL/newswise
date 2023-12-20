import matplotlib.pyplot as plt

data = """
{'loss': 0.5888, 'learning_rate': 2.069700982783857e-05, 'epoch': 1.0}
{'eval_loss': 0.38266250491142273, 'eval_accuracy': 0.8700791876633956, 'eval_recall': 0.8700796079264808, 'eval_precision': 0.8716915423191243, 'eval_f1': 0.8702089111973379, 'eval_runtime': 595.0217, 'eval_samples_per_second': 133.069, 'eval_steps_per_second': 4.16, 'epoch': 1.0}
{'loss': 0.2929, 'learning_rate': 1.0348504913919285e-05, 'epoch': 2.0}
{'eval_loss': 0.30478915572166443, 'eval_accuracy': 0.8970181487515629, 'eval_recall': 0.8970183996890544, 'eval_precision': 0.8973837379449504, 'eval_f1': 0.8969007823719861, 'eval_runtime': 572.7746, 'eval_samples_per_second': 138.238, 'eval_steps_per_second': 4.321, 'epoch': 2.0}
{'loss': 0.183, 'learning_rate': 0.0, 'epoch': 3.0}
{'eval_loss': 0.28694796562194824, 'eval_accuracy': 0.9073996893115599, 'eval_recall': 0.9074002618573253, 'eval_precision': 0.90739921425011, 'eval_f1': 0.9073064001079918, 'eval_runtime': 551.6849, 'eval_samples_per_second': 143.522, 'eval_steps_per_second': 4.486, 'epoch': 3.0}
"""

# Extracting relevant metrics
epochs = set()
train_loss = []
eval_loss = []
eval_f1 = []

for line in data.split('\n'):
    if 'epoch' in line:
        metrics = eval(line)
        epochs.add(metrics['epoch'])
        if 'loss' in metrics:
            train_loss.append(metrics['loss'])
        if 'eval_loss' in metrics:
            eval_loss.append(metrics['eval_loss'])
        if 'eval_f1' in metrics:
            eval_f1.append(metrics['eval_f1'])

epochs = sorted(epochs)

# Plotting for Losses
plt.figure(figsize=(10, 5))
plt.plot(epochs[:len(train_loss)], train_loss, label='Strata treningowa', marker='o')
plt.plot(epochs[:len(eval_loss)], eval_loss, label='Strata walidacyjna', marker='o')
plt.title('Zależność straty treningowej i walidacyjnej od liczby epok')
plt.xlabel('Epoka')
plt.ylabel('Strata')
plt.xticks(epochs)
plt.legend()
plt.grid(True)
plt.show()

# Plotting for F1 Scores (Separate Image)
plt.figure(figsize=(10, 5))
plt.plot(epochs[:len(eval_f1)], eval_f1)
plt.title('Zależność dokładności F1 od liczby epok')
plt.xlabel('Epoka')
plt.ylabel('Dokładność F1')
plt.xticks(epochs)
plt.grid(True)
plt.show()
