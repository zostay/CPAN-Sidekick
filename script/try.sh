#!/bin/bash

case $1 in
    search)
        file="module_search_template.json"
        path="/v0/file/_search"
        ;;

    ratings)
        file="distribution_ratings_template.json"
        path="/rating/_search"
        ;;
esac

curl -d@$file 'http://api.metacpan.org'$path > /tmp/try-metacpan-api.log

cat /tmp/try-metacpan-api.log | json_pp | less
