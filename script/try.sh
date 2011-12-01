#!/bin/bash

curl -d@module_search_template.json 'http://api.metacpan.org/v0/file/_search' | less
