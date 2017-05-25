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

            // Open.

            Gpio red = RainbowHat.openLedRed();

            Gpio green = RainbowHat.openLedGreen();

            Gpio blue = RainbowHat.openLedBlue();

            Bmx280 sensor = RainbowHat.openSensor();

            AlphanumericDisplay segment = RainbowHat.openDisplay();

            Apa102 strip = RainbowHat.openLedStrip();

            Speaker buzzer = RainbowHat.openPiezo();

            // Initialize.

            sensor.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X);
            sensor.setPressureOversampling(Bmx280.OVERSAMPLING_1X);

            segment.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX);
            segment.setEnabled(true);

            // Apply.

            red.setValue(true);

            green.setValue(true);

            blue.setValue(true);

            float[] readings = sensor.readTemperatureAndPressure();
            float centigrade = readings[0];
            float fahrenheit = (centigrade * 9.0f / 5.0f) + 32.0f;
            float hectopascals = readings[1];
            float inches = hectopascals * 0.02953f;

            Log.i(TAG, centigrade + "C " + fahrenheit + "F " + hectopascals + "hPa " + inches + "in");

            segment.display(fahrenheit);

            int[] rainbow = new int[RainbowHat.LEDSTRIP_LENGTH];
            strip.setBrightness(Apa102.MAX_BRIGHTNESS);
            for (int ii = 0; ii < rainbow.length; ii++) {
                rainbow[ii] = Color.HSVToColor(0xff, new float[] { ii * 360.0f / rainbow.length, 1.0f, 1.0f });
            }
            strip.write(rainbow);
            for (int i = 0; i < rainbow.length; i++) {
                rainbow[i] = ~0;
            }
            strip.write(rainbow);

            for (float frequency = 20.0f; frequency <= 20000.0f; frequency *= 1.1f) {
                Log.i(TAG, frequency + "Hz");
                buzzer.play(frequency);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    /* Do nothing. */
                }
            }

            // Shutdown.

            buzzer.stop();

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

            // Close.

            buzzer.close();

            strip.close();

            segment.close();

            sensor.close();

            blue.close();

            green.close();

            red.close();

        } catch (IOException e) {
            Log.e(TAG, "Failed! " + e);
        }
    }
}
