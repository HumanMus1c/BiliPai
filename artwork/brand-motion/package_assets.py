"""Deduplicate shipped images; keep the artwork preview self-contained and offline."""
import base64
import copy
import json
from pathlib import Path

ART = Path(__file__).resolve().parent
ROOT = ART.parents[1]
RAW = ROOT / 'brand-motion/src/main/res/raw'
DRAWABLE = ROOT / 'brand-motion/src/main/res/drawable-nodpi'
FALLBACKS = {
    'welcome': 'bilipai_maid_static',
    'clean_complete': 'bilipai_maid_clean_static',
    'cleaning': 'bilipai_maid_cleaning_static',
    'retry': 'bilipai_maid_retry_static',
    'empty': 'bilipai_maid_empty_static',
    'search_empty': 'bilipai_maid_search_empty_static',
    'favorite_saved': 'bilipai_maid_favorite_static',
    'follow_success': 'bilipai_maid_follow_static',
    'unfollow_complete': 'bilipai_maid_unfollow_static',
    'dislike_confirmed': 'bilipai_maid_dislike_static',
    'share_ready': 'bilipai_maid_share_static',
    'coin_success': 'bilipai_maid_coin_static',
    'download_complete': 'bilipai_maid_download_static',
    'triple_success': 'bilipai_maid_triple_static',
}

def build_preview():
    animations = {}
    for state, fallback in FALLBACKS.items():
        key = 'bilipai_maid_' + state
        data = json.loads((RAW / (key + '.json')).read_text())
        for asset in data['assets']:
            if asset.get('id') == 'maid_bitmap':
                png = (DRAWABLE / (fallback + '.png')).read_bytes()
                asset.update(p='data:image/png;base64,' + base64.b64encode(png).decode(), u='', e=1)
        animations[key] = data
    (ART / 'preview.html').write_text((ART / 'preview-template.html').read_text().replace(
        '__ANIMATIONS__', json.dumps(animations, separators=(',', ':'))))

def package_existing():
    before = after = 0
    for state, fallback in FALLBACKS.items():
        path = RAW / ('bilipai_maid_' + state + '.json')
        before += path.stat().st_size
        data = json.loads(path.read_text())
        original = copy.deepcopy(data)
        images = [a for a in data['assets'] if 'p' in a]
        assert len(images) == 1 and images[0]['id'] == 'maid_bitmap'
        asset = images[0]
        if asset['p'].startswith('data:'):
            assert base64.b64decode(asset['p'].split(',', 1)[1], validate=True) == (DRAWABLE / (fallback + '.png')).read_bytes()
        else:
            assert asset['p'] == fallback + '.png'
        asset.update(p=fallback + '.png', u='', e=0)
        # Only image packaging may change; preserve all gestures, masks and timings.
        original['assets'] = data['assets']
        assert original == data
        path.write_text(json.dumps(data, separators=(',', ':')) + '\n')
        after += path.stat().st_size
    build_preview()
    print(f'JSON bytes: {before} -> {after}; all 14 PNGs are shared with static fallbacks.')

if __name__ == '__main__':
    package_existing()
