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

    authors)
        file="author_by_pauseid_template.json"
        path="/author/_search"
        ;;

    favorites)
        file="distribution_favorites_template.json"
        path="/favorite/_search"
        ;;

    modules)
        file="modules_for_release_template.json"
        path="/file/_search"
        ;;

    *)
        echo "Say what!?"
        exit
        ;;
esac

curl -d@$file 'http://api.metacpan.org'$path > /tmp/try-metacpan-api.log

cat /tmp/try-metacpan-api.log | json_pp | less
