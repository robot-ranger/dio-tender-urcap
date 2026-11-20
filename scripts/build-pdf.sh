#!/usr/bin/env bash
# Build the MkDocs site as a single PDF.
# Requires: pip install mkdocs-material pymdown-extensions mkdocs-print-site-plugin playwright
#           playwright install --with-deps chromium
set -euo pipefail
cd "$(dirname "$0")/.."
OUT="${1:-DIO_Tender_Docs.pdf}"

python3 -m mkdocs build -f mkdocs-pdf.yml --clean
python3 - "$OUT" <<'PY'
import pathlib, sys
from playwright.sync_api import sync_playwright
page_path = pathlib.Path("build/pdf-site/print_page/index.html").resolve()
out = sys.argv[1]
with sync_playwright() as p:
    b = p.chromium.launch()
    pg = b.new_page()
    pg.goto(page_path.as_uri(), wait_until="networkidle")
    pg.emulate_media(media="print")
    pg.pdf(path=out, format="Letter", print_background=True,
           margin={"top": "15mm", "bottom": "15mm", "left": "12mm", "right": "12mm"})
    b.close()
print(f"Wrote {out}")
PY
