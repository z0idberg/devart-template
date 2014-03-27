package ru.softinvent.ascii;

import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

/**
 * View that draws ascii-styled images.
 */
public class AsciiView extends SurfaceView implements SurfaceHolder.Callback {
    private String chars;
    private SurfaceHolder holder;
    private boolean holderReady;
    private OnSurfaceChangedListener listener;
    private final Random rnd = new Random();


    public AsciiView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
    }

    public AsciiView(Context context, OnSurfaceChangedListener listener) {
        this(context);
        this.listener = listener;
    }

    public boolean showImage(String path) {
        if (holderReady) {
            new MakeAsciiTask().execute(path);
        }
        return holderReady;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        holderReady = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        if (holder.getSurface() == null) return;
        if (listener != null) {
            listener.surfaceChanged();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        holderReady = false;
    }

    public void setChars(String chars) {
        this.chars = chars;
    }

    private class MakeAsciiTask extends AsyncTask<String, Void, char[]> {
        @Override
        protected char[] doInBackground(String... imgPath) {
            Point size = new Point(9, 12);

            try {
                Canvas canvas = holder.lockCanvas();
                int canvasWidth = canvas.getWidth();
                int canvasHeight = canvas.getHeight();
                Bitmap bmp = getImageBitmap(imgPath[0], canvasWidth, canvasHeight);
                bmp = resize(bmp, canvasWidth, canvasHeight);
                bmp = crop(bmp, canvasWidth, canvasHeight);
                ArrayList<int[]> res = getAvgPixelsList(bmp, size.x, size.y);
                Paint bgPaint = new Paint();
                bgPaint.setColor(Color.BLACK);
                Paint txtPaint = new Paint();
                txtPaint.setTypeface(Typeface.MONOSPACE);
                txtPaint.setTextSize(12);
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), bgPaint);
                int charsCount = res.size();
                for (int i = 0; i < charsCount; i++) {
                    txtPaint.setColor(res.get(i)[2]);
                    canvas.drawText(getNextChar(), res.get(i)[0], res.get(i)[1], txtPaint);
                }
                holder.unlockCanvasAndPost(canvas);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("AsciiArtDream", "Unable to load image", e);
            }
            return null;
        }

        /**
         * Loads bitmap from uri (content or file) and does downsampling if needed to match requred width and height.
         *
         * @param imagePath Image URI or file path.
         * @param width Required width.
         * @param height Required height.
         * @return Loaded bitmap or null in case of loading error.
         */
        private Bitmap getImageBitmap(String imagePath, int width, int height) {
            Bitmap outBmp = null;
            try {
                InputStream in = openInputStream(imagePath);

                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, opts);
                in.close();

                boolean rotateImg = (width > height && opts.outWidth < opts.outHeight)
                        || (width < height && opts.outWidth > opts.outHeight);
                int origWidth;
                int origHeight;
                if (rotateImg) {
                    origWidth = opts.outHeight;
                    origHeight = opts.outWidth;
                } else {
                    origWidth = opts.outWidth;
                    origHeight = opts.outHeight;
                }

                in = openInputStream(imagePath);
                opts = new BitmapFactory.Options();
                opts.inSampleSize = calculateInSampleSize(origWidth, origHeight, width, height);
                outBmp = BitmapFactory.decodeStream(in, null, opts);
                in.close();

                if (rotateImg) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap decodedBitmap = outBmp;
                    outBmp = Bitmap.createBitmap(decodedBitmap, 0, 0,
                            outBmp.getWidth(), outBmp.getHeight(), matrix, true);
                    if (decodedBitmap != null && !decodedBitmap.equals(outBmp) && !decodedBitmap.isRecycled()) {
                        decodedBitmap.recycle();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return outBmp;
        }

        private InputStream openInputStream(String path) throws FileNotFoundException {
            Uri uri = Uri.parse(path);
            return "content".equals(uri.getScheme())
                    ? getContext().getContentResolver().openInputStream(uri)
                    : new FileInputStream(path);
        }

        private int calculateInSampleSize(int origWidth, int origHeight, int reqWidth, int reqHeight) {
            int inSampleSize = 1;
            if (origWidth > reqWidth || origHeight > reqHeight) {
                int halfWidth = origWidth / 2;
                int halfHeight = origHeight / 2;
                do {
                    inSampleSize *= 2;
                } while ((halfWidth / inSampleSize) > reqWidth && (halfHeight / inSampleSize) > reqHeight);
            }
            return inSampleSize;
        }

        private Bitmap resize(Bitmap srcBmp, int width, int height) {
            if (srcBmp == null) {
                return null;
            }

            int srcWidth = srcBmp.getWidth();
            int srcHeight = srcBmp.getHeight();
            float srcRatio = (float) srcWidth / (float) srcHeight;
            float screenRatio = (float) width / (float) height;
            int resizedWidth;
            int resizedHeight;

            if (srcRatio > screenRatio) {
                resizedHeight = height;
                resizedWidth = (int) (resizedHeight * srcRatio);
            } else {
                resizedWidth = width;
                resizedHeight = (int) (resizedWidth / srcRatio);
            }

            Bitmap resizedBmp = Bitmap.createScaledBitmap(srcBmp, resizedWidth, resizedHeight, true);
            if (!srcBmp.equals(resizedBmp)) {
                srcBmp.recycle();
            }

            return resizedBmp;
        }

        private Bitmap crop(Bitmap srcBmp, int width, int height) {
            if (srcBmp == null) {
                return null;
            }
            int srcWidth = srcBmp.getWidth();
            int srcHeight = srcBmp.getHeight();
            int croppedX = (srcWidth > width) ? (srcWidth - width) / 2 : 0;
            int croppedY = (srcHeight > height) ? (srcHeight - height) / 2 : 0;

            if (croppedX == 0 && croppedY == 0) {
                return srcBmp;
            }

            Bitmap croppedBitmap;
            try {
                croppedBitmap = Bitmap.createBitmap(srcBmp, croppedX, croppedY, width, height);
            }
            catch(IllegalArgumentException e) {
                if (BuildConfig.DEBUG) Log.e("AsciiArtDream", "Unable to crop bitmap", e);
                return srcBmp;
            }
            if (!srcBmp.equals(croppedBitmap)) {
                srcBmp.recycle();
            }

            return croppedBitmap;
        }

        private ArrayList<int[]> getAvgPixelsList(Bitmap bmp, int stepX, int stepY) {
            int xStepsCount = bmp.getWidth() / stepX;
            int yStepsCount = bmp.getHeight() / stepY;
            ArrayList<int[]> res = new ArrayList<>(xStepsCount * yStepsCount);
            for (int y = 0; y < yStepsCount; y++) {
                for (int x = 0; x < xStepsCount; x++) {
                    res.add(new int[] {x * stepX, y * stepY, getAvgColor(bmp, x, y, stepX, stepY)});
                }
            }
            return res;
        }

        private int getAvgColor(Bitmap bmp, int x, int y, int stepX, int stepY) {
            int pixelsCount = stepX * stepY;
            int avgColor;
            if (pixelsCount > 0) {
                int[] pixels = new int[pixelsCount];
                bmp.getPixels(pixels, 0, stepX, x * stepX, y * stepY, stepX, stepY);
                int red = 0, green = 0, blue = 0;
                for (int pixel : pixels) {
                    red += Color.red(pixel);
                    green += Color.green(pixel);
                    blue += Color.blue(pixel);
                }
                avgColor = Color.rgb(red / pixelsCount, green / pixelsCount, blue / pixelsCount);
            } else {
                avgColor = bmp.getPixel(x, y);
            }
            return avgColor;
        }

        private String getNextChar() {
            int pos = rnd.nextInt(chars.length());
            return chars.substring(pos, pos + 1);
        }
    }
}
