package jp.co.miosys.aitec.models;

import java.io.Serializable;

/**
 * Created by Duc on 9/14/2017.
 */

public class Contact implements Serializable{
    String id;
    String name;
    int state;
    boolean isSelected;

    public Contact(String id, String name, int state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }

    public Contact(String id, String name, int state, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.isSelected = isSelected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
