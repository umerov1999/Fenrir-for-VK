/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.integration.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.android.Intents;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sean Owen
 * @author Fred Lin
 * @author Isaac Potoczny-Jones
 * @author Brad Drehmer
 * @author gcstang
 */
@SuppressWarnings("unused")
public class IntentIntegrator {

    // Product Codes
    public static final String UPC_A = "UPC_A";


    // supported barcode formats
    public static final String UPC_E = "UPC_E";
    public static final String EAN_8 = "EAN_8";
    public static final String EAN_13 = "EAN_13";
    public static final String RSS_14 = "RSS_14";
    // Other 1D
    public static final String CODE_39 = "CODE_39";
    public static final String CODE_93 = "CODE_93";
    public static final String CODE_128 = "CODE_128";
    public static final String ITF = "ITF";
    public static final String RSS_EXPANDED = "RSS_EXPANDED";
    // 2D
    public static final String QR_CODE = "QR_CODE";
    public static final String DATA_MATRIX = "DATA_MATRIX";
    public static final String PDF_417 = "PDF_417";
    public static final Collection<String> PRODUCT_CODE_TYPES = list(UPC_A, UPC_E, EAN_8, EAN_13, RSS_14);
    public static final Collection<String> ONE_D_CODE_TYPES =
            list(UPC_A, UPC_E, EAN_8, EAN_13, RSS_14, CODE_39, CODE_93, CODE_128,
                    ITF, RSS_14, RSS_EXPANDED);
    public static final Collection<String> ALL_CODE_TYPES = null;
    private static final String TAG = IntentIntegrator.class.getSimpleName();
    private final Map<String, Object> moreExtras = new HashMap<>(3);
    private final Context context;
    private Collection<String> desiredBarcodeFormats;
    private Class<?> captureActivity;

    public IntentIntegrator(Context context) {
        this.context = context;
    }

    /**
     * Parse activity result, without checking the request code.
     *
     * @param result result from {@code onActivityResult()}
     * @return an {@link IntentResult} containing the result of the scan. If the user cancelled scanning,
     * the fields will be null.
     */
    public static IntentResult parseActivityResult(@NonNull ActivityResult result) {
        Intent intent = result.getData();
        if (result.getResultCode() == Activity.RESULT_OK) {
            String contents = intent.getStringExtra(Intents.Scan.RESULT);
            String formatName = intent.getStringExtra(Intents.Scan.RESULT_FORMAT);
            byte[] rawBytes = intent.getByteArrayExtra(Intents.Scan.RESULT_BYTES);
            int intentOrientation = intent.getIntExtra(Intents.Scan.RESULT_ORIENTATION, Integer.MIN_VALUE);
            Integer orientation = intentOrientation == Integer.MIN_VALUE ? null : intentOrientation;
            String errorCorrectionLevel = intent.getStringExtra(Intents.Scan.RESULT_ERROR_CORRECTION_LEVEL);
            String barcodeImagePath = intent.getStringExtra(Intents.Scan.RESULT_BARCODE_IMAGE_PATH);
            return new IntentResult(contents,
                    formatName,
                    rawBytes,
                    orientation,
                    errorCorrectionLevel,
                    barcodeImagePath,
                    intent);
        }
        return new IntentResult(intent);
    }

    public static String decodeFromBitmap(@Nullable Bitmap gen) {
        if (gen == null) {
            return "error";
        }
        Bitmap generatedQRCode = gen;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && generatedQRCode.getConfig() == Bitmap.Config.HARDWARE) {
            generatedQRCode = generatedQRCode.copy(Bitmap.Config.ARGB_8888, true);
            if (generatedQRCode == null) {
                return "error";
            }
        }
        int width = generatedQRCode.getWidth();
        int height = generatedQRCode.getHeight();
        int[] pixels = new int[width * height];
        generatedQRCode.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new MultiFormatReader();
        Result result;
        try {
            result = reader.decode(binaryBitmap);
        } catch (NotFoundException | ChecksumException | FormatException e) {
            return e.getLocalizedMessage();
        }
        if (result == null) {
            return "error";
        }
        return result.getText();
    }

    private static List<String> list(String... values) {
        return Collections.unmodifiableList(Arrays.asList(values));
    }

    protected Class<?> getDefaultCaptureActivity() {
        return CaptureActivity.class;
    }

    public Class<?> getCaptureActivity() {
        if (captureActivity == null) {
            captureActivity = getDefaultCaptureActivity();
        }
        return captureActivity;
    }

    /**
     * Set the Activity class to use. It can be any activity, but should handle the intent extras
     * as used here.
     *
     * @param captureActivity the class
     */
    public IntentIntegrator setCaptureActivity(Class<?> captureActivity) {
        this.captureActivity = captureActivity;
        return this;
    }

    public Map<String, ?> getMoreExtras() {
        return moreExtras;
    }

    public final IntentIntegrator addExtra(String key, Object value) {
        moreExtras.put(key, value);
        return this;
    }

    /**
     * Set a prompt to display on the capture screen, instead of using the default.
     *
     * @param prompt the prompt to display
     */
    public final IntentIntegrator setPrompt(String prompt) {
        if (prompt != null) {
            addExtra(Intents.Scan.PROMPT_MESSAGE, prompt);
        }
        return this;
    }

    /**
     * By default, the orientation is locked. Set to false to not lock.
     *
     * @param locked true to lock orientation
     */
    public IntentIntegrator setOrientationLocked(boolean locked) {
        addExtra(Intents.Scan.ORIENTATION_LOCKED, locked);
        return this;
    }

    /**
     * Use the specified camera ID.
     *
     * @param cameraId camera ID of the camera to use. A negative value means "no preference".
     * @return this
     */
    public IntentIntegrator setCameraId(int cameraId) {
        if (cameraId >= 0) {
            addExtra(Intents.Scan.CAMERA_ID, cameraId);
        }
        return this;
    }

    /**
     * Set to true to enable initial torch
     *
     * @param enabled true to enable initial torch
     * @return this
     */
    public IntentIntegrator setTorchEnabled(boolean enabled) {
        addExtra(Intents.Scan.TORCH_ENABLED, enabled);
        return this;
    }

    /**
     * Set to false to disable beep on scan.
     *
     * @param enabled false to disable beep
     * @return this
     */
    public IntentIntegrator setBeepEnabled(boolean enabled) {
        addExtra(Intents.Scan.BEEP_ENABLED, enabled);
        return this;
    }

    /**
     * Set to true to enable saving the barcode image and sending its path in the result Intent.
     *
     * @param enabled true to enable barcode image
     * @return this
     */
    public IntentIntegrator setBarcodeImageEnabled(boolean enabled) {
        addExtra(Intents.Scan.BARCODE_IMAGE_ENABLED, enabled);
        return this;
    }

    /**
     * Set the desired barcode formats to scan.
     *
     * @param desiredBarcodeFormats names of {@code BarcodeFormat}s to scan for
     * @return this
     */
    public IntentIntegrator setDesiredBarcodeFormats(Collection<String> desiredBarcodeFormats) {
        this.desiredBarcodeFormats = desiredBarcodeFormats;
        return this;
    }

    /**
     * Set the desired barcode formats to scan.
     *
     * @param desiredBarcodeFormats names of {@code BarcodeFormat}s to scan for
     * @return this
     */
    public IntentIntegrator setDesiredBarcodeFormats(String... desiredBarcodeFormats) {
        this.desiredBarcodeFormats = Arrays.asList(desiredBarcodeFormats);
        return this;
    }

    /**
     * Initiates a scan for all known barcode types with the default camera.
     * And starts a timer to finish on timeout
     *
     * @return Activity.RESULT_CANCELED and true on parameter TIMEOUT.
     */
    public IntentIntegrator setTimeout(long timeout) {
        addExtra(Intents.Scan.TIMEOUT, timeout);
        return this;
    }

    /**
     * Create an scan intent with the specified options.
     *
     * @return the intent
     */
    public Intent createScanIntent() {
        Intent intentScan = new Intent(context, getCaptureActivity());
        intentScan.setAction(Intents.Scan.ACTION);

        // check which types of codes to scan for
        if (desiredBarcodeFormats != null) {
            // set the desired barcode types
            StringBuilder joinedByComma = new StringBuilder();
            for (String format : desiredBarcodeFormats) {
                if (joinedByComma.length() > 0) {
                    joinedByComma.append(',');
                }
                joinedByComma.append(format);
            }
            intentScan.putExtra(Intents.Scan.FORMATS, joinedByComma.toString());
        }

        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        attachMoreExtras(intentScan);
        return intentScan;
    }

    /**
     * Initiates a scan, only for a certain set of barcode types, given as strings corresponding
     * to their names in ZXing's {@code BarcodeFormat} class like "UPC_A". You can supply constants
     * like {@link #PRODUCT_CODE_TYPES} for example.
     *
     * @param desiredBarcodeFormats names of {@code BarcodeFormat}s to scan for
     */
    public Intent createScanIntent(Collection<String> desiredBarcodeFormats) {
        setDesiredBarcodeFormats(desiredBarcodeFormats);
        return createScanIntent();
    }

    private void attachMoreExtras(Intent intent) {
        for (Map.Entry<String, Object> entry : moreExtras.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // Kind of hacky
            if (value instanceof Integer) {
                intent.putExtra(key, (Integer) value);
            } else if (value instanceof Long) {
                intent.putExtra(key, (Long) value);
            } else if (value instanceof Boolean) {
                intent.putExtra(key, (Boolean) value);
            } else if (value instanceof Double) {
                intent.putExtra(key, (Double) value);
            } else if (value instanceof Float) {
                intent.putExtra(key, (Float) value);
            } else if (value instanceof Bundle) {
                intent.putExtra(key, (Bundle) value);
            } else if (value instanceof int[]) {
                intent.putExtra(key, (int[]) value);
            } else if (value instanceof long[]) {
                intent.putExtra(key, (long[]) value);
            } else if (value instanceof boolean[]) {
                intent.putExtra(key, (boolean[]) value);
            } else if (value instanceof double[]) {
                intent.putExtra(key, (double[]) value);
            } else if (value instanceof float[]) {
                intent.putExtra(key, (float[]) value);
            } else if (value instanceof String[]) {
                intent.putExtra(key, (String[]) value);
            } else {
                intent.putExtra(key, value.toString());
            }
        }
    }
}
