package in.iot.lab.bitscan.interfaces;

import android.graphics.Bitmap;



import in.iot.lab.bitscan.enums.ScanHint;

/**
 * Interface between activity and surface view
 */

public interface IScanner {



    void displayHint(ScanHint scanHint);

    void onPictureClicked(Bitmap bitmap);
}
