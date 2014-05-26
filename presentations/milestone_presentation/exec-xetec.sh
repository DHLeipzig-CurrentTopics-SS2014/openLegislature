#!/usr/bin/env bash

FILENAME_WITHOUT_TEX=$(echo $1 | cut -d '.' -f 1)
echo $FILENAME_WITHOUT_TEX
xelatex -interaction=nonstopmode --src-specials $1 && \
#bibtex $FILENAME_WITHOUT_TEX && \
biber $FILENAME_WITHOUT_TEX  && \
xelatex -interaction=nonstopmode --src-specials $1 && \
xelatex -interaction=nonstopmode --src-specials $1
