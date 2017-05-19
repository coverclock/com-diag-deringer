package com.diag.deringer.androidthingsperipheralio;

// https://codelabs.developers.google.com/codelabs/androidthings-peripherals/#0
// mailto:coverclock@diag.com

import java.io.IOException;
import android.app.Activity;
import android.os.Bundle;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
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

    // GPIO connection to button input.
    private Gpio mButtonGpio;
    // GPIO connection to LED output.
    private Gpio mLedGpio;

    private GpioCallback mCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                boolean buttonValue = gpio.getValue();
                Log.i(TAG, "GPIO changed, button " + buttonValue);
                mLedGpio.setValue(buttonValue);
            } catch (IOException e) {
                Log.w(TAG, "Error reading GPIO");
            }

            // Return true to keep callback active.
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManagerService service = new PeripheralManagerService();
        Log.d(TAG, "Available GPIO: " + service.getGpioList());

        try {
            // Create GPIO connections.
            mButtonGpio = service.openGpio(BUTTON_A);
            mLedGpio = service.openGpio(LED_RED);

            // Configure as an input, trigger events on every change.
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            // Value is true with the pin is LOW.
            mButtonGpio.setActiveType(Gpio.ACTIVE_LOW);

            // Configure as an output.
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            // Register the event callback.
            mButtonGpio.registerGpioCallback(mCallback);
        } catch (IOException e) {
            Log.w(TAG, "Error opening GPIO", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close the button.
        if (mButtonGpio != null) {
            mButtonGpio.unregisterGpioCallback(mCallback);
            try {
                mButtonGpio.close();
            } catch (IOException e) {
                Log.w(TAG,"Error closing GPIO", e);
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
}
