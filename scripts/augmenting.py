import sys
from json import dumps
# noinspection PyPackageRequirements
from nlpaug.augmenter.word import SynonymAug, AntonymAug
# noinspection PyPackageRequirements
from nlpaug.flow import Sequential

from cleaning import full_cleanup, basic_cleanup

# noinspection PyTypeChecker
aug = Sequential([
    SynonymAug(aug_src='wordnet', aug_max=None, aug_p=100),
    AntonymAug(aug_max=None, aug_p=100)
])


# noinspection PyBroadException
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
