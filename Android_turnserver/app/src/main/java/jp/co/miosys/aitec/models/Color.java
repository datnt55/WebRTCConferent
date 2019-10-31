package jp.co.miosys.aitec.models;

/**
 * Created by DatNT on 8/29/2017.
 */

public class Color {
    private String colorCode;
    private boolean selected;

    public Color(String colorCode, boolean selected) {
        this.colorCode = colorCode;
        this.selected = selected;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
