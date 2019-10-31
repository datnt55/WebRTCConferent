package jp.co.miosys.aitec.utils;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jp.co.miosys.aitec.models.LocationGPS;

import static jp.co.miosys.aitec.utils.Globals.ROOT_DIRECTORY;
import static jp.co.miosys.aitec.utils.Globals.mMioTempDirectory;

/**
 * Created by DatNT on 10/24/2017.
 */

public class KMLHelper {

    private Element coordinates;
    private Document doc;
    private Element document;
    private String pathName;
    private String description;


    public final static String START_CALL = "startCall";
    public final static String END_CALL = "endCall";
    public final static String SEND_IMAGE = "sendImage";
    public final static String RECEIVE_IMAGE = "receiveImage";
    public final static String START_RECORD = "start record";
    public final static String CAPTURE_IMAGE = "capture image";
    public final static String VOICE_MEMO = "voice memo";

    private File kmlFile;

    public KMLHelper() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void createGPXTrack() {
        // root elements
        Element rootElement = doc.createElement("kml");
        doc.appendChild(rootElement);
        Attr xmlns = doc.createAttribute("xmlns");
        xmlns.setValue("http://www.opengis.net/kml/2.2");
        rootElement.setAttributeNode(xmlns);

        // set dockument tag
        document = doc.createElement("Document");
        rootElement.appendChild(document);

        Element name = doc.createElement("name");
        document.appendChild(name);
        name.appendChild(doc.createTextNode("pathName"));

        Element eleDescription = doc.createElement("description");
        document.appendChild(eleDescription);
        eleDescription.appendChild(doc.createTextNode("description"));

        Element style = doc.createElement("Style");
        document.appendChild(style);
        Attr styleId = doc.createAttribute("id");
        styleId.setValue("poligon");
        style.setAttributeNode(styleId);

        Element lineStyle = doc.createElement("LineStyle");
        style.appendChild(lineStyle);

        Element lineColor = doc.createElement("color");
        lineColor.appendChild(doc.createTextNode("7f00fff"));
        lineStyle.appendChild(lineColor);

        Element lineWidth = doc.createElement("width");
        lineWidth.appendChild(doc.createTextNode("4"));
        lineStyle.appendChild(lineWidth);

        Element polyStyle = doc.createElement("PolyStyle");
        style.appendChild(polyStyle);

        Element polyColor = doc.createElement("color");
        polyColor.appendChild(doc.createTextNode("7f00ff00"));
        polyStyle.appendChild(polyColor);

        Element placeMark = doc.createElement("Placemark");
        document.appendChild(placeMark);

        Element placeName = doc.createElement("name");
        placeName.appendChild(doc.createTextNode("Ai-Tec"));
        placeMark.appendChild(placeName);

        Element placeDescription = doc.createElement("description");
        placeDescription.appendChild(doc.createTextNode(""));
        placeMark.appendChild(placeDescription);

        Element styleUrl = doc.createElement("styleUrl");
        styleUrl.appendChild(doc.createTextNode("#poligon"));
        placeMark.appendChild(styleUrl);

        Element lineString = doc.createElement("LineString");
        placeMark.appendChild(lineString);

        Element extrude = doc.createElement("extrude");
        extrude.appendChild(doc.createTextNode("1"));
        lineString.appendChild(extrude);

        Element tessellate = doc.createElement("tessellate");
        tessellate.appendChild(doc.createTextNode("1"));
        lineString.appendChild(tessellate);

        Element altitudeMode = doc.createElement("altitudeMode");
        altitudeMode.appendChild(doc.createTextNode("absolute"));
        lineString.appendChild(altitudeMode);

        coordinates = doc.createElement("coordinates");
        lineString.appendChild(coordinates);

        // add schema
        Element schema = doc.createElement("Schema");
        Attr schemaName = doc.createAttribute("name");
        schemaName.setValue("HeadType");
        schema.setAttributeNode(schemaName);
        Attr id = doc.createAttribute("id");
        id.setValue("HeadTypeId");
        schema.setAttributeNode(id);
        document.appendChild(schema);


        Element simpleField = doc.createElement("SimpleField");
        Attr type = doc.createAttribute("type");
        type.setValue("string");
        simpleField.setAttributeNode(type);
        Attr simpleFieldName = doc.createAttribute("name");
        simpleFieldName.setValue("CallType");
        simpleField.setAttributeNode(simpleFieldName);
        schema.appendChild(simpleField);
        Element displayCall = doc.createElement("displayName");
        displayCall.appendChild(doc.createTextNode("<![CDATA[<b>Call Type</b>]]>"));
        simpleField.appendChild(displayCall);

        Element simpleFieldPartner = doc.createElement("SimpleField");
        Attr typePartner = doc.createAttribute("type");
        typePartner.setValue("string");
        simpleFieldPartner.setAttributeNode(typePartner);
        Attr simplePartnerName = doc.createAttribute("name");
        simplePartnerName.setValue("Partner");
        simpleFieldPartner.setAttributeNode(simplePartnerName);
        schema.appendChild(simpleFieldPartner);

        Element displayPartner = doc.createElement("displayName");
        displayPartner.appendChild(doc.createTextNode("<![CDATA[<b>Partner</b>]]>"));
        simpleFieldPartner.appendChild(displayPartner);

        Element simpleFieldTime = doc.createElement("SimpleField");
        Attr typeTime = doc.createAttribute("type");
        typeTime.setValue("string");
        simpleFieldTime.setAttributeNode(typeTime);
        Attr simpleTimeName = doc.createAttribute("name");
        simplePartnerName.setValue("TimeStamp");
        simpleFieldTime.setAttributeNode(simpleTimeName);
        schema.appendChild(simpleFieldTime);
        Element displayTime = doc.createElement("displayName");
        displayTime.appendChild(doc.createTextNode("<![CDATA[<b>TimeStamp</b>]]>"));
        simpleFieldTime.appendChild(displayTime);
    }

    public void addMarker (String id, String url){
        Element style = doc.createElement("Style");
        document.appendChild(style);
        Attr styleId = doc.createAttribute("id");
        styleId.setValue(id);
        style.setAttributeNode(styleId);

        Element styleIcon = doc.createElement("IconStyle");
        style.appendChild(styleIcon);

        Element color = doc.createElement("color");
        color.appendChild(doc.createTextNode("ff00ff00"));
        styleIcon.appendChild(color);

        Element colorMode = doc.createElement("colorMode");
        colorMode.appendChild(doc.createTextNode("random"));
        styleIcon.appendChild(colorMode);

        Element scale = doc.createElement("scale");
        scale.appendChild(doc.createTextNode("1.1"));
        styleIcon.appendChild(scale);

        Element Icon = doc.createElement("Icon");
        Element href = doc.createElement("href");
        href.appendChild(doc.createTextNode(url));
        Icon.appendChild(href);
        styleIcon.appendChild(Icon);
    }

    public void addWayPointWithMarker(String id,String title,String partner,String callType,String url, Location location, String roomId,String roomCreated) {
        Element placeMark = doc.createElement("Placemark");
        document.appendChild(placeMark);

        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(title));
        placeMark.appendChild(name);

        if (id.equals(SEND_IMAGE)){
            Element description = doc.createElement("description");
            placeMark.appendChild(description);
            description.appendChild(doc.createTextNode("![CDATA[<img src='"+url+"' width='400' /><br/&gt;]]>"));
        }

        Element styleUrl = doc.createElement("styleUrl");
        styleUrl.appendChild(doc.createTextNode("#"+id));
        placeMark.appendChild(styleUrl);

        Element extendedData = doc.createElement("ExtendedData");
        placeMark.appendChild(extendedData);

        Element schemaData = doc.createElement("SchemaData");
        Attr schemaUrl = doc.createAttribute("schemaUrl");
        schemaUrl.setValue("#HeadTypeId");
        schemaData.setAttributeNode(schemaUrl);
        extendedData.appendChild(schemaData);

        if (id.equals(START_RECORD)){
            Element Data = doc.createElement("Data");
            Attr schemaName = doc.createAttribute("name");
            schemaName.setValue("Video");
            Data.setAttributeNode(schemaName);
            extendedData.appendChild(Data);


            Element value = doc.createElement("value");
            Data.appendChild(value);
            value.appendChild(doc.createTextNode("<![CDATA[<iframe width=\"480\" height=\"360\"\n" + "src=\""+url+"\" frameborder=\"0\" allowfullscreen></iframe><br><br>]]>"));

        }

        Element eCallType = doc.createElement("SimpleData");
        Attr callName = doc.createAttribute("name");
        callName.setValue("CallType");
        eCallType.setAttributeNode(callName);
        eCallType.appendChild(doc.createTextNode(callType));
        schemaData.appendChild(eCallType);

        Element ePartner = doc.createElement("SimpleData");
        Attr partnerName = doc.createAttribute("name");
        partnerName.setValue("Partner");
        ePartner.setAttributeNode(partnerName);
        ePartner.appendChild(doc.createTextNode(partner));
        schemaData.appendChild(ePartner);

        Element eTimeStamp = doc.createElement("SimpleData");
        Attr timeName = doc.createAttribute("name");
        timeName.setValue("TimeStamp");
        eTimeStamp.setAttributeNode(timeName);
        eTimeStamp.appendChild(doc.createTextNode(partner));
        schemaData.appendChild(eTimeStamp);

        Element point = doc.createElement("Point");
        placeMark.appendChild(point);
        Element coordinates = doc.createElement("coordinates");
        String position;
        if (location == null)
            position = "Lost GPS";
        else {
            if (id.equals(START_RECORD))
                position = location.getLongitude() + "," + location.getLatitude() + "," + location.getAltitude() + "," + CommonUtils.getCurrentLocalTimeFormat(Globals.timeZoneFormatter) + "," + roomId + "\n";
            else
                position = location.getLongitude() + "," + location.getLatitude() + "," + location.getAltitude() + "," + CommonUtils.getCurrentLocalTimeFormat(Globals.timeZoneFormatter) + "," + roomId +"," +roomCreated + "\n";
        }
        coordinates.appendChild(doc.createTextNode(position));
        point.appendChild(coordinates);
    }

    public void addWayPointWithMarker(String id,String title,String partner,String callType,String url,Location location,  DateTime start, DateTime end , String roomId,String roomCreated) {
        Element placeMark = doc.createElement("Placemark");
        document.appendChild(placeMark);

        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(title));
        placeMark.appendChild(name);

        if (id.equals(SEND_IMAGE)){
            Element description = doc.createElement("description");
            placeMark.appendChild(description);
            description.appendChild(doc.createTextNode("![CDATA[<img src='"+url+"' width='400' /><br/&gt;]]>"));
        }

        Element styleUrl = doc.createElement("styleUrl");
        styleUrl.appendChild(doc.createTextNode("#"+id));
        placeMark.appendChild(styleUrl);

        Element extendedData = doc.createElement("ExtendedData");
        placeMark.appendChild(extendedData);

        Element schemaData = doc.createElement("SchemaData");
        Attr schemaUrl = doc.createAttribute("schemaUrl");
        schemaUrl.setValue("#HeadTypeId");
        schemaData.setAttributeNode(schemaUrl);
        extendedData.appendChild(schemaData);

        Element eCallType = doc.createElement("SimpleData");
        Attr callName = doc.createAttribute("name");
        callName.setValue("CallType");
        eCallType.setAttributeNode(callName);
        eCallType.appendChild(doc.createTextNode(callType));
        schemaData.appendChild(eCallType);

        Element ePartner = doc.createElement("SimpleData");
        Attr partnerName = doc.createAttribute("name");
        partnerName.setValue("Partner");
        ePartner.setAttributeNode(partnerName);
        ePartner.appendChild(doc.createTextNode(partner));
        schemaData.appendChild(ePartner);

        Element eTimeStamp = doc.createElement("SimpleData");
        Attr timeName = doc.createAttribute("name");
        timeName.setValue("TimeStamp");
        eTimeStamp.setAttributeNode(timeName);
        eTimeStamp.appendChild(doc.createTextNode(partner));
        schemaData.appendChild(eTimeStamp);

        Element point = doc.createElement("Point");
        placeMark.appendChild(point);
        Element coordinates = doc.createElement("coordinates");
        String position;
        if (location == null)
            position = "Lost GPS";
        else {
            DateTimeFormatter fmt = DateTimeFormat.forPattern(Globals.timeZoneFormatter);
            position = location.getLongitude() + "," + location.getLatitude() + "," + location.getAltitude() + "," + fmt.print(start) + "," + fmt.print(end) + "," + roomId + "\n";
        }
        coordinates.appendChild(doc.createTextNode(position));
        point.appendChild(coordinates);
    }


    public void addWayPoint(Location location) {
        String a = CommonUtils.getCurrentLocalTimeFormat(Globals.timeZoneFormatter);
        String position = location.getLongitude() +","+location.getLatitude()+","+location.getAltitude()+ ","+CommonUtils.getCurrentLocalTimeFormat(Globals.timeZoneFormatter) +"\n";
        coordinates.appendChild(doc.createTextNode(position));
    }

    public synchronized boolean saveKMLFile(String path){
        try {
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));
            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);
            //Toast.makeText(context, " Save GPX file success",Toast.LENGTH_SHORT).show();
            return true;
        } catch (TransformerConfigurationException e){
            e.printStackTrace();
            return false;
        } catch (TransformerException e){
            e.printStackTrace();
            return false;
        }
    }

    public void setKMLFileName(String session){
        File file = new File(mMioTempDirectory);
        if (!file.exists())
            file.mkdir();
        DateTime dateTime = new DateTime();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH-mm-ss");
        kmlFile = new File(file, Globals.name_client +"_"+session+"_"+dtf.print(dateTime)+".kml");
    }

    public String getKMLFile(){
        return kmlFile.getAbsolutePath();
    }

    public ArrayList<LocationGPS> getTrajectory(String file){
        ArrayList<LocationGPS> gpsArrayList = new ArrayList<>();
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream is = new FileInputStream(new File(file));
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String eltName = null;

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        eltName = parser.getName();
                        if ("coordinates".equals(eltName)) {
                            String coordinate = parser.nextText();
                            String[]list = coordinate.split("[\\r\\n]+");
                            for (int i = 0 ; i < list.length; i++){
                                double lon = Double.parseDouble(list[i].split(",")[0]);
                                double lat = Double.parseDouble(list[i].split(",")[1]);
                                gpsArrayList.add(new LocationGPS(lon,lat));
                            }
                        }
                        break;
                }

                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            return gpsArrayList;
        } catch (IOException e) {
            return gpsArrayList;
        }
        return gpsArrayList;
    }
}
