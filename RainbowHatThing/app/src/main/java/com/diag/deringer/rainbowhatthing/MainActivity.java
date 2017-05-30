package com.diag.deringer.rainbowhatthing;

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
import java.lang.Boolean;
import android.util.Log;
import android.graphics.Color;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    public void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            /* Do nothing. */
        }
    }

    protected class Lifecycle extends Thread {

        private Boolean running = new Boolean(false);

        public void lifecycleOpen() throws java.io.IOException {
            Log.i(getClass().getSimpleName(), "open");
        }

        public void lifecycleClose() throws java.io.IOException {
            Log.i(getClass().getSimpleName(), "close");
        }

        public void lifecycleEnable() throws java.io.IOException {
            Log.i(getClass().getSimpleName(), "enable");
        }

        public void lifecycleDisable() throws java.io.IOException {
            Log.i(getClass().getSimpleName(), "disable");
        }

        public void lifecycleStart() {
            Log.i(getClass().getSimpleName(), "start");
            synchronized (running) {
                if (!running) {
                    running = true;
                    start();
                }
            }
        }

        public void lifecycleStop() {
            Log.i(getClass().getSimpleName(), "stop");
            synchronized (running) {
                running = false;
                interrupt();
            }
        }

        public void lifecycleWait() {
            Log.i(getClass().getSimpleName(), "wait");
            boolean done;

            synchronized (running) {
                running = false;
                interrupt();
            }
            do {
                try {
                    join();
                    done = true;
                } catch (InterruptedException e) {
                    done = false;
                }
            } while (!done);
        }

        public boolean lifecycleDone() {
            boolean done;

            synchronized (running) {
                done = !running;
            }

            if (done) {
                Log.i(getClass().getSimpleName(), "done");
            }

            return done;
        }

    }

    protected class Leds extends Lifecycle {

        protected Gpio red;
        protected Gpio green;
        protected Gpio blue;

        @Override
        public void lifecycleOpen() throws java.io.IOException {
            super.lifecycleOpen();
            red = RainbowHat.openLedRed();
            green = RainbowHat.openLedGreen();
            blue = RainbowHat.openLedBlue();
        }

        @Override
        public void lifecycleClose() throws java.io.IOException {
            super.lifecycleClose();
            blue.close();
            green.close();
            red.close();
        }

        @Override
        public void lifecycleEnable() throws java.io.IOException {
            super.lifecycleEnable();
            red.setValue(false);
            green.setValue(false);
            blue.setValue(false);
        }

        @Override
        public void lifecycleDisable() throws java.io.IOException {
            super.lifecycleDisable();
            blue.setValue(false);
            green.setValue(false);
            red.setValue(false);
        }

        @Override
        public void run() {
            Log.i(getClass().getSimpleName(), "begin");
            while (!lifecycleDone()) {
                for (int ii = 0; ii < 8; ++ii) {
                    try {
                        red.setValue((ii & 0x4) != 0);
                        green.setValue((ii & 0x2) != 0);
                        blue.setValue((ii & 0x1) != 0);
                    } catch (IOException e) {
                        // Do nothing.
                    }
                    pause(100);
                }
            }
            Log.i(getClass().getSimpleName(), "end");
        }

    }

    protected class Sensor extends Lifecycle {

        protected Bmx280 sensor;

        @Override
        public void lifecycleOpen() throws java.io.IOException {
            super.lifecycleOpen();
            sensor = RainbowHat.openSensor();
        }

        @Override
        public void lifecycleClose() throws java.io.IOException {
            super.lifecycleClose();
            sensor.close();
        }

        @Override
        public void lifecycleEnable() throws java.io.IOException {
            super.lifecycleEnable();
            sensor.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X);
            sensor.setPressureOversampling(Bmx280.OVERSAMPLING_1X);
        }

        @Override
        public void run() {
            Log.i(getClass().getSimpleName(), "begin");
            while (!lifecycleDone()) {
                try {
                    float[] readings = sensor.readTemperatureAndPressure();
                    float centigrade = readings[0];
                    float fahrenheit = (centigrade * 9.0f / 5.0f) + 32.0f;
                    float hectopascals = readings[1];
                    float inches = hectopascals * 0.02953f;
                    Log.i(TAG, centigrade + "C " + fahrenheit + "F " + hectopascals + "hPa " + inches + "in");
                    pause(1000);
                } catch (IOException e) {
                    // Do nothing.
                }
            }
            Log.i(getClass().getSimpleName(), "end");
        }

    }

    protected class Segment extends Lifecycle {

        protected AlphanumericDisplay segment;

        private String[] spinner = new String[] {
                "-",    "\\",    "|",    "/",
                " -",   " \\",   " |",   " /",
                "  -",  "  \\",  "  |",  "  /",
                "   -", "   \\", "   |", "   /"
        };

        @Override
        public void lifecycleOpen() throws java.io.IOException {
            super.lifecycleOpen();
            segment = RainbowHat.openDisplay();
        }

        @Override
        public void lifecycleClose() throws java.io.IOException {
            super.lifecycleClose();
            segment.close();
        }

        @Override
        public void lifecycleEnable() throws java.io.IOException {
            super.lifecycleEnable();
            segment.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX);
            segment.setEnabled(true);
        }

        @Override
        public void lifecycleDisable() throws java.io.IOException {
            super.lifecycleDisable();
            segment.setEnabled(false);
        }

        @Override
        public void run() {
            Log.i(getClass().getSimpleName(), "begin");
            while (!lifecycleDone()) {
                try {
                    for (int ii = 0; ii < spinner.length; ++ii) {
                        segment.display(spinner[ii]);
                        pause(100);
                    }
                } catch (IOException e) {
                    // Do nothing.
                }
            }
            Log.i(getClass().getSimpleName(), "end");
        }

    }

    protected class Strip extends Lifecycle {

        protected Apa102 strip;

        protected int[] rainbow = new int[RainbowHat.LEDSTRIP_LENGTH];

        protected void flush() throws java.io.IOException {
            for (int i = 0; i < rainbow.length; i++) {
                rainbow[i] = ~0;
            }
            strip.write(rainbow);
        }

        @Override
        public void lifecycleOpen() throws java.io.IOException {
            super.lifecycleOpen();
            strip = RainbowHat.openLedStrip();
        }

        @Override
        public void lifecycleClose() throws java.io.IOException {
            super.lifecycleClose();
            strip.close();
        }

        @Override
        public void lifecycleEnable() throws java.io.IOException {
            super.lifecycleEnable();
            strip.setBrightness(Apa102.MAX_BRIGHTNESS);
        }

        @Override
        public void lifecycleDisable() throws java.io.IOException {
            super.lifecycleDisable();
            strip.setBrightness(0);
            for (int i = 0; i < rainbow.length; i++) {
                rainbow[i] = Color.HSVToColor(0xff, new float[] { 0.0f, 1.0f, 1.0f });
            }
            strip.write(rainbow);
            flush();
        }

        @Override
        public void run() {
            Log.i(getClass().getSimpleName(), "begin");
            while (!lifecycleDone()) {
                try {
                    for (int ii = 0; ii < rainbow.length; ii++) {
                        rainbow[ii] = Color.HSVToColor(0xff, new float[]{ii * 360.0f / rainbow.length, 1.0f, 1.0f});
                    }
                    strip.write(rainbow);
                    flush();
                    pause(1000);
                    for (int ii = 0; ii < rainbow.length; ii++) {
                        rainbow[ii] = Color.HSVToColor(0xff, new float[]{(rainbow.length - 1 - ii) * 360.0f / rainbow.length, 1.0f, 1.0f});
                    }
                    strip.write(rainbow);
                    flush();
                    pause(1000);
                } catch (IOException e) {
                    // Do nothing.
                }
            }
            Log.i(getClass().getSimpleName(), "end");
        }

    }

    protected class Buzzer extends Lifecycle {

        protected  Speaker buzzer;

        @Override
        public void lifecycleOpen() throws java.io.IOException {
            super.lifecycleOpen();
            buzzer = RainbowHat.openPiezo();
        }

        @Override
        public void lifecycleClose() throws java.io.IOException {
            super.lifecycleClose();
            buzzer.close();
        }

        @Override
        public void lifecycleDisable() throws java.io.IOException {
            super.lifecycleDisable();
            buzzer.stop();
        }

        private void hotline() {
            try {
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
            } catch (IOException e) {
                // Do nothing.
            }
        }

        private void welcome() {
            try {
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
            } catch (IOException e) {
                // Do nothing.
            }
        }

        private void range() {
            try {
                for (float frequency = 20.0f; frequency <= 20000.0f; frequency *= 1.1f) {
                    Log.i(TAG, frequency + "Hz");
                    buzzer.play(frequency);
                    pause(100);
                }
                buzzer.stop();
            } catch (IOException e) {
                // Do nothing. */
            }
        }

        @Override
        public void run() {
            Log.i(getClass().getSimpleName(), "begin");
            while (true) {
                hotline();
                if (lifecycleDone()) {
                    break;
                }
                pause(1000);
                welcome();
                if (lifecycleDone()) {
                    break;
                }
                pause(1000);
                range();
                if (lifecycleDone()) {
                    break;
                }
                pause(1000);
            }
            Log.i(getClass().getSimpleName(), "end");
        }

    }

    protected Leds leds = new Leds();

    protected Sensor sensor = new Sensor();

    protected Segment segment = new Segment();

    protected Strip strip = new Strip();

    protected Buzzer buzzer = new Buzzer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            leds.lifecycleOpen();
            sensor.lifecycleOpen();
            segment.lifecycleOpen();
            strip.lifecycleOpen();
            buzzer.lifecycleOpen();

            leds.lifecycleEnable();
            sensor.lifecycleEnable();
            segment.lifecycleEnable();
            strip.lifecycleEnable();
            buzzer.lifecycleEnable();

            leds.lifecycleStart();
            sensor.lifecycleStart();
            segment.lifecycleStart();
            strip.lifecycleStart();
            buzzer.lifecycleStart();

            pause(30000);

            buzzer.lifecycleStop();
            strip.lifecycleStop();
            segment.lifecycleStop();
            sensor.lifecycleStop();
            leds.lifecycleStop();

            buzzer.lifecycleWait();
            strip.lifecycleWait();
            segment.lifecycleWait();
            sensor.lifecycleWait();
            leds.lifecycleWait();

            buzzer.lifecycleDisable();
            strip.lifecycleDisable();
            segment.lifecycleDisable();
            sensor.lifecycleDisable();
            leds.lifecycleDisable();

            buzzer.lifecycleClose();
            strip.lifecycleClose();
            segment.lifecycleClose();
            sensor.lifecycleClose();
            leds.lifecycleClose();

        } catch (IOException e) {
            Log.e(TAG, "Failed! " + e);
        }
    }
}
