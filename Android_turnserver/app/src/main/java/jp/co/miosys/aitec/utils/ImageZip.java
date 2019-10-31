package jp.co.miosys.aitec.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ImageZip
{
    static String TAG = "ImageZip";

    public static boolean mergeFiles(String imageFile, String kmlFile, String outputImageFile)
    {
        File[] files = new File[2];
        String zipOfKMLFile = changeFileExt(kmlFile,"zip");
        File mergedFile = new File(outputImageFile);

        // Zip kml file
        doZip(kmlFile, zipOfKMLFile);

        files[0] = new File(imageFile);
        files[1] = new File(zipOfKMLFile);

        FileOutputStream fstream = null;
        BufferedWriter out = null;
        try {
            fstream = new FileOutputStream(mergedFile, false);
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        }

        // Merge Jpg with Zip files
        for (File f : files) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(f);

                byte[] buffer = new byte[1024];
                int read = 0;
                while ((read = fis.read( buffer, 0, 1024)) > 0) {
                    fstream.write(buffer, 0, read);
                }
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        // close merged file
        try {
            fstream.close();
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Get bitmap file from concatinated file
    public static Bitmap getBitmap(String concatImageFile, String outputJpgFile)
    {
        try {
            Bitmap bm = BitmapFactory.decodeFile(concatImageFile);
            FileOutputStream fileOutputStream = new FileOutputStream(outputJpgFile);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            bos.flush();
            bos.close();
            return bm;
        } catch (IOException e) {
            Log.e(TAG, "Error saving image file: " + e.getMessage());
            return null;
        }
    }

    // Get KML file from concatinated file
    public static void getKML(String concatImageFile)
    {
        try {
            String zipFile = changeFileExt(concatImageFile,"zip");
            fixInvalidZipFile(new File(concatImageFile));
            extractZipData(concatImageFile, zipFile);
            unZip(zipFile, "kml");
        }
        catch (IOException ex)
        {
            Log.w(TAG, "Error get KML file: " + ex.getMessage());
        }
    }

    public static void unZip(String zipFile, String ext) {
        try  {
            FileInputStream fin = new FileInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            String kmlFile = changeFileExt(zipFile, "kml");
            while ((ze = zin.getNextEntry()) != null) {
                FileOutputStream fout = new FileOutputStream(kmlFile, false);
                byte[] buffer = new byte[1024];
                int read = 0;
                while ((read = zin.read(buffer)) != -1) {
                    fout.write(buffer, 0, read);
                }
                fout.close();
                zin.closeEntry();
                fout.close();
            }
            zin.close();
        } catch(Exception e) {
            Log.e(TAG, "unzip", e);
        }
    }

    public static boolean doZip(String sFile, String sZipFile)
    {
        File file = new File(sFile), zipFile = new File(sZipFile);
        ZipOutputStream zipOutputStream = null;
        try {
            FileOutputStream fout = new FileOutputStream(zipFile, false);
            zipOutputStream = new ZipOutputStream(fout);
            byte[] buf = new byte[1024];
            ZipEntry zipEntry;
            zipEntry = new ZipEntry(file.getName());//file.toURI().relativize(file.toURI()).getPath());
            zipOutputStream.putNextEntry(zipEntry);
            if (!file.isDirectory()) {
                FileInputStream fstream = new FileInputStream(file);
                int readLength;
                while ((readLength = fstream.read(buf, 0, 1024)) != -1) {
                    zipOutputStream.write(buf, 0, readLength);
                }
                fstream.close();
            }
            zipOutputStream.close();
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static String changeFileExt(String inputFile, String ext)
    {
        return inputFile.substring(0, inputFile.lastIndexOf(".") + 1) + ext;
    }

        //https://stackoverflow.com/questions/11039079/cannot-extract-file-from-zip-archive-created-on-android-device-os-specific
    private static final int LFH_SIGNATURE = 0x04034b50;
    private static final int DD_SIGNATURE = 0x08074b50;
    private static final int CDE_SIGNATURE = 0x02014b50;
    private static final int EOCD_SIGNATURE = 0x06054b50;

    private static void extractZipData(String inFile, String outFile) throws IOException {
        File zip = new File(inFile);

        long file_len = zip.length();
        RandomAccessFile r = new RandomAccessFile(zip, "rw");
        long eocd_offset = findEOCDRecord(r, LFH_SIGNATURE);
        try {
            FileOutputStream fout = new FileOutputStream(new File(outFile), false);

            if (eocd_offset > 0 && eocd_offset < file_len) {
                //fout.write(LFH_SIGNATURE);
                r.seek(eocd_offset);  // offset of first CDE in EOCD
                byte[] buffer = new byte[1024];
                int read = 0;
                while ((read = r.read(buffer, 0, 1024)) > 0) {
                    fout.write(buffer, 0, read);
                }
            }
            fout.close();
        }
        finally
        {
            r.close();
        }
    }
    private static void fixInvalidZipFile(File zip) throws IOException
    {
        RandomAccessFile r = new RandomAccessFile(zip, "rw");
        try
        {
            long eocd_offset = findEOCDRecord(r, EOCD_SIGNATURE);

            if (eocd_offset > 0)
            {
                r.seek(eocd_offset + 16);  // offset of first CDE in EOCD
                long cde_offset = readInt(r);  // read offset of first Central Directory Entry
                long lfh_offset = 0;
                long fskip, dskip;

                while (true)
                {
                    r.seek(cde_offset);
                    if (readInt(r) != CDE_SIGNATURE)  // got off sync!
                        return;

                    r.seek(cde_offset + 20);  // compressed file size offset
                    fskip = readInt(r);

                    // fix the header
                    //
                    r.seek(lfh_offset + 7);
                    short localFlagsHi = r.readByte();  // hi-order byte of local header flags (general purpose)
                    r.seek(cde_offset + 9);
                    short realFlagsHi = r.readByte();  // hi-order byte of central directory flags (general purpose)
                    if (localFlagsHi != realFlagsHi)
                    { // in latest versions this bug is fixed, so we're checking is bug exists.
                        r.seek(lfh_offset + 7);
                        r.write(realFlagsHi);
                    }

                    //  calculate offset of next Central Directory Entry
                    //
                    r.seek(cde_offset + 28);  // offset of variable CDE parts length in CDE
                    dskip = 46;  // length of fixed CDE part
                    dskip += readShort(r);  // file name
                    dskip += readShort(r);  // extra field
                    dskip += readShort(r);  // file comment

                    cde_offset += dskip;
                    if (cde_offset >= eocd_offset)  // finished!
                        break;

                    // calculate offset of next Local File Header
                    //
                    r.seek(lfh_offset + 26);  // offset of variable LFH parts length in LFH
                    fskip += readShort(r);  // file name
                    fskip += readShort(r);  // extra field
                    fskip += 30;  // length of fixed LFH part
                    fskip += 16;  // length of Data Descriptor (written after file data)

                    lfh_offset += fskip;
                }
            }
        }
        finally
        {
            r.close();
        }
    }

    /** Find an offset of End Of Central Directory record in file */
    private static long findEOCDRecord(RandomAccessFile f, int Signature) throws IOException
    {
        long result = f.length() - 22; // 22 is minimal EOCD record length
        while (result > 0)
        {
            f.seek(result);

            if (readInt(f) == Signature) return result;

            result--;
        }
        return -1;
    }

    /** Read a 4-byte integer from file converting endianness. */
    private static int readInt(RandomAccessFile f) throws IOException
    {
        int result = 0;
        result |= f.read();
        result |= (f.read() << 8);
        result |= (f.read() << 16);
        result |= (f.read() << 24);
        return result;
    }

    /** Read a 2-byte integer from file converting endianness. */
    private static short readShort(RandomAccessFile f) throws IOException
    {
        short result = 0;
        result |= f.read();
        result |= (f.read() << 8);
        return result;
    }
}
