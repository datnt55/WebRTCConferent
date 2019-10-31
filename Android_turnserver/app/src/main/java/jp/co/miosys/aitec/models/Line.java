package jp.co.miosys.aitec.models;

/**
 * Created by DatNT on 8/29/2017.
 */

public class Line {
    private int id;
    private boolean selected;

    public Line(int id, boolean selected) {
        this.id = id;
        this.selected = selected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
