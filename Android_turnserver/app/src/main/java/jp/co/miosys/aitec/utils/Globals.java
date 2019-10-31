package jp.co.miosys.aitec.utils;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Environment;

import org.joda.time.DateTime;

import java.util.ArrayList;

import jp.co.miosys.aitec.models.LocationGPS;
import jp.co.miosys.aitec.models.Memo;

/**
 * Created by Duc on 9/5/2017.
 */

public class Globals {
//
//    // Config socket server
//    public static final String URL_HOST = "http://157.7.209.73/";
//    public static final String URL_REGISTER = URL_HOST + "api/registerCustomer";
//    public static final int REQUEST_CODE_LOCATION_PERMISSIONS = 234;
//
//    public static final String PUSH_EXTRA_LONGTITUDE = "1";
//    public static final String PUSH_EXTRA_LATLITUDE = "2";
//    public static final String PUSH_BOOLEAN_SERVICE = "3";
//    public static final String PUSH_GUEST_NAME = "4";
//
//    public static final String FUNCTION_LOGIN = "login";
//    public static final String FUNCTION_DISCOVERY = "discovery";
//    public static final String FUNCTION_CALL = "call";
//    public static final String FUNCTION_CONFERENCE_INVITATION = "conference invitation";
//    public static final String FUNCTION_CONFERENCE_CONFIRM = "conference confirm";
//    public static final String FUNCTION_ANSWER = "answer";
//    public static final String FUNCTION_END_CALL = "endCall";
//    public static final String FUNCTION_MUTE = "mute";
//    public static final String FUNCTION_DISCONECT = "disconnect";
//    public static final String FUNCTION_SEND_IMAGE_URL = "sendFile";
//    public static final String FUNCTION_SEND_NOTIFICATION = "emergency";
//    public static final String FUNCTION_OFFER_SDP = "offerSdp";
//    public static final String FUNCTION_ANSWER_SDP = "answerSdp";
//    public static final String FUNCTION_CANDIDATE = "candidate";
//    public static final String FUNCTION_CREATE_ROOM = "createRoom";
//    public static final String FUNCTION_SEND_NOTIFICATION_SUCCESS = "emergencySuccess";
//    public static final String FUNCTION_ERROR_CONNECT = "errorConnectToServer";
//    public static final String FUNCTION_IS_VIEW_IMAGE = "view";
//
//    public static final String BUNDLE_SEND_GPS = "gps";
//    public static final String BUNDLE_SEND_IMAGE = "image";
//    public static final String BUNDLE_SEND_EXIF = "exif";
//    public static final String BUNDLE_SEND_GRID = "grid";
//    public static final String BUNDLE_SEND_ANGLE = "angle";
//
//    public static final String ROOT_DIRECTORY = Environment.getExternalStoragePublicDirectory("Ai-TEC").getAbsolutePath();
//    public static final String mMioTempDirectory = Environment.getExternalStoragePublicDirectory("Ai-TEC/Temp").getAbsolutePath();
//    public static String IMAGE_DIRECTORY;
//    public static String IMAGE_CLOSED_DIRECTORY;
//    public static final String IMAGE_DONT_SEND_DIRECTORY = Environment.getExternalStoragePublicDirectory(".Ai-TEC-TEMP-DONT-SEND").getAbsolutePath();
//    public static final String MIO_DIRECTORY = Environment.getExternalStoragePublicDirectory("MioDirectory").getAbsolutePath();
//
//    public static final String patternFileName = "yyyyMMdd_HHmmssSSS";
//    public static final String patternImageName = "yyyy/MM/dd HH:mm:ss";
//    public static final String patternExifDateTime = "yyyy:MM:dd HH:mm:ss";
//
//    public static String id_guest = "";
//    public static String name_guest = "";
//    public static String id_client = "";
//    public static String name_client = "";
//    public static boolean isReceiver = false;
//    public static final int REQUEST_WRITE_STORAGE = 112;
//
//    public static int WIDTH_SCREEN;
//    public static int HEIGHT_SCREEN;
//
//    public static boolean isPatnerViewImage;
//
//    // [20180907 VMio] Add Revision for Debug APK
//    public static boolean IsDebug = true;       // [20180907 VMio] Debug Flag
//    public static String APP_VERSION = "1.12";  // [20180907 VMio] Application version
//    public static String SVN_REVISION = "109";  // [20180907 VMio] Adding SVN revision for show at Splash in Debug mode
//
//    // [20180907 VMio] Add Flag for Disable/Enable check VPN when call Multi-User
//    public static boolean IsConferenceCheckVPN = false;
//    public static String ConferenceVideoResolution = "640x360";
//    public static String ConferenceVideoFPS = "10";
//
//    //public static final String ROOT = "133.130.68.123";  //For Kanto Construction Management
//    //public static final String ROOT = "133.130.68.66";  //For Tokyo Consultant
//    //public static final String ROOT = "157.7.209.40";  //For asanuma-gumi
//    public static final String ROOT = "157.7.199.128"; //For Road Mainternant
//    //public static final String ROOT = "219.111.20.218";  // old
//    //public static final String ROOT = "57.7.141.150"; //new
//    //public static final String ROOT = "157.7.211.84"; // aicon16gb
//
//    //public static final String ROOT = "157.7.139.61"; // test
//
//    //public static final String MIO_HOST = "https://aimap.dock.miosys.vn";
//    //public static final String MIO_HOST = "https://luck2-stg.aimap.jp";
//    public static final String MIO_HOST = "https://nrm.aimap.jp"; //For Road Mainternant
//    //public static final String MIO_HOST = "https://asanuma.aimap.jp"; //For Asanuma-gumi
//    //public static final String MIO_HOST = "https://kcm.aimap.jp"; //For Kanto Construction Management
//    //public static final String MIO_HOST = "https://tokyo-con.aimap.jp"; //For Tokyo Consultant
//
//    //Signaling server
//    //public static final String SERVER_IP = "ws://157.7.141.150:9090";
//    //public static final String SERVER_IP = "ws://157.7.211.84:9090"; // 16gb
//    public static final String SERVER_IP =  "ws://" + ROOT + ":9090"; // aipickluck
//
//    // [20180907 VMio] Turn server information
//    //public static String TurnServerURI = "turn:157.7.141.150:3478";
//    //public static String TurnServerURI = "turn:157.7.211.84:3478";
//    public static String TurnServerURI = "turn:" + ROOT + ":3478"; // aipickluck
//    public static String TurnServerUser = "vmio";
//    public static String TurnServerPass = "vm69vm69";
//
//    public static ArrayList<LocationGPS> locations = new ArrayList<>();
//    public static Location currentLocation;
//    public static  KMLHelper kmlHelper;
//    public static Bitmap captureBitmap;
//
//    public static boolean initiator;
//    public static String room_id = "";
//
//    public static final String HOST = "https://" + ROOT + ":4443";
//    public static final String TOKEN_URL = HOST +"/api/tokens";
//    public static final String SAVE_RECORDING_URL = HOST +"/api/recordings/stop/";
//    public static final String START_RECORDING_URL = HOST +"/api/recordings/start";
//    public static final String GET_RECORDING_URL = HOST +"/api/recordings";
//    public static final String SESSION_URL = HOST+"/api/sessions";
//
//    public static final String URL_SEND_IMAGE = MIO_HOST +"/api/v1/chat/upload-image";
//
//    public static final String SAVE_CALL_DETAIL = MIO_HOST +"/api/v1/chat/send-message";
//    public static final String SAVE_USER = MIO_HOST +"/api/v1/user/login-username-uuid";
//    public static final String URL_KML = MIO_HOST +"/api/v1/logger/add";
//    public static final String URL_REGISTER_ROOM = MIO_HOST +"/api/v1/chat/add-room";
//
//    public static Boolean startThread =false;
//
//    public static final String BUNDLE_HOST = "host";
//    public static final String BUNDLE_CONFERENCE = "conference";
//
//    public static final String BUNDLE = "bundle";
//    public static DateTime GPSFirstTime;


    // Config socket server
    public static final String URL_HOST = "http://157.7.209.73/";
    public static final String URL_REGISTER = URL_HOST + "api/registerCustomer";
    public static final int REQUEST_CODE_LOCATION_PERMISSIONS = 234;

    public static final String PUSH_EXTRA_LONGTITUDE = "1";
    public static final String PUSH_EXTRA_LATLITUDE = "2";
    public static final String PUSH_EXTRA_MEMO = "3";
    public static final String PUSH_GUEST_NAME = "4";

    public static final String FUNCTION_LOGIN = "login";
    public static final String FUNCTION_CHANGE_STATUS = "state";
    public static final String FUNCTION_DISCOVERY = "discovery";
    public static final String FUNCTION_CALL = "call";
    public static final String FUNCTION_CONFERENCE_INVITATION = "conference invitation";
    public static final String FUNCTION_CONFERENCE_CONFIRM = "conference confirm";
    public static final String FUNCTION_ANSWER = "answer";
    public static final String FUNCTION_END_CALL = "endCall";
    public static final String FUNCTION_DISCONECT = "disconnect";
    public static final String FUNCTION_MUTE = "mute";
    public static final String FUNCTION_CHANGE_CAMERA = "change camera";
    public static final String FUNCTION_SEND_IMAGE_URL = "sendFile";
    public static final String FUNCTION_SEND_NOTIFICATION = "emergency";
    public static final String FUNCTION_OFFER_SDP = "offerSdp";
    public static final String FUNCTION_ANSWER_SDP = "answerSdp";
    public static final String FUNCTION_CANDIDATE = "candidate";
    public static final String FUNCTION_CREATE_ROOM = "createRoom";
    public static final String FUNCTION_SEND_NOTIFICATION_SUCCESS = "emergencySuccess";
    public static final String FUNCTION_ERROR_CONNECT = "errorConnectToServer";
    public static final String FUNCTION_IS_VIEW_IMAGE = "view";

    public static final String BUNDLE_SEND_GPS = "gps";
    public static final String BUNDLE_SEND_IMAGE = "image";
    public static final String BUNDLE_SEND_EXIF = "exif";
    public static final String BUNDLE_SEND_GRID = "grid";
    public static final String BUNDLE_SEND_ANGLE = "angle";

    public static final String ROOT_DIRECTORY = Environment.getExternalStoragePublicDirectory("Ai-TEC").getAbsolutePath();
    public static final String mMioTempDirectory = Environment.getExternalStoragePublicDirectory("Ai-TEC/Temp").getAbsolutePath();
    public static String IMAGE_DIRECTORY;
    public static String IMAGE_CLOSED_DIRECTORY;
    public static final String IMAGE_DONT_SEND_DIRECTORY = Environment.getExternalStoragePublicDirectory(".Ai-TEC-TEMP-DONT-SEND").getAbsolutePath();
    public static final String MIO_DIRECTORY = Environment.getExternalStoragePublicDirectory("MioDirectory").getAbsolutePath();

    public static final String patternFileName = "yyyyMMdd_HHmmssSSS";
    public static final String patternImageName = "yyyy/MM/dd HH:mm:ss";
    public static final String patternExifDateTime = "yyyy:MM:dd HH:mm:ss";
    public static final String timeZoneFormatter = "yyyy:MM:dd HH:mm:ss Z";

    public static final int ONE_TEC = 1;

    public static String id_guest = "";
    public static String name_guest = "";
    public static String id_client = "";
    public static String name_client = "";
    public static boolean isReceiver = false;
    public static final int REQUEST_WRITE_STORAGE = 112;
    public static final int REQUEST_API_101 = 101;
    public static final int REQUEST_API_102 = 102;

    public static int WIDTH_SCREEN;
    public static int HEIGHT_SCREEN;

    public static boolean isPatnerViewImage;

    // [20180907 VMio] Add Revision for Debug APK
    public static boolean IsDebug = true;       // [20180907 VMio] Debug Flag
    public static String APP_VERSION = "1.1.2";  // [20180907 VMio] Application version
    public static String SVN_REVISION = "110";  // [20180907 VMio] Adding SVN revision for show at Splash in Debug mode

    // [20180907 VMio] Add Flag for Disable/Enable check VPN when call Multi-User
    public static boolean IsConferenceCheckVPN = false;
    // public static String ConferenceVideoResolution = "640x480";
    public static String ConferenceVideoResolution = "1920x1080"; // [20190910 Stmfko] Change max resolution
    public static String ConferenceVideoFPS = "24";

    public static final String CHOOSE_COMPANY = "https://luck-manager.aimap.jp/api/v1/user/company-info-by-code";
    //public static final String CHOOSE_COMPANY = "https://hq-asanuma.man.aimap.jp/api/v1/user/company-info-by-code";

    //public static final String KMS_ROOT = "133.130.68.123";  //For Kanto Construction Management
    //public static final String KMS_ROOT = "133.130.68.66";  //For Tokyo Consultant
    public static String KMS_ROOT;
    public static String MIO_HOST;
//    public static String KMS_ROOT = "157.7.209.40";  //For asanuma-gumi
    //public static final String KMS_ROOT = "157.7.199.128"; //For Road Mainternant
    //public static final String KMS_ROOT = "219.111.20.218";  // old
    //public static final String KMS_ROOT = "57.7.141.150"; //new
    //public static final String KMS_ROOT = "157.7.211.84"; // aicon16gb

    //public static final String MIO_HOST = "https://aimap.dock.miosys.vn";
    //public static final String MIO_HOST = "https://luck2-stg.aimap.jp";
    //public static final String MIO_HOST = "https://nrm.aimap.jp"; //For Road Mainternant
//    public static String MIO_HOST = "https://asanuma.aimap.jp"; //For Asanuma-gumi
    //public static final String MIO_HOST = "https://kcm.aimap.jp"; //For Kanto Construction Management
    //public static final String MIO_HOST = "https://tokyo-con.aimap.jp"; //For Tokyo Consultant

    //Signaling server
    //public static final String SERVER_IP = "ws://157.7.141.150:9090";
    //public static final String SERVER_IP = "ws://157.7.211.84:9090"; // 16gb
    public static String SERVER_IP = "ws://" + KMS_ROOT + ":9090"; // aipickluck

    // [20180907 VMio] Turn server information
    //public static String TurnServerURI = "turn:157.7.141.150:3478";
    //public static String TurnServerURI = "turn:157.7.211.84:3478";
    public static String TurnServerURI = "turn:" + KMS_ROOT + ":3478"; // aipickluck
    public static String TurnServerUser = "vmio";
    public static String TurnServerPass = "vm69vm69";

    public static ArrayList<LocationGPS> locations = new ArrayList<>();
    public static ArrayList<Memo> arrayMemo = new ArrayList<>();
    public static Location currentLocation;
    public static  KMLHelper kmlHelper;
    public static Bitmap captureBitmap;

    public static boolean initiator;
    public static String room_id = "";

    public static String HOST = "https://" + KMS_ROOT + ":4443";
    public static String TOKEN_URL = HOST +"/api/tokens";
    public static String SAVE_RECORDING_URL = HOST +"/api/recordings/stop/";
    public static String START_RECORDING_URL = HOST +"/api/recordings/start";
    public static String GET_RECORDING_URL = HOST +"/api/recordings";
    public static String SESSION_URL = HOST+"/api/sessions";
    public static String MEMO_UPLOAD = MIO_HOST+"/api/v1/chat/send-voice-memo";

    public static String URL_SEND_IMAGE = MIO_HOST +"/api/v1/chat/upload-image";

    public static String SAVE_CALL_DETAIL = MIO_HOST +"/api/v1/chat/send-message";
    public static String SAVE_ONE_TEC = MIO_HOST +"/api/v1/chat/send-onetec";
    public static String SAVE_USER = MIO_HOST +"/api/v1/user/login-username-uuid";
    public static String URL_KML = MIO_HOST +"/api/v1/logger/add";
    public static String URL_REGISTER_ROOM = MIO_HOST +"/api/v1/chat/add-room";
    public static String URL_LIST_MEMO = MIO_HOST +"/api/v1/memo/list";
    public static String URL_MEMO_CATEGORIES = "https://ai-con-stg.aimap.jp/api/v1/voice-memo-categories/list";

    public static Boolean startThread =false;

    public static final String BUNDLE_HOST = "host";
    public static final String BUNDLE_CONFERENCE = "conference";

    public static final String BUNDLE = "bundle";
    public static final String BUNDLE_TITLE = "bundle title";
    public static DateTime GPSFirstTime;

    public static final int DISTANCE = 2; // 2 kilometer

}
