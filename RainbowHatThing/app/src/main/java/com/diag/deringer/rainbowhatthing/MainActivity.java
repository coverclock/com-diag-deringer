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
            if (millis > 0) {
                Thread.sleep(millis);
            } else if (millis == 0) {
                Thread.yield();
            } else {
                // Do nothing.
            }
        } catch (InterruptedException e) {
            /* Do nothing. */
        }
    }

    protected class Lifecycle extends Thread {

        private boolean running = false;

        public Lifecycle() {
            Log.i(getClass().getSimpleName(), "constructor");
        }

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
            synchronized (this) {
                if (!running) {
                    running = true;
                    start();
                }
            }
        }

        public void lifecycleStop() {
            Log.i(getClass().getSimpleName(), "stop");
            synchronized (this) {
                running = false;
                interrupt();
            }
        }

        public void lifecycleWait() {
            Log.i(getClass().getSimpleName(), "wait");
            boolean done;

            synchronized (this) {
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

            synchronized (this) {
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
                    Log.i(getClass().getSimpleName(), centigrade + "C " + fahrenheit + "F " + hectopascals + "hPa " + inches + "in");
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

    public enum Noise { NONE, HOTLINE, WELCOME, TINNITUS }

    private static final Object noises = new Object();
    private static boolean noisy = false;

    public void p() {
        synchronized (noises) {
            while (noisy) {
                try {
                    noises.wait();
                } catch (InterruptedException e) {
                    // Do nothing.
                }
            }
            noisy = true;
        }
    }

    public void v() {
        synchronized (noises) {
            noisy = false;
            noises.notifyAll();
        }
    }

    protected class Buzzer extends Lifecycle {

        protected Speaker buzzer;
        protected Noise noise = Noise.NONE;

        public Buzzer(Noise nn) {
            noise = nn;
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
                pause(500);
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
                pause(500);
            } catch (IOException e) {
                // Do nothing.
            }
        }

        private void tinnitus() {
            try {
                for (float frequency = 20.0f; frequency <= 20000.0f; frequency *= 1.1f) {
                    Log.i(getClass().getSimpleName(), frequency + "Hz");
                    buzzer.play(frequency);
                    pause(100);
                }
                buzzer.stop();
                pause(500);
            } catch (IOException e) {
                // Do nothing.
            }
        }

        @Override
        public void run() {
            Log.i(getClass().getSimpleName(), "begin");
            while (!lifecycleDone()) {
                p();
                do {
                    if (lifecycleDone()) {
                        break;
                    }
                    try {
                        buzzer = RainbowHat.openPiezo();
                        switch (noise) {
                            case HOTLINE:
                                hotline();
                                break;
                            case WELCOME:
                                welcome();
                                break;
                            case TINNITUS:
                                tinnitus();
                                break;
                            default:
                                break;
                        }
                        buzzer.close();
                    } catch (IOException e) {
                        // Do nothing.
                    }
                    if (lifecycleDone()) {
                        break;
                    }
                } while (false);
                v();
                pause(0);
            }
            Log.i(getClass().getSimpleName(), "end");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Lifecycle[] lifecycles = new Lifecycle[] { new Leds(), new Sensor(), new Segment(), new Strip(), new Buzzer(Noise.HOTLINE), new Buzzer(Noise.WELCOME), new Buzzer(Noise.TINNITUS) };

        try {

            for (int ii = 0; ii < lifecycles.length; ++ii) {
                lifecycles[ii].lifecycleOpen();
            }

            for (int ii = 0; ii < lifecycles.length; ++ii) {
                lifecycles[ii].lifecycleEnable();
            }

            for (int ii = 0; ii < lifecycles.length; ++ii) {
                lifecycles[ii].lifecycleStart();
            }

            pause(30000);

            for (int ii = 0; ii < lifecycles.length; ++ii) {
                lifecycles[ii].lifecycleStop();
            }

            for (int ii = 0; ii < lifecycles.length; ++ii) {
                lifecycles[ii].lifecycleWait();
            }

            for (int ii = 0; ii < lifecycles.length; ++ii) {
                lifecycles[ii].lifecycleDisable();
            }

            for (int ii = 0; ii < lifecycles.length; ++ii) {
                lifecycles[ii].lifecycleClose();
            }

        } catch (IOException e) {
            Log.e(TAG, "Failed! " + e);
        }
    }
}
