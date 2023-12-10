import numpy as np
import os
from datasets import load_dataset, Features, Value, ClassLabel
from evaluate import load
from shutil import rmtree
from sklearn.metrics import classification_report
from sklearn.model_selection import StratifiedKFold
from torch import cuda
from transformers import DistilBertForSequenceClassification, DistilBertConfig, TrainingArguments, Trainer, \
    EarlyStoppingCallback, DistilBertTokenizerFast
from transformers.trainer_utils import IntervalStrategy
from warnings import filterwarnings

filterwarnings(action='ignore', category=FutureWarning, module=r'.*datasets')

data_path = os.environ['DATA_PATH']
checkpoints_path = 'checkpoints'

n_folds = 5
class_names = ['Business&Money', 'Crime&Legal', 'Entertainment&Arts', 'Lifestyle', 'SciTech&Education',
               'Society&Religion', 'Sports&Health', 'Travel&Food', 'World&Politics']
features = Features({'text': Value('string'), 'label': ClassLabel(names=class_names)})

rmtree(checkpoints_path, ignore_errors=True)
cuda.set_device('cuda:0')

tokenizer = DistilBertTokenizerFast.from_pretrained('distilbert-base-uncased', truncation_side='left')
tokenizer.deprecation_warnings['Asking-to-pad-a-fast-tokenizer'] = True

config = DistilBertConfig.from_pretrained(
    'distilbert-base-uncased',
    num_labels=len(class_names),
    id2label={str(i): label for i, label in enumerate(class_names)},
    label2id={label: i for i, label in enumerate(class_names)}
)


def preprocess_function(sample):
    args = (sample['text'],)
    result = tokenizer(*args, padding='max_length', truncation=True)
    result['label'] = sample['label']
    return result


accuracy_metric = load('accuracy')
recall_metric = load('recall')
precision_metric = load('precision')
f1_metric = load('f1')


def compute_metrics(eval_pred):
    logits, labels = eval_pred
    preds = np.argmax(logits, axis=-1)
    results = {}
    results.update(accuracy_metric.compute(predictions=preds, references=labels))
    results.update(recall_metric.compute(predictions=preds, references=labels, average='macro', zero_division=0))
    results.update(precision_metric.compute(predictions=preds, references=labels, average='macro', zero_division=0))
    results.update(f1_metric.compute(predictions=preds, references=labels, average='macro'))
    return results


training_args = TrainingArguments(
    output_dir=checkpoints_path,
    learning_rate=5e-5,
    per_device_train_batch_size=32,
    per_device_eval_batch_size=32,
    gradient_accumulation_steps=2,
    num_train_epochs=3,
    warmup_steps=500,
    weight_decay=0.01,
    seed=42,
    evaluation_strategy=IntervalStrategy.EPOCH,
    save_strategy=IntervalStrategy.EPOCH,
    logging_strategy=IntervalStrategy.EPOCH,
    save_total_limit=1,
    metric_for_best_model='eval_loss',
    load_best_model_at_end=True
)

ds = load_dataset(data_path, data_files='data_dl.csv', features=features, split='train')
splits = StratifiedKFold(n_splits=n_folds).split(np.zeros(ds.num_rows), ds['label'])

all_true_labels = []
all_predicted_labels = []


def evaluate():
    print(trainer.evaluate())
    predictions = trainer.predict(val_dataset)
    true_labels = val_dataset['label']
    predicted_labels = predictions.predictions.argmax(axis=1)
    all_true_labels.extend(true_labels)
    all_predicted_labels.extend(predicted_labels)


for idx, (train_idx, val_idx) in enumerate(splits):
    print(f'Started cross-validation fold {idx + 1}')
    train_dataset = ds.select(train_idx).map(preprocess_function, batched=True)
    val_dataset = ds.select(val_idx).map(preprocess_function, batched=True)
    model = DistilBertForSequenceClassification.from_pretrained('distilbert-base-uncased', config=config)
    trainer = Trainer(
        model=model,
        args=training_args,
        train_dataset=train_dataset,
        eval_dataset=val_dataset,
        tokenizer=tokenizer,
        compute_metrics=compute_metrics,
        callbacks=[EarlyStoppingCallback(early_stopping_patience=3)]
    )
    trainer.train()
    evaluate()
    rmtree(checkpoints_path, ignore_errors=True)
    print(f'Finished cross-validation fold {idx + 1}')

print('Overall Classification Report:')
print(classification_report(all_true_labels, all_predicted_labels, target_names=class_names, zero_division=0))
