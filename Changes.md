# Change Log

## v0.4.5 vc13 2012-12-07 Release

 * Bugfix: Issue #11: Fix the runtime exception that causes random failures while loading Gravatars by removing that runtime exception completely.

## v0.4.4 vc12 2012-12-06 Release

 * Bugfix: Issue #11: Attempted to fix the runtime exception that causes random failures while loading Gravatars, but this fix is not successful.
 * Bugfix: Since the last release MetaCPAN has started using HTTPS URLs, which meant that links within the app to other modules opened a browser window instead of another module viewer within the app. This has been fixed.

## v0.4.3 vc11 2012-07-04 Release

 * Bugfix: The change in v0.4.2 to fix AsyncTask problems in Honeycomb and later, breaks Gingerbread and earlier. Fixed it in a way that works on both old and new Android devices.
 * Bugfix: The timer that limits the amount of time the app is allowed to wait for a Gravatar image was not being shutdown correctly if an error occurred while attempting the download. This was causing a force close. This problem has been corrected and the timer should get shutdown properly in all cases. 

## v0.4.2 vc10 2012-07-03 Release

 * Bugfix: Honeycomb and later changes the behavior of AsyncTask to perform tasks synchronously unless explicitly requested for parallel. Changed to fix synchronicity. This will probably speed things up spectacularly on newer phones and tablets from before. (Though, not on my Thrive, which seems to still use the pre-Honeycomb behavior.)

## v0.4.1 vc9 2012-07-01 Not Released

 * Bugfix: Honeycomb and later devices force closed because of how the connectivity test was implemented. The test has been improved so this will not happen in the future.

## v0.4.0 vc8 2012-07-01 Not Released

 * Added a connectivity test, which displays a toast about not being able to contact api.metacpan.org
 * More properly split out support for tablets, phones, Eclair, and Honeycomb functionality, which should be a bit more robust in all environments.
 * Added new layouts for use with displaying releases and some code, but none of this is yet used within the app.
 * Added a live object cache to cache module, author, release, and Gravatar information within the view. This reduces the amount of network traffic generated and allows information already fetched in a search session to be reused immediately when already found.
 * Heavily refactored internals for accessing the MetaCPAN API. These are easier to understand and maintain.
 * The app icon is slightly smaller and has a drop-shadow
 * Added the change log to the project

## v0.3.1 vc7 2012-03-11 Release

 * Bugfix: Fixing duplicate duplicate search box on Ice Cream Sandwich phones. (Issue #9, HT @clintongormley)

## v0.3 vc6 2012-03-10 Release

 * Adds a 2-panel, 1-screen view for tablets
 * Includes a simple help screen on the tablet view
 * Fixes jagginess in some of the app icons
 * Bigger contact icons
    
## v0.1 vc3 2012-02-08 Release

 * Original release
 * Search for CPAN modules
 * See search results with the author's picture, module abstract, ratings, and favorites.
 * Tap on individual results to see the documentation for that module.
 * Follow links within the documentation to see the documentation for other modules.
 * Any link to something other than a module will work, but will go to a browser, even if that link is within CPAN. (Eventually, we hope to change this.)
