package nnt.ascii;

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
    private SurfaceHolder holder;
    boolean holderReady;


    public AsciiView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
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

        //TODO restart draw
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        holderReady = false;
    }

    private class MakeAsciiTask extends AsyncTask<String, Void, char[]> {
        @Override
        protected char[] doInBackground(String... imgPath) {
            Point size = new Point(8, 11);

            try {
                Canvas canvas = holder.lockCanvas();
                Log.i("AsciiArtDream", "canvas: " + canvas.getWidth() + ", " + canvas.getHeight());
                Bitmap bmp1 = getImageBitmap(imgPath[0], canvas.getWidth(), canvas.getHeight());
                //TODO: scale bitmap to screen size or larger
                //TODO: scale bitmap to screen size or larger
                int xOffset = (canvas.getWidth() - bmp1.getWidth()) / 2;
                int yOffset = (canvas.getHeight() - bmp1.getHeight()) / 2;
                ArrayList<int[]> res = getAvgPixelsList(bmp1, size.x, size.y);
                Paint bgPaint = new Paint();
                bgPaint.setColor(Color.BLACK);
                Paint txtPaint = new Paint();
                txtPaint.setTypeface(Typeface.MONOSPACE);
                txtPaint.setTextSize(11);
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), bgPaint);
                for (int i = 0; i < res.size(); i++) {
                    txtPaint.setColor(res.get(i)[2]);
                    canvas.drawText(getNextChar(), xOffset + res.get(i)[0], yOffset + res.get(i)[1], txtPaint);
                }
                holder.unlockCanvasAndPost(canvas);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("AsciiArtDream", "Unable to load image", e);
            }
            return null;
        }

        private Bitmap getImageBitmap(String imagePath, int width, int height) {
            Bitmap outBmp = null;
            InputStream in = null;
            try {
                in = openInputStream(imagePath);

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

                Log.i("AsciiArtDream", "Scaled from " + origWidth + ", " + origHeight + " to "  + outBmp.getWidth() + ", " + outBmp.getHeight());
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
                while ((halfWidth / inSampleSize) > reqWidth && (halfHeight / inSampleSize) > reqHeight) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }

        private ArrayList<int[]> getAvgPixelsList(Bitmap bmp, int stepX, int stepY) {
            int xStepsCount = bmp.getWidth() / stepX;
            int yStepsCount = bmp.getHeight() / stepY;
            ArrayList<int[]> res = new ArrayList<>(xStepsCount * yStepsCount);
            for (int y = 0; y < yStepsCount; y++) {
                for (int x = 0; x < xStepsCount; x++) {
                    res.add(new int[] {x * stepX, y * stepY, bmp.getPixel(x * stepX, y * stepY)});
                }
            }
            return res;
        }

        String[] chars = new String[] {"B","X","0","Z","A","8","V","D","5","Y","E","S","4","K","W","R"};
        Random rnd = new Random();
        private String getNextChar() {
            return chars[rnd.nextInt(chars.length)];
        }
    }
}
