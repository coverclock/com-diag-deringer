# com-diag-deringer

COPYRIGHT

Copyright 2017 by the  Digital Aggregates Corporation, Arvada Colorado USA.

LICENSE

Licensed under the terms of the Apache license v2.0.

CONTACT

    Chip Overclock
    Digital Aggregates Corporation
    3440 Youngfield Street, Suite 209
    Wheat Ridge CO 80033 USA
    http://www.diag.com
    mailto:coverclock@diag.com

ABSTRACT

These are musings with Android Things (the platform formerly known as
"brillo") on the Raspberry Pi 3.

Android Things provides low level drivers for common embedded interfaces
like the Serial Peripheral Interface (SPI) bus, the Inter-Integrated
Circuit (I2C) bus, Pulse Width Modulation (PWM), and General Purpose
Input/Output (GPIO). Third party software drivers are available for
peripherals that connect to the Raspberry Pi using these hardware
interfaces.

* The Apa102 LED strip driver uses SPI.
* The Bmx280 temperature and pressure sensor driver uses I2C.
* The Ht16k33 alphanumeric segment driver uses I2C.
* The Piezoelectric buzzer driver uses PWM.
* The button driver uses GPIO.
The LED driver uses GPIO.

So far I have written four toy applications that run under Android
Things using Android Studio. You can find all of them in this repository,
the project for which is code-named "Deringer".

* AndroidThingsPeripheralIO is my implementation of the first exercise in
the Google Codelabs tutorial for Android Things peripherals. It uses the
buttons and the LEDs.
* AndroidThingsPeripheralDriver is my implementation of the second exercise
(a minor variation on the first) in the Google Codelabs tutorial for
Android Things peripherals. It uses the buttons and the LEDs.
* RainbowHatDemo is a single-threaded demo of the peripherals (except for
the buttons) on the Pimoroni Rainbow Hat for the Raspberry Pi 3.
* RainbowHatThing is a multi-threaded demo of the peripherals (except for
the buttons) on the Pimoroni Rainbow Hat for the Raspberry Pi 3.

IMAGES AND VIDEOS

<https://www.flickr.com/photos/johnlsloan/albums/72157681393247802>

<https://www.youtube.com/watch?v=DHEZW0UPd2M>

REPOSITORIES

<https://github.com/coverclock/com-diag-deringer>

<https://github.com/amitshekhariitbhu/awesome-android-things>

<https://github.com/androidthings/contrib-drivers>

<https://github.com/androidthings/sample-simpleui>

RESOURCES

<https://developer.android.com/things/index.html>

<https://developer.android.com/things/sdk/index.html>

<https://codelabs.developers.google.com/codelabs/androidthings-peripherals/#0>

<https://developer.android.com/reference/android/graphics/Color.html>

<https://cpldcpu.com/2014/08/27/apa102/>

<https://cpldcpu.files.wordpress.com/2014/08/apa-102-super-led-specifications-2013-en.pdf>

<https://cpldcpu.files.wordpress.com/2014/08/apa-102c-super-led-specifications-2014-en.pdf>

<http://hackaday.com/2014/12/09/digging-into-the-apa102-serial-led-protocol/>

<https://blog.stylingandroid.com/simple-things-part-4/>

NOTES

    adb uninstall com.diag.deringer.androidthingsperipheralio
    adb uninstall com.diag.deringer.androidthingsperipheraldriver
    adb uninstall com.diag.deringer.rainbowhatdemo
    adb uninstall com.diag.deringer.rainbowhatthing
    adb uninstall com.example.androidthings.simpleui

