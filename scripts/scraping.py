import sys
from trafilatura import fetch_url, extract

from cleaning import full_cleanup, basic_cleanup


def preprocess(content):
    return full_cleanup(content) if sys.argv[2] == '1' else basic_cleanup(content)


# noinspection PyBroadException
def get_article_content(url):
    try:
        html = fetch_url(url)
        content = extract(html, include_links=False, include_comments=False, include_images=False, include_tables=False)
        return preprocess(content) if content is not None else ""
    except:
        return ""


article_text = get_article_content(sys.argv[1])
print(article_text, end='')
