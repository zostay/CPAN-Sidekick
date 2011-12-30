# CPAN Sidekick

CPAN Sidekick is an Android application for searching and viewing CPAN (the
Comprehensive Perl Archive Network). This application is currently in beta
testing and has not yet been released on CPAN. *Beta testers are still needed,
so read on if you are interested in helping to test this application.*

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

## Beta Testing

If you are interested in beta testing. You will first need to know where to get
a copy of the signed, official APK:

> [CPAN Sidekick Downloads](http://github.com/zostay/CPAN-Sidekick/downloads)

Once downloaded and installed on your device, the app will automatically report
any crash. However, we still need issues to be reported and features to be
requested to know what's quirky and what can be made better. 

 * Report bugs to the [Issue Tracker](http://github.com/zostay/CPAN-Sidekick/issues).
 * Please be sure to include these details:
   * Manufacturer
   * Device Make
   * Data Provider
   * Data Service Type
   * OS Version
 * If relevant, to your request, you may want to include the size of the screen
   or type of phone as well (i.e., is it a 7" tablet or a phone with a slide-out
   keyboard, etc.).

### Getting Started with Android 2.x (Froyo or Gingerbread)

These instructions are the ones to use for most Android phones and many smaller
tablets.

1. Go to the Home screen.
2. Tap on the Menu button.
3. Tap on the Settings menu.
4. Tap on the Applications tab.
5. Make sure that the checkbox next to “Unknown sources” is checked.
6. Download the application:
   * Option 1: On your PC/Mac go to the [downloads](http://github.com/zostay/CPAN-Sidekick/downloads) 
     page and click on the latest version. Scan the QR code that appears with a
     QR code scanner on your phone.
   * Option 2: Open the web browser and go to: github.com/zostay/CPAN-Sidekick/downloads
     and select the latest version for download.
   * Option 3: On your PC/Mac go to the [downloads](http://github.com/zostay/CPAN-Sidekick/downloads)
     page and download the latest version. Copy the file over to your phone
     using your USB cable or SD card.
7. Use the download link to download the application to your phone.
8. When the download finishes, tap on the Menu button and tap on the Downloads
   menu item.
9. Tap on the CPAN-Sidekick APK file you just downloaded.
10. Follow the instructions to install the application.
11. Open the application and enjoy.

Remember to submit any issues or suggestions you have to the [Issue Tracker](http://github.com/zostay/CPAN-Sidekick/issues).

### Getting Started with Android 3.x (Honeycomb)

These instructions are the ones to use for some smaller tablets and nearly all
larger tablets.

1. Go to the Home screen.
2. Tap on the Apps button in the upper right corner.
3. Find the Settings icon and tap it.
4. Tap on the Applications tab.
5. Make sure the checkbox next to “Unknown sources” is checked.
6. Download the application:
   * Option 1: On your PC/Mac go to the [downloads](http://github.com/zostay/CPAN-Sidekick/downloads) 
     page and click on the latest version. Scan the QR code that appears with a
     QR code scanner on your tablet.
   * Option 2: Open the web browser and go to: github.com/zostay/CPAN-Sidekick/downloads
     and select the latest version for download.
   * Option 3: On your PC/Mac go to the [downloads](http://github.com/zostay/CPAN-Sidekick/downloads)
     page and download the latest version. Copy the file over to your tablet
     using your USB cable or SD card.
7. Use the Download link to download the application to your tablet.
8. When the download finishes, tap on the Menu button and tap on the Downloads
   menu item.
9. Tap on the CPAN-Sidekick APK file you just downloaded.
10. Follow the instructions to install the application.
11. Open the application and enjoy.

Remember to submit any issues or suggestions you have to the [Issue Tracker](http://github.com/zostay/CPAN-Sidekick/issues).

### Getting Started with Android 4.x (Ice Cream Sandwich)

You are on your own for now. I have not tested and do not own a ICS device. If
someone needs help and neither of the instructions above are helpful, I can use
an emulator to come up installation instructions. Please submit an issue if you
have this need.

### Getting Started with the Kindle Fire or Nook Color

This is not going to happen. The application may become available for on the
*Kindle Appstore* in the future (hhe *Nook* store seems like a long shot at this
point), but my understanding is that these devices must be rooted to install
apps not from that store. If you have such a rooted device, then I'm sure you
can install the application without any help from me.

## Copyright and License

Copyright 2011 Qubling Software LLC.

CPAN Sidekick and its source is distributed under the terms of the Artistic
License 2.0 (same as Perl 6).
