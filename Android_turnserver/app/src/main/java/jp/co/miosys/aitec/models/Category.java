package jp.co.miosys.aitec.models;

import java.io.Serializable;

public class Category implements Serializable {
    private String categoryId;
    private String categoryName;
    private String categoryNote;

    public Category(String categoryId, String categoryName, String categoryNote) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryNote = categoryNote;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryNote() {
        return categoryNote;
    }

    public void setCategoryNote(String categoryNote) {
        this.categoryNote = categoryNote;
    }
}
