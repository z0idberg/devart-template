package nnt.ascii;

import android.content.Context;
import android.database.Cursor;
import android.graphics.*;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.samen.imgtools.ImageTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.LogRecord;

/**
 * Created by chernov on 02.03.14.
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
                Bitmap bmp = ImageTools.resizeiBitmapToFit(BitmapFactory.decodeFile(imgPath[0]), canvas.getWidth(), canvas.getHeight());
                int xOffset = (canvas.getWidth() - bmp.getWidth()) / 2;
                int yOffset = (canvas.getHeight() - bmp.getHeight()) / 2;
                ArrayList<int[]> res = getAvgPixelsList(bmp, size.x, size.y);
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
            }
            return null;
        }

        private ArrayList<int[]> getAvgPixelsList(Bitmap bmp, int stepX, int stepY) {
            int xStepsCount = bmp.getWidth() / stepX;
            int yStepsCount = bmp.getHeight() / stepY;
//            int[][] res = new int[xStepsCount * yStepsCount][3];
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

        @Override
        protected void onPostExecute(char[] ascii) {
            super.onPostExecute(ascii);
//            drawAscii(ascii);
        }
    }
}
