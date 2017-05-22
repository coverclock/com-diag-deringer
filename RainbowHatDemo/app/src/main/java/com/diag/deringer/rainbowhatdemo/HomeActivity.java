package com.diag.deringer.rainbowhatdemo;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            Gpio red = RainbowHat.openLedRed();
            red.setValue(true);

            Gpio green = RainbowHat.openLedGreen();
            green.setValue(true);

            Gpio blue = RainbowHat.openLedBlue();
            blue.setValue(true);

            Bmx280 sensor = RainbowHat.openSensor();
            sensor.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X);
            float centigrade = sensor.readTemperature();
            float fahrenheit = (centigrade * 9.0f / 5.0f) + 32.0f;

            AlphanumericDisplay segment = RainbowHat.openDisplay();
            segment.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX);
            segment.display(fahrenheit);
            segment.setEnabled(true);

            Apa102 strip = RainbowHat.openLedStrip();
            int[] rainbow = new int[RainbowHat.LEDSTRIP_LENGTH];
            strip.setBrightness(Apa102.MAX_BRIGHTNESS);
            for (int i = 0; i < rainbow.length; i++) {
                rainbow[i] = Color.HSVToColor(0xff, new float[] { i * 360.0f / rainbow.length, 1.0f, 1.0f });
            }
            strip.write(rainbow);
            for (int i = 0; i < rainbow.length; i++) {
                rainbow[i] = ~0;
            }
            strip.write(rainbow);

            Speaker buzzer = RainbowHat.openPiezo();
            buzzer.play(440);

            try { Thread.sleep(5000); } catch (InterruptedException e) { /* Do nothing. */ }

            buzzer.stop();
            buzzer.close();

            strip.setBrightness(0);
            for (int i = 0; i < rainbow.length; i++) {
                rainbow[i] = Color.HSVToColor(0xff, new float[] { 0.0f, 1.0f, 1.0f });
            }
            strip.write(rainbow);
            for (int i = 0; i < rainbow.length; i++) {
                rainbow[i] = ~0;
            }
            strip.write(rainbow);
            strip.close();

            segment.setEnabled(false);
            segment.close();

            sensor.close();

            blue.setValue(false);
            blue.close();

            green.setValue(false);
            green.close();

            red.setValue(false);
            red.close();

        } catch (IOException e) {
            Log.e(TAG, "Failed! " + e);
        }
    }
}
