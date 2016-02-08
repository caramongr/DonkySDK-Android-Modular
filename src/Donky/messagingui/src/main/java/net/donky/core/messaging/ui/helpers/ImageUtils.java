package net.donky.core.messaging.ui.helpers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Marcin Swierczek
 * 18/10/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ImageUtils {

    public static final int MESSAGE_OK = 0;
    public static final int MESSAGE_ERROR = -1;

    /**
     * Create a new {@link Intent} for capturing an image.
     *
     * @param context       The current context.
     * @param captureToFile The file to write the image to.
     * @return The image pick intent.
     */
    public static Intent getImageCaptureIntent(Context context, File captureToFile) {
        Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureToFile != null) {
            imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(captureToFile));
            captureToFile.setWritable(true, false);
        }
        return imageCaptureIntent;
    }

    /**
     * Create a new {@link Intent} for cropping an image.
     *
     * @param inputCropFile  The image file to crop.
     * @param outputCropFile The image file to crop. Need to specify a different output directory
     * @return The image crop intent.
     */
    public static Intent getImageCropIntent(final File inputCropFile, File outputCropFile) {
        if (inputCropFile == null || outputCropFile == null) {
            return null;
        }

        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(Uri.fromFile(inputCropFile), "image/*");
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputCropFile));

        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        return cropIntent;
    }

    /**
     * Create a new {@link Intent} for picking an image.
     *
     * @return The image pick intent.
     */
    public static Intent getImagePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        return intent;
    }

    /**
     * Save a bitmap picked from the image picker to the given file in a background thread.
     * The handler will be called when finished with a what of {@link ImageUtils#MESSAGE_OK},
     * or will have a what of {@link ImageUtils#MESSAGE_ERROR} on an error with the obj set to the Exception.
     *
     * @see ImageUtils#getImagePickerIntent()
     * @param contentResolver The {@link ContentResolver}.
     * @param data The data intent from the {@link Activity#onActivityResult(int, int, Intent)}.
     * @param outputFile The file to save the image to.
     * @param handler The handler to call back when finished.
     */
    public static void saveBitmapContent(final ContentResolver contentResolver, final Intent data, final File outputFile, final Handler handler) {
        saveBitmapContent(contentResolver, data.getData(), outputFile, handler);
    }

    /**
     * Save a bitmap picked from the image picker to the given file in a background thread.
     * The handler will be called when finished with a what of {@link ImageUtils#MESSAGE_OK},
     * or will have a what of {@link ImageUtils#MESSAGE_ERROR} on an error with the obj set to the Exception.
     *
     * @see ImageUtils#getImagePickerIntent()
     * @param contentResolver The {@link ContentResolver}.
     * @param uri The uri from the data intent from the {@link Activity#onActivityResult(int, int, Intent)}.
     * @param outputFile The file to save the image to.
     * @param handler The handler to call back when finished.
     */
    public static void saveBitmapContent(final ContentResolver contentResolver, final Uri uri, final File outputFile, final Handler handler) {
        new Thread() {
            @Override
            public void run() {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = contentResolver.openInputStream(uri);
                    out = new BufferedOutputStream(new FileOutputStream(outputFile));
                    copy(in, out);

                    int o = getOrientation(contentResolver, uri);
                    if (o != 0) {
                        if (o < 0) {
                            o = 360 - o;
                        }
                        if (o > 0 && o < 360) {
                            int oExif = ExifInterface.ORIENTATION_ROTATE_90;
                            if (o == 180) {
                                oExif = ExifInterface.ORIENTATION_ROTATE_180;
                            }
                            else if (o == 270) {
                                oExif = ExifInterface.ORIENTATION_ROTATE_270;
                            }
                            ExifInterface exif = new ExifInterface(outputFile.getAbsolutePath());
                            exif.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(oExif));
                            exif.saveAttributes();
                        }
                    }

                    handler.sendEmptyMessage(MESSAGE_OK);
                } catch (Exception e) {
                    Message m = handler.obtainMessage(MESSAGE_ERROR, e);
                    m.sendToTarget();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {}
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {}
                    }
                }
            }
        }.start();
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len = in.read(buffer);
        while (len != -1) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
        out.flush();
        out.close();
        in.close();
    }

    /**
     * @return The orientation of an image file in the gallery.
     */
    public static int getOrientation(final ContentResolver contentResolver, final Uri uri) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);
        } catch (Throwable t) {}
        if (cursor == null || cursor.getCount() == 0) {
            return 0;
        }
        cursor.moveToFirst();
        return cursor.getInt(0);
    }


    /**
     * Load an image from the given file at a size that will equal or exceed the given bounds.
     * @param imageFile The file to load.
     * @param reqWidth The required width.
     * @param reqHeight The required height.
     * @return The bitmap loaded from the file.
     */
    public static Bitmap loadImageAtRequiredSize(final File imageFile, int reqWidth, int reqHeight) {
        return ImageUtils.loadImageAtRequiredSize(imageFile, reqWidth, reqHeight, null);
    }

    /**
     * Load an image from the given file at a size that will equal or exceed the given bounds.
     * @param imageFile The file to load.
     * @param reqWidth The required width.
     * @param reqHeight The required height.
     * @param listener The listener for throwable events
     * @return The bitmap loaded from the file.
     */
    public static Bitmap loadImageAtRequiredSize(final File imageFile, int reqWidth, int reqHeight, DonkyListener listener) {
        BitmapFactory.Options options = readImageBounds(imageFile);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            if (exif != null) {
                int exifR = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                if (exifR == ExifInterface.ORIENTATION_ROTATE_90) {
                    rotate = 90;
                } else if (exifR == ExifInterface.ORIENTATION_ROTATE_180) {
                    rotate = 180;
                } else if (exifR == ExifInterface.ORIENTATION_ROTATE_270) {
                    rotate = 270;
                }
            }
        } catch (IOException e) {
        }

        Bitmap b = null;
        int maxShrinkTries = 5;
        for (int i = 0; i < maxShrinkTries; i++) {
            try {
                // attempt to decode - may throw out of memory exception or return null with no exception
                b = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

                if (b == null) {
                    reportThrowable(new Exception("Unable to decode bitmap"), listener);
                }
                else {
                    if (rotate > 0) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(rotate);
                        b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                        if (b == null) {
                            reportThrowable(new Exception("Image post rotation failed"), listener);
                        }
                    }
                }
                break;
            } catch (OutOfMemoryError e) {
                options.inSampleSize *= 2; // Shrink the image
                if (i == maxShrinkTries -1) {
                    reportThrowable(e,listener);
                }
            } catch (Throwable t) {
                reportThrowable(t,listener);
                break;
            }
        }

        return b;
    }

    /**
     * Load an image from the given file at a size that will equal or exceed the given bounds.
     * @param reqWidth The required width.
     * @param reqHeight The required height.
     * @return The bitmap loaded from the file.
     */
    public static Bitmap loadImageAtRequiredSize(ContentResolver contentResolver, Uri uri, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = readImageBounds(contentResolver, uri);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        InputStream is;
        try {
            is = contentResolver.openInputStream(uri);
        } catch (FileNotFoundException e) {
            return null;
        }
        return BitmapFactory.decodeStream(is, null, options);
    }

    /**
     * Resize the given bitmap, if it is larger than the given width or height. Save at the given quality.
     * @param imageFile The file to resize.
     * @param maxWidth The max width.
     * @param maxHeight The max height.
     * @param jpegQuality The quality to save at.
     * @throws FileNotFoundException If the file does not exist, or we cannot decode a bitmap from it.
     */
    public static void resizeBitmap(final File imageFile, final int maxWidth, final int maxHeight, final int jpegQuality) throws FileNotFoundException {
        if (imageFile == null || !imageFile.exists()) {
            String filename = null;
            if (imageFile != null) {
                filename = imageFile.getAbsolutePath();
            }
            throw new FileNotFoundException("Filename: " + filename);
        }
        Bitmap b = loadImageAtRequiredSize(imageFile, maxWidth, maxHeight);
        if (b == null) {
            throw new FileNotFoundException("Error decoding bitmap from file: " + imageFile.getAbsolutePath());
        }
        resizeBitmapToFile(b, imageFile, maxWidth, maxHeight, jpegQuality);
    }

    /**
     * Resize the given bitmap, if it is larger than the given width or height. Save as a PNG.
     * @param imageFile The file to resize.
     * @param maxWidth The max width.
     * @param maxHeight The max height.
     * @throws FileNotFoundException If the file does not exist, or we cannot decode a bitmap from it.
     */
    public static void resizeBitmapToPNG(final File imageFile, final int maxWidth, final int maxHeight) throws FileNotFoundException {
        if (imageFile == null || !imageFile.exists()) {
            throw new FileNotFoundException();
        }
        Bitmap b = loadImageAtRequiredSize(imageFile, maxWidth, maxHeight);
        if (b == null) {
            throw new FileNotFoundException("");
        }
        resizeBitmapToFile(b, imageFile, maxWidth, maxHeight, null);
    }

    /**
     * Read the image size from the given file.
     * @param file The file to read.
     * @return The image options with width, height and mime type.
     */
    public static BitmapFactory.Options readImageBounds(final File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        return options;
    }

    /**
     * Read the image size from the given file.
     * @param uri The file to read.
     * @return The image options with width, height and mime type.
     */
    public static BitmapFactory.Options readImageBounds(ContentResolver contentResolver, Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream is;
        try {
            is = contentResolver.openInputStream(uri);
        } catch (FileNotFoundException e) {
            return options;
        }
        BitmapFactory.decodeStream(is, null, options);
        return options;
    }

    /**
     * Calculate the largest inSampleSize value that is a power of 2 and keeps both height and width larger than the requested height and width.
     * @param options The bitmap options (to get the actual height and width of the image)
     * @param reqWidth The required width.
     * @param reqHeight The required height.
     * @return The number of samples to read in.
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Get the number of pixels scaled for the current density.
     * @param resources The {@link Resources}.
     * @param dps Number of pixels in mdpi.
     * @return The number of pixels in current density.
     */
    public static int getPixelsFromDP(Resources resources, float dps) {
        // Get the screen's density scale
        final float scale = resources.getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dps * scale + 0.5f);
    }

    /**
     * Try to resize the given bitmap in memory, keeping it's aspect ratio to the given max dimensions.
     * @param b The bitmap to scale.
     * @param maxWidth The max width.
     * @param maxHeight The max height.
     * @return The resized (if required) bitmap.
     */
    public static Bitmap resizeBitmap(Bitmap b, int maxWidth, int maxHeight, boolean allowLossOfPrecision) {

        Bitmap resized = b;
        if (resized != null) {
            Point p = scaleKeepingAspectRatio(b, maxWidth, maxHeight, allowLossOfPrecision);
            if (p != null) {
                try {
                    resized = Bitmap.createScaledBitmap(b, p.x, p.y, true);
                    //b.recycle();
                } catch (OutOfMemoryError e) {
                    resized = b;
                }
            }
        }
        return resized;
    }

    private static void resizeBitmapToFile(Bitmap b, File outputFile, int maxWidth, int maxHeight, Integer jpegQuality) throws FileNotFoundException {
        Bitmap resized = b;
        Point p = scaleKeepingAspectRatio(b, maxWidth, maxHeight, false);
        if (p != null) {
            try {
                resized = Bitmap.createScaledBitmap(b, p.x, p.y, true);
                b.recycle();
            } catch (OutOfMemoryError e) {
                resized = b;
            }
        }
        FileOutputStream stream = new FileOutputStream(outputFile);
        if (jpegQuality == null) {
            resized.compress(Bitmap.CompressFormat.PNG, 100, stream);
        }
        else {
            resized.compress(Bitmap.CompressFormat.JPEG, jpegQuality, stream);
        }
    }

    private static Point scaleKeepingAspectRatio(Bitmap b, int maxWidth, int maxHeight, boolean allowLossOfDetails) {
        int oldWidth = b.getWidth();
        int oldHeight = b.getHeight();

        if ((oldWidth > maxWidth || oldHeight > maxHeight) || allowLossOfDetails) {
            Point size = new Point();
            float scaleWidth = 1f;
            float scaleHeight = 1f;
            if (oldWidth > maxWidth || allowLossOfDetails) {
                scaleWidth = ((float) maxWidth) / oldWidth;
            }
            if (oldHeight > maxHeight || allowLossOfDetails) {
                scaleHeight = ((float) maxHeight) / oldHeight;
            }

            float scale = Math.min(scaleWidth, scaleHeight);
            size.x = (int) (oldWidth * scale);
            size.y = (int) (oldHeight * scale);

            return size;
        }
        return null;
    }

    private static void reportThrowable(Throwable t, DonkyListener listener) {
        if (listener != null) {
            DonkyException donkyException = new DonkyException(t.getLocalizedMessage());
            donkyException.initCause(t);
            listener.error(donkyException, null);
        }
    }

}
