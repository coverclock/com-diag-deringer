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
    private static final String NAME = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            /*
            Gpio red = RainbowHat.openLedRed();
            red.setValue(true);
            red.close();
            */

            Gpio green = RainbowHat.openLedGreen();
            green.setValue(true);

            Gpio blue = RainbowHat.openLedBlue();
            blue.setValue(true);

            AlphanumericDisplay segment = RainbowHat.openDisplay();
            segment.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX);
            segment.display("CHIP");
            segment.setEnabled(true);

            Speaker buzzer = RainbowHat.openPiezo();
            buzzer.play(440);

            Apa102 strip = RainbowHat.openLedStrip();
            strip.setBrightness(31);
            int[] rainbow = new int[RainbowHat.LEDSTRIP_LENGTH];
            for (int i = 0; i < rainbow.length; i++) {
                rainbow[i] = Color.HSVToColor(255, new float[] { i * 360.f / rainbow.length, 1.0f, 1.0f });
            }
            strip.write(rainbow);

            try { Thread.sleep(5000); } catch (InterruptedException e) { /* Do nothing. */ }

            strip.setBrightness(0);
            strip.close();

            buzzer.stop();
            buzzer.close();

            segment.setEnabled(false);
            segment.close();

            blue.setValue(false);
            blue.close();

            green.setValue(false);
            green.close();

            /*
            red.setValue(false);
            red.close();
            */

        } catch (IOException e) {
            Log.e(NAME, "Failed! " + e);
        }
    }
}
