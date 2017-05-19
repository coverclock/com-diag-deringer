package com.diag.deringer.androidthingsperipheraldriver;

// https://codelabs.developers.google.com/codelabs/androidthings-peripherals/#0
// mailto:coverclock@diag.com
// https://github.com/coverclock/com-diag-deringer

import java.io.IOException;
import android.app.Activity;
import android.os.Bundle;
import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Gpio;
import android.view.KeyEvent;
import android.util.Log;

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
    private static final String BUTTON_A = "BCM21";
    private static final String BUTTON_B = "BCM20";
    private static final String BUTTON_C = "BCM16";
    private static final String LED_RED = "BCM6";

    // Driver for the GPIO button.
    private ButtonInputDriver mButtonInputDriver;
    // GPIO connection to LED output.
    private Gpio mLedGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManagerService service = new PeripheralManagerService();
        Log.d(TAG, "Available GPIO: " + service.getGpioList());

        try {
            // Create GPIO connections.
            mLedGpio = service.openGpio(LED_RED);

            // Configure as an output.
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.w(TAG, "Error opening GPIO", e);
        }

        try {
            // Initialize button driver to emit SPACE key events.
            mButtonInputDriver = new ButtonInputDriver(
                    BUTTON_A,
                    Button.LogicState.PRESSED_WHEN_LOW,
                    KeyEvent.KEYCODE_SPACE);
            // Register with the framework.
            mButtonInputDriver.register();
        } catch (IOException e) {
            Log.e(TAG, "Error opening button.driver", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister the driver and close.
        if (mButtonInputDriver != null) {
            mButtonInputDriver.unregister();
            try {
                mButtonInputDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Button driver", e);
            }
        }

        //Close the LED.
        if (mLedGpio != null) {
            try {
                mLedGpio.close();
            } catch (IOException e) {
                Log.e(TAG,"Error closing GPIO", e);
            }
        }
    }

    /**
     * Update the value of the LED output.
     */
    private void setLedValue(boolean value) {
        try {
            mLedGpio.setValue(value);
        } catch (IOException e) {
            Log.e(TAG, "Error updating GPIO value", e);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            // Turn on the LED.
            setLedValue(true);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            // Turn off the LED.
            setLedValue(false);
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }
}
