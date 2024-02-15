import sys
from json import dumps
from nlpaug.augmenter.sentence import ContextualWordEmbsForSentenceAug
from nlpaug.augmenter.word import SynonymAug, AntonymAug, BackTranslationAug
from nlpaug.flow import Sequential

from cleaning import full_cleanup, basic_cleanup


aug = Sequential([
    SynonymAug(aug_src='ppdb', model_path='ppdb-2.0-s-all', aug_max=None, aug_p=100),
    AntonymAug(aug_max=None, aug_p=100),
    BackTranslationAug(from_model_name='Helsinki-NLP/opus-mt-en-jap', to_model_name='Helsinki-NLP/opus-mt-jap-en',
                       max_length=512, device='gpu'),
    ContextualWordEmbsForSentenceAug(model_path='distilgpt2', model_type='gpt2', max_length=512, device='gpu'),
])


def augment(text, number):
    try:
        augmented = aug.augment(text, number)
        if augmented is not None:
            if sys.argv[3] == '1':
                result = [full_cleanup(item) for item in augmented if item is not None]
            else:
                result = [basic_cleanup(item) for item in augmented if item is not None]
            return list(filter(None, result))
        else:
            return []
    except:
        return []


augmented_text = augment(sys.argv[1], int(sys.argv[2]))
print(dumps(augmented_text), end='')
