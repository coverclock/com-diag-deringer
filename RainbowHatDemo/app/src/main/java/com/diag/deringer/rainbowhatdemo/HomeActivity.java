package com.diag.deringer.rainbowhatdemo;

// Copyright 2017 by the Digital Aggregates Corporation, Arvada Colorado USA.
// Licensed under the terms of the Apache License version 2.0.
// https://github.com/androidthings/contrib-drivers/tree/master/rainbowhat
// mailto:coverclock@diag.com
// https://github.com/coverclock/com-diag-deringer

import android.app.Activity;
import android.os.Bundle;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.contrib.driver.ht16k33.Ht16k33;
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;
import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.bmx280.Bmx280;
import com.google.android.things.pio.Gpio;
import java.io.IOException;
import java.lang.InterruptedException;
import android.util.Log;
import android.graphics.Color;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class HomeActivity extends Activity {

    private static final String TAG = "HomeActivity";

    protected static Gpio red;

    protected static Gpio green;

    protected static Gpio blue;

    protected static Bmx280 sensor;

    protected static AlphanumericDisplay segment;

    protected static Apa102 strip;

    protected static Speaker buzzer;

    protected void doOpen() throws java.io.IOException {

        red = RainbowHat.openLedRed();

        green = RainbowHat.openLedGreen();

        blue = RainbowHat.openLedBlue();

        sensor = RainbowHat.openSensor();

        segment = RainbowHat.openDisplay();

        strip = RainbowHat.openLedStrip();

        buzzer = RainbowHat.openPiezo();

    }

    protected void doStart() throws java.io.IOException {

        blue.setValue(false);
        green.setValue(false);
        red.setValue(false);

        sensor.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X);
        sensor.setPressureOversampling(Bmx280.OVERSAMPLING_1X);

        segment.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX);
        segment.setEnabled(true);

        strip.setBrightness(Apa102.MAX_BRIGHTNESS);

    }

    protected void doStop() throws java.io.IOException {

        buzzer.stop();

        int[] rainbow = new int[RainbowHat.LEDSTRIP_LENGTH];
        strip.setBrightness(0);
        for (int i = 0; i < rainbow.length; i++) {
            rainbow[i] = Color.HSVToColor(0xff, new float[] { 0.0f, 1.0f, 1.0f });
        }
        strip.write(rainbow);
        for (int i = 0; i < rainbow.length; i++) {
            rainbow[i] = ~0;
        }
        strip.write(rainbow);

        segment.setEnabled(false);

        blue.setValue(false);
        green.setValue(false);
        red.setValue(false);

    }

    protected void doClose() throws java.io.IOException {

        buzzer.close();

        strip.close();

        segment.close();

        sensor.close();

        blue.close();
        green.close();
        red.close();

    }

    protected void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            /* Do nothing. */
        }
    }



    protected void strip_flush() throws java.io.IOException {
        int[] rainbow = new int[RainbowHat.LEDSTRIP_LENGTH];
        for (int i = 0; i < rainbow.length; i++) {
            rainbow[i] = ~0;
        }
        strip.write(rainbow);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            doOpen();

            doStart();

            red.setValue(true);

            float[] readings = sensor.readTemperatureAndPressure();
            float centigrade = readings[0];
            float fahrenheit = (centigrade * 9.0f / 5.0f) + 32.0f;
            float hectopascals = readings[1];
            float inches = hectopascals * 0.02953f;
            Log.i(TAG, centigrade + "C " + fahrenheit + "F " + hectopascals + "hPa " + inches + "in");
            segment.display(fahrenheit);

            int[] rainbow = new int[RainbowHat.LEDSTRIP_LENGTH];
            for (int ii = 0; ii < rainbow.length; ii++) {
                rainbow[ii] = Color.HSVToColor(0xff, new float[] { ii * 360.0f / rainbow.length, 1.0f, 1.0f });
            }
            strip.write(rainbow);
            strip_flush();

            buzzer.play(460);
            pause(150);
            buzzer.stop();
            buzzer.play(500);
            pause(250);
            buzzer.stop();
            for (int ii = 0; ii < 2; ++ii) {
                buzzer.play(500);
                pause(150);
                buzzer.stop();
            }
            buzzer.play(550);
            pause(250);
            buzzer.stop();
            for (int ii = 0; ii < 2; ++ii) {
                buzzer.play(550);
                pause(150);
                buzzer.stop();
            }
            buzzer.play(610);
            pause(250);
            buzzer.stop();
            for (int ii = 0; ii < 2; ++ii) {
                buzzer.play(610);
                pause(150);
                buzzer.stop();
            }
            buzzer.play(470);
            pause(250);
            buzzer.stop();
            for (int ii = 0; ii < 2; ++ii) {
                buzzer.play(470);
                pause(150);
                buzzer.stop();
            }
            buzzer.play(500);
            pause(250);
            buzzer.stop();

            pause(1000);

            red.setValue(false);
            green.setValue(true);

            for (int ii = 0; ii < rainbow.length; ii++) {
                rainbow[ii] = Color.HSVToColor(0xff, new float[] { ii * 360.0f / rainbow.length, 1.0f, 1.0f });
            }
            strip.write(rainbow);
            strip_flush();

            for (float frequency = 20.0f; frequency <= 20000.0f; frequency *= 1.1f) {
                Log.i(TAG, frequency + "Hz");
                buzzer.play(frequency);
                pause(100);
            }
            buzzer.stop();

            pause(1000);

            green.setValue(false);
            blue.setValue(true);

            for (int ii = 0; ii < rainbow.length; ii++) {
                rainbow[ii] = Color.HSVToColor(0xff, new float[] { ii * 360.0f / rainbow.length, 1.0f, 1.0f });
            }
            strip.write(rainbow);
            strip_flush();

            buzzer.play(294);
            pause(1000);
            buzzer.play(330);
            pause(1000);
            buzzer.play(262);
            pause(1000);
            buzzer.play(131);
            pause(1000);
            buzzer.play(196);
            pause(1000);
            buzzer.stop();

            segment.display("-");
            pause(100);
            segment.display("\\");
            pause(100);
            segment.display("|");
            pause(100);
            segment.display("/");
            pause(100);

            segment.display(" -");
            pause(100);
            segment.display(" \\");
            pause(100);
            segment.display(" |");
            pause(100);
            segment.display(" /");
            pause(100);

            segment.display("  -");
            pause(100);
            segment.display("  \\");
            pause(100);
            segment.display("  |");
            pause(100);
            segment.display("  /");
            pause(100);

            segment.display("   -");
            pause(100);
            segment.display("   \\");
            pause(100);
            segment.display("   |");
            pause(100);
            segment.display("   /");
            pause(100);

            doStop();

            doClose();

        } catch (IOException e) {
            Log.e(TAG, "Failed! " + e);
        }
    }
}
