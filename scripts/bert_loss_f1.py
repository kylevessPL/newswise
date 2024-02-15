import matplotlib.pyplot as plt

data = """
{'loss': 0.5801, 'learning_rate': 2.30830223880597e-05, 'epoch': 1.0}
{'eval_loss': 0.3755791187286377, 'eval_accuracy': 0.872426684179949, 'eval_recall': 0.8724279964064869, 'eval_precision': 0.8731716763396699, 'eval_f1': 0.8721642119087251, 'eval_runtime': 548.9021, 'eval_samples_per_second': 144.248, 'eval_steps_per_second': 4.509, 'epoch': 1.0}
{'loss': 0.2883, 'learning_rate': 1.5388681592039803e-05, 'epoch': 2.0}
{'eval_loss': 0.2975562810897827, 'eval_accuracy': 0.8997701381696936, 'eval_recall': 0.8997712104162068, 'eval_precision': 0.9005437059366341, 'eval_f1': 0.8997298608058711, 'eval_runtime': 536.4585, 'eval_samples_per_second': 147.594, 'eval_steps_per_second': 4.614, 'epoch': 2.0}
{'loss': 0.1733, 'learning_rate': 7.694340796019901e-06, 'epoch': 3.0}
{'eval_loss': 0.2744365930557251, 'eval_accuracy': 0.9123367602111697, 'eval_recall': 0.9123373139205945, 'eval_precision': 0.9121373637621838, 'eval_f1': 0.9120756701896003, 'eval_runtime': 536.5947, 'eval_samples_per_second': 147.556, 'eval_steps_per_second': 4.612, 'epoch': 3.0}
{'loss': 0.1073, 'learning_rate': 0.0, 'epoch': 4.0}
{'eval_loss': 0.2902291417121887, 'eval_accuracy': 0.9161004319381646, 'eval_recall': 0.9161007481445489, 'eval_precision': 0.9160932774040433, 'eval_f1': 0.9160693915085284, 'eval_runtime': 536.5639, 'eval_samples_per_second': 147.565, 'eval_steps_per_second': 4.613, 'epoch': 4.0}
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
plt.title('Zależność miary F1 od liczby epok')
plt.xlabel('Epoka')
plt.ylabel('Miara F1')
plt.xticks(epochs)
plt.grid(True)
plt.show()
