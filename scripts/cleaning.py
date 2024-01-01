import nltk
from cleantext import clean
from contractions import fix
from nltk import pos_tag, word_tokenize
from nltk.corpus import wordnet
from nltk.stem import WordNetLemmatizer
from re import sub, finditer, compile
from unicodedata import normalize

nltk.download('punkt', quiet=True)
nltk.download('averaged_perceptron_tagger', quiet=True)
nltk.download('wordnet', quiet=True)


def full_cleanup(content):
    fixed = fix(content.lower())
    fixed = __remove_accents(fixed)
    fixed = __remove_url(fixed)
    fixed = __remove_digits(fixed)
    fixed = __remove_repeated_characters(fixed)
    fixed = __lemmatize_sentence(fixed)
    fixed = clean(fixed, clean_all=False, extra_spaces=True, stopwords=True, numbers=True, lowercase=True, punct=True)
    fixed = __remove_single_char_word(fixed)
    return fixed


def basic_cleanup(content):
    fixed = fix(content.lower())
    fixed = __remove_accents(fixed)
    fixed = __remove_url(fixed)
    fixed = __remove_repeated_characters(fixed)
    fixed = clean(fixed, clean_all=False, extra_spaces=True, stopwords=True, lowercase=True, punct=True)
    fixed = __remove_single_char_word(fixed)
    return fixed


def __nltk_tag_to_wordnet_tag(nltk_tag):
    if nltk_tag.startswith('J'):
        return wordnet.ADJ
    elif nltk_tag.startswith('V'):
        return wordnet.VERB
    elif nltk_tag.startswith('N'):
        return wordnet.NOUN
    elif nltk_tag.startswith('R'):
        return wordnet.ADV
    else:
        return None


def __lemmatize_sentence(text):
    lemmatizer = WordNetLemmatizer()
    nltk_tagged = pos_tag(word_tokenize(text))
    wordnet_tagged = map(lambda x: (x[0], __nltk_tag_to_wordnet_tag(x[1])), nltk_tagged)
    lemmatized = []
    for word, tag in wordnet_tagged:
        if tag is None:
            lemmatized.append(word)
        else:
            res = lemmatizer.lemmatize(word, tag)
            lemmatized.append(res)
    return ' '.join(lemmatized)


def __remove_accents(text):
    normalized = normalize('NFD', text)
    return normalized.encode('ascii', 'ignore').decode('utf-8')


def __remove_repeated_characters(text):
    regex_pattern = compile(r'(.)\1+')
    clean_text = regex_pattern.sub(r'\1\1', text)
    return clean_text


def __remove_digits(text):
    txt = []
    for each in text.split():
        if not any(x in each.lower() for x in '0123456789'):
            txt.append(each)
    txtsent = " ".join(txt)
    return txtsent


def __remove_single_char_word(text):
    words = text.split()
    filter_words = [word for word in words if len(word) > 1]
    return " ".join(filter_words)


# noinspection RegExpSimplifiable,RegExpRedundantEscape
def __remove_url(text):
    urlfree = []
    for word in text.split():
        if not (word.startswith('www') or word.startswith('http') or word.endswith('.html')):
            urlfree.append(word)
    urlfree = " ".join(urlfree)
    urls = finditer(r'http[\w]*:\/\/[\w]*\.?[\w-]+\.+[\w]+[\/\w]+', urlfree)
    for i in urls:
        urlfree = sub(i.group().strip(), '', urlfree)
    return urlfree
