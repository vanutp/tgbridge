import json
import re
from pathlib import Path

import httpx

DATA_URL = 'https://raw.githubusercontent.com/milesj/emojibase/abb21ab07ed1d155d4fc38c8aab42eb773f65f36/packages/data/en/shortcodes/emojibase.raw.json'
CODEGEN_DIR_PATH = Path(__file__).absolute().parent
REPO_ROOT = CODEGEN_DIR_PATH.parent
TARGET_FILE_PATH = REPO_ROOT / 'common' / 'src' / 'main' / 'resources' / 'replacements.json'
SKIP_SHORTCODES_RE = re.compile(r'tone\d+(?:-\d+)?$')

resp = httpx.get(DATA_URL)
resp.raise_for_status()
data = json.loads(resp.text)

res = {}

for k, v in data.items():
    if isinstance(v, str):
        v = [v]
    emoji = ''.join(chr(int(x, 16)) for x in k.split('-'))
    for shortcode in v:
        if SKIP_SHORTCODES_RE.search(shortcode):
            continue
        res[f':{shortcode}:'] = emoji

with open(TARGET_FILE_PATH, 'w') as f:
    json.dump(res, f, indent=2, ensure_ascii=False)
