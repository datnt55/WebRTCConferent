package jp.co.miosys.aitec.models;

import java.io.Serializable;

/**
 * Created by Duc on 9/8/2017.
 */

public class Image implements Serializable {

    private String imagePath;

    public Image(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
