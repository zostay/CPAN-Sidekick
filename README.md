# CPAN Sidekick

CPAN Sidekick is an Android application for searching and viewing CPAN (the
Comprehensive Perl Archive Network). This application is now available on
Google Play.

 * <https://play.google.com/store/apps/details?id=com.qubling.sidekick>

## Features

The feature list is not very long yet. We are focusing on getting each feature
implemented cleanly before moving on to add more features, but a useful core is
already present:

 * Search for CPAN modules
 * See search results with the author's picture, module abstract, ratings, and
   favorites.
 * Tap on individual results to see the documentation for that module.
 * Follow links within the documentation to see the documentation for other
   modules.
 * Any link to something other than a module will work, but will go to a
   browser, even if that link is within CPAN. (Eventually, we hope to change
   this.)
 * Two panel view for tablets.

## Possible Upcoming Features

These are not formally spec'd or anything yet, just vague intentions at this
point:

 * Provide in-app features that mirror all the basic functionality provided by
   the MetaCPAN API:
   * Author information
   * Release information
   * User profiles
   * Favorite voting
   * Etc.
 * Provide the ability to load portions of the documentation or even entire
   release archives for offline use. (A CPAN::Mini mirror currently sits around
   2GB, so someone could conceivably load all of the POD for CPAN onto a tablet 
   or a larger SD card.)
 * Integrate with SL4A to do on device scripting (maybe?)
 * Provide tools for other applications/developers to hook in if they so desire.
 * Port to iOS or other OSes?

You may wish to take a look at the
[issues](https://github.com/zostay/CPAN-Sidekick/issues) if you want to see
anything like an actual roadmap. It's not that format at this point.

## Copyright and License

Copyright 2012 Qubling Software LLC.

CPAN Sidekick and its source is distributed under the terms of the Artistic
License 2.0 (same as Perl 6).
