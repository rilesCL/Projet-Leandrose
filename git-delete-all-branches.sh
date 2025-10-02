#!/bin/bash

git fetch --prune
for i in `git branch -a | grep -v HEAD | grep -v main`; do git branch -D $i; done
