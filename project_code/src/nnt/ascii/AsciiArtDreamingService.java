package nnt.ascii;

import android.database.Cursor;
import android.os.Handler;
import android.provider.MediaStore;
import android.service.dreams.DreamService;
import android.widget.FrameLayout;

import java.util.Random;

public class AsciiArtDreamingService extends DreamService {
    private static final int IMAGE_CHANGE_TIMEOUT = 5000;
    private AsciiView out;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setContentView(R.layout.main);

        out = new AsciiView(this);
        FrameLayout outFrame = (FrameLayout) findViewById(R.id.out);
        outFrame.addView(out);

        handler.postDelayed(task, 500);
    }

    private Handler handler = new Handler();

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            String path = getRandomImagePath();
            out.showImage(path);
            handler.postDelayed(task, IMAGE_CHANGE_TIMEOUT);
        }
    };

    private String[] imagesPaths;
    private String getRandomImagePath() {
        if (imagesPaths == null || imagesPaths.length == 0) {
            String[] projection = new String[] {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, "", null, "");
            imagesPaths = new String[cursor.getCount()];
            if (cursor.moveToFirst()) {
                int i = 0;
                do {
                    imagesPaths[i++] = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        Random rnd = new Random();
        return imagesPaths[rnd.nextInt(imagesPaths.length)];
    }

}
