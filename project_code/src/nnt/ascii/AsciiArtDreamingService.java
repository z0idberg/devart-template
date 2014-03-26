package nnt.ascii;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.service.dreams.DreamService;
import android.widget.FrameLayout;

import java.util.Random;
import java.util.Set;

public class AsciiArtDreamingService extends DreamService {
    /** Time to show each photo in milliseconds */
    private int photoChangeRate;
    /** Characters to use for ASCII artwork */
    private String chars;
    /** View drawing artwork */
    private AsciiView out;
    /** Gallery albums from which images should be retrieved, may be null */
    private Set<String> mediaStoreAlbums;
    private String[] imagesPaths;
    private Random rnd = new Random();


    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        photoChangeRate = prefs.getInt(getString(R.string.key_change_rate), 5) * 1000;
        chars = prefs.getString(getString(R.string.key_chars), "01");
        mediaStoreAlbums = prefs.getStringSet(getString(R.string.key_sources), null);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setContentView(R.layout.main);

        //Create and init view
        out = new AsciiView(this, onSurfaceChangedListener);
        out.setChars(chars);

        //Add view to layout
        FrameLayout outFrame = (FrameLayout) findViewById(R.id.out);
        outFrame.addView(out);
    }

    /**
     * Listener used to redraw picture when surface changed (ie device rotated)
     */
    private OnSurfaceChangedListener onSurfaceChangedListener = new OnSurfaceChangedListener() {
        @Override
        public void surfaceChanged() {
            handler.removeCallbacks(task);
            handler.post(task);
        }
    };

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        handler.removeCallbacks(task);
        handler.post(task);
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();
        handler.removeCallbacks(task);
    }

    private Handler handler = new Handler();

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            String path = getRandomImagePath();
            out.showImage(path);
            handler.postDelayed(task, photoChangeRate);
        }
    };

    /**
     * Concatenates set of strings to one string using delimiter ','.
     * @param src Set of strings.
     * @return Concatenated string.
     */
    private String concatenate(Set<String> src) {
        String[] strings = new String[src.size()];
        strings = src.toArray(strings);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length - 1; i++) {
            sb.append(strings[i]).append(',');
        }
        sb.append(strings[strings.length - 1]);
        return sb.toString();
    }

    private String getRandomImagePath() {
        if (imagesPaths == null || imagesPaths.length == 0) {
            String[] projection = new String[] {MediaStore.Images.Media.DATA};
            String selection = null;
            if (mediaStoreAlbums != null && !mediaStoreAlbums.isEmpty()) {
                selection = MediaStore.Images.ImageColumns.BUCKET_ID + " IN (" + concatenate(mediaStoreAlbums) + ")";
            }
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, null, "");
            imagesPaths = new String[cursor.getCount()];
            if (cursor.moveToFirst()) {
                int i = 0;
                do {
                    imagesPaths[i++] = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return imagesPaths[rnd.nextInt(imagesPaths.length)];
    }

}
