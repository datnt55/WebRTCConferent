package jp.co.miosys.aitec.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class SharePreference {

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Context activity;
    private String REG_ID = "REG_ID";
    private String USER = "USER";
    private String PASS = "PASS";
    private String COMPANY = "company";
    private String COMPANY_NAME = "company name";
    private String VIBRATE = "vibrate";
    private String VOLUME = "volume";
    // constructor
    public SharePreference(Context activity) {
        this.activity = activity;
    }

    public void saveLogIn(String user, String pass) {
        sp = PreferenceManager.getDefaultSharedPreferences(activity);
        editor = sp.edit();
        editor.putString(USER, user);
        editor.putString(PASS, pass);
        editor.commit();
    }

    public String[] getLogin() {
        sp = PreferenceManager.getDefaultSharedPreferences(activity);
        String user = sp.getString(USER, "");
        String pass = sp.getString(PASS, "");
        String[] result = {user, pass};
        return result;
    }

    public String getCompany() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        String address =  sp.getString(COMPANY, "");
        return address;
    }

    public void saveCompany(String id) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(COMPANY, id);
        editor.apply();
    }

    public String getCompanyName() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        String address =  sp.getString(COMPANY_NAME, "");
        return address;
    }

    public void saveCompanyName(String id) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(COMPANY_NAME, id);
        editor.apply();
    }

    public boolean getVibrate() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean vibrate =  sp.getBoolean(VIBRATE, true);
        return vibrate;
    }

    public void saveVibrate(boolean vibrate) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(VIBRATE, vibrate);
        editor.apply();
    }

    public int getVolume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        int volume =  sp.getInt(VOLUME, -1);
        return volume;
    }

    public void saveVolume(int volume) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(VOLUME, volume);
        editor.apply();
    }
}
