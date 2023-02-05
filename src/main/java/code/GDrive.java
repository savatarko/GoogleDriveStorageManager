package code;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import exceptions.MyFileNotFoundException;
import exceptions.StorageAlreadyInitializedException;
import exceptions.StorageCountLimitException;
import exceptions.StorageSizeLimitException;
import spec.Configuration;
import spec.MyFile;
import spec.StorageManager;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.File;

import javax.swing.*;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

//import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

public class GDrive extends StorageManager {

    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "GDrive";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */

    private static final List<String> SCOPES =
            Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static NetHttpTransport HTTP_TRANSPORT;

    private static Drive service;

    private long currentsize;


    //gets the file from the given path
    public FileList GetFromPath(String path) throws MyFileNotFoundException
    {
        String[] split = path.split("/");
        String parent = "root";
        FileList result = null;
        try {
            if(storageLocation.length() > 0)
            {
                String[] storagesplit = storageLocation.split("/");
                for (var query : storagesplit) {
                    result = service.files().list()
                            .setQ("name ='" + query + "' and parents = '" + parent + "' and trashed = false")
                            .setPageSize(10)
                            .setFields("nextPageToken, files(id, name, createdTime, modifiedTime, size)")
                            .execute();
                    if (result.getFiles().size() == 0) {
                        //throw new MyFileNotFoundException(path);
                        return null;
                    }
                    parent = result.getFiles().get(0).getId();
                }
            }
            if(path.length() == 0)
                return result;
            for (var query : split) {
                result = service.files().list()
                        .setQ("name ='" + query + "' and parents = '" + parent + "' and trashed = false")
                        .setPageSize(10)
                        .setFields("nextPageToken, files(id, name, createdTime, modifiedTime, size, parents)")
                        .execute();
                if (result.getFiles().size() == 0) {
                    //throw new MyFileNotFoundException(path);
                    return null;
                }
                parent = result.getFiles().get(0).getId();
            }
        }
        catch (IOException e)
        {
           e.printStackTrace();
        }
        return result;
    }

    private int CountFiles(String path)
    {
        try{
        FileList parent = GetFromPath(path);
        FileList result;
            result = service.files().list()
                    .setQ("parents = '" + parent.getFiles().get(0).getId() + "' and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name, createdTime, modifiedTime, size, parents, mimeType)")
                    .execute();
            return result.size();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return -1;
    }

    private Map<String, String> extmap = new HashMap<>();

    public GDrive() {
        currentsize = 0;
        currentconfig = defaultconfig;
        extmap.put(".323", "text/h323");
        extmap.put(".3g2", "video/3gpp2");
        extmap.put(".3gp", "video/3gpp");
        extmap.put(".3gp2", "video/3gpp2");
        extmap.put(".3gpp", "video/3gpp");
        extmap.put(".7z", "application/x-7z-compressed");
        extmap.put(".aa", "audio/audible");
        extmap.put(".AAC", "audio/aac");
        extmap.put(".aaf", "application/octet-stream");
        extmap.put(".aax", "audio/vnd.audible.aax");
        extmap.put(".ac3", "audio/ac3");
        extmap.put(".aca", "application/octet-stream");
        extmap.put(".accda", "application/msaccess.addin");
        extmap.put(".accdb", "application/msaccess");
        extmap.put(".accdc", "application/msaccess.cab");
        extmap.put(".accde", "application/msaccess");
        extmap.put(".accdr", "application/msaccess.runtime");
        extmap.put(".accdt", "application/msaccess");
        extmap.put(".accdw", "application/msaccess.webapplication");
        extmap.put(".accft", "application/msaccess.ftemplate");
        extmap.put(".acx", "application/internet-property-stream");
        extmap.put(".AddIn", "text/xml");
        extmap.put(".ade", "application/msaccess");
        extmap.put(".adobebridge", "application/x-bridge-url");
        extmap.put(".adp", "application/msaccess");
        extmap.put(".ADT", "audio/vnd.dlna.adts");
        extmap.put(".ADTS", "audio/aac");
        extmap.put(".afm", "application/octet-stream");
        extmap.put(".ai", "application/postscript");
        extmap.put(".aif", "audio/x-aiff");
        extmap.put(".aifc", "audio/aiff");
        extmap.put(".aiff", "audio/aiff");
        extmap.put(".air", "application/vnd.adobe.air-application-installer-package+zip");
        extmap.put(".amc", "application/x-mpeg");
        extmap.put(".application", "application/x-ms-application");
        extmap.put(".art", "image/x-jg");
        extmap.put(".asa", "application/xml");
        extmap.put(".asax", "application/xml");
        extmap.put(".ascx", "application/xml");
        extmap.put(".asd", "application/octet-stream");
        extmap.put(".asf", "video/x-ms-asf");
        extmap.put(".ashx", "application/xml");
        extmap.put(".asi", "application/octet-stream");
        extmap.put(".asm", "text/plain");
        extmap.put(".asmx", "application/xml");
        extmap.put(".aspx", "application/xml");
        extmap.put(".asr", "video/x-ms-asf");
        extmap.put(".asx", "video/x-ms-asf");
        extmap.put(".atom", "application/atom+xml");
        extmap.put(".au", "audio/basic");
        extmap.put(".avi", "video/x-msvideo");
        extmap.put(".axs", "application/olescript");
        extmap.put(".bas", "text/plain");
        extmap.put(".bcpio", "application/x-bcpio");
        extmap.put(".bin", "application/octet-stream");
        extmap.put(".bmp", "image/bmp");
        extmap.put(".c", "text/plain");
        extmap.put(".cab", "application/octet-stream");
        extmap.put(".caf", "audio/x-caf");
        extmap.put(".calx", "application/vnd.ms-office.calx");
        extmap.put(".cat", "application/vnd.ms-pki.seccat");
        extmap.put(".cc", "text/plain");
        extmap.put(".cd", "text/plain");
        extmap.put(".cdda", "audio/aiff");
        extmap.put(".cdf", "application/x-cdf");
        extmap.put(".cer", "application/x-x509-ca-cert");
        extmap.put(".chm", "application/octet-stream");
        extmap.put(".class", "application/x-java-applet");
        extmap.put(".clp", "application/x-msclip");
        extmap.put(".cmx", "image/x-cmx");
        extmap.put(".cnf", "text/plain");
        extmap.put(".cod", "image/cis-cod");
        extmap.put(".config", "application/xml");
        extmap.put(".contact", "text/x-ms-contact");
        extmap.put(".coverage", "application/xml");
        extmap.put(".cpio", "application/x-cpio");
        extmap.put(".cpp", "text/plain");
        extmap.put(".crd", "application/x-mscardfile");
        extmap.put(".crl", "application/pkix-crl");
        extmap.put(".crt", "application/x-x509-ca-cert");
        extmap.put(".cs", "text/plain");
        extmap.put(".csdproj", "text/plain");
        extmap.put(".csh", "application/x-csh");
        extmap.put(".csproj", "text/plain");
        extmap.put(".css", "text/css");
        extmap.put(".csv", "text/csv");
        extmap.put(".cur", "application/octet-stream");
        extmap.put(".cxx", "text/plain");
        extmap.put(".dat", "application/octet-stream");
        extmap.put(".datasource", "application/xml");
        extmap.put(".dbproj", "text/plain");
        extmap.put(".dcr", "application/x-director");
        extmap.put(".def", "text/plain");
        extmap.put(".deploy", "application/octet-stream");
        extmap.put(".der", "application/x-x509-ca-cert");
        extmap.put(".dgml", "application/xml");
        extmap.put(".dib", "image/bmp");
        extmap.put(".dif", "video/x-dv");
        extmap.put(".dir", "application/x-director");
        extmap.put(".disco", "text/xml");
        extmap.put(".dll", "application/x-msdownload");
        extmap.put(".dll.config", "text/xml");
        extmap.put(".dlm", "text/dlm");
        extmap.put(".doc", "application/msword");
        extmap.put(".docm", "application/vnd.ms-word.document.macroEnabled.12");
        extmap.put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        extmap.put(".dot", "application/msword");
        extmap.put(".dotm", "application/vnd.ms-word.template.macroEnabled.12");
        extmap.put(".dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        extmap.put(".dsp", "application/octet-stream");
        extmap.put(".dsw", "text/plain");
        extmap.put(".dtd", "text/xml");
        extmap.put(".dtsConfig", "text/xml");
        extmap.put(".dv", "video/x-dv");
        extmap.put(".dvi", "application/x-dvi");
        extmap.put(".dwf", "drawing/x-dwf");
        extmap.put(".dwp", "application/octet-stream");
        extmap.put(".dxr", "application/x-director");
        extmap.put(".eml", "message/rfc822");
        extmap.put(".emz", "application/octet-stream");
        extmap.put(".eot", "application/octet-stream");
        extmap.put(".eps", "application/postscript");
        extmap.put(".etl", "application/etl");
        extmap.put(".etx", "text/x-setext");
        extmap.put(".evy", "application/envoy");
        extmap.put(".exe", "application/octet-stream");
        extmap.put(".exe.config", "text/xml");
        extmap.put(".fdf", "application/vnd.fdf");
        extmap.put(".fif", "application/fractals");
        extmap.put(".filters", "Application/xml");
        extmap.put(".fla", "application/octet-stream");
        extmap.put(".flr", "x-world/x-vrml");
        extmap.put(".flv", "video/x-flv");
        extmap.put(".fsscript", "application/fsharp-script");
        extmap.put(".fsx", "application/fsharp-script");
        extmap.put(".generictest", "application/xml");
        extmap.put(".gif", "image/gif");
        extmap.put(".group", "text/x-ms-group");
        extmap.put(".gsm", "audio/x-gsm");
        extmap.put(".gtar", "application/x-gtar");
        extmap.put(".gz", "application/x-gzip");
        extmap.put(".h", "text/plain");
        extmap.put(".hdf", "application/x-hdf");
        extmap.put(".hdml", "text/x-hdml");
        extmap.put(".hhc", "application/x-oleobject");
        extmap.put(".hhk", "application/octet-stream");
        extmap.put(".hhp", "application/octet-stream");
        extmap.put(".hlp", "application/winhlp");
        extmap.put(".hpp", "text/plain");
        extmap.put(".hqx", "application/mac-binhex40");
        extmap.put(".hta", "application/hta");
        extmap.put(".htc", "text/x-component");
        extmap.put(".htm", "text/html");
        extmap.put(".html", "text/html");
        extmap.put(".htt", "text/webviewhtml");
        extmap.put(".hxa", "application/xml");
        extmap.put(".hxc", "application/xml");
        extmap.put(".hxd", "application/octet-stream");
        extmap.put(".hxe", "application/xml");
        extmap.put(".hxf", "application/xml");
        extmap.put(".hxh", "application/octet-stream");
        extmap.put(".hxi", "application/octet-stream");
        extmap.put(".hxk", "application/xml");
        extmap.put(".hxq", "application/octet-stream");
        extmap.put(".hxr", "application/octet-stream");
        extmap.put(".hxs", "application/octet-stream");
        extmap.put(".hxt", "text/html");
        extmap.put(".hxv", "application/xml");
        extmap.put(".hxw", "application/octet-stream");
        extmap.put(".hxx", "text/plain");
        extmap.put(".i", "text/plain");
        extmap.put(".ico", "image/x-icon");
        extmap.put(".ics", "application/octet-stream");
        extmap.put(".idl", "text/plain");
        extmap.put(".ief", "image/ief");
        extmap.put(".iii", "application/x-iphone");
        extmap.put(".inc", "text/plain");
        extmap.put(".inf", "application/octet-stream");
        extmap.put(".inl", "text/plain");
        extmap.put(".ins", "application/x-internet-signup");
        extmap.put(".ipa", "application/x-itunes-ipa");
        extmap.put(".ipg", "application/x-itunes-ipg");
        extmap.put(".ipproj", "text/plain");
        extmap.put(".ipsw", "application/x-itunes-ipsw");
        extmap.put(".iqy", "text/x-ms-iqy");
        extmap.put(".isp", "application/x-internet-signup");
        extmap.put(".ite", "application/x-itunes-ite");
        extmap.put(".itlp", "application/x-itunes-itlp");
        extmap.put(".itms", "application/x-itunes-itms");
        extmap.put(".itpc", "application/x-itunes-itpc");
        extmap.put(".IVF", "video/x-ivf");
        extmap.put(".jar", "application/java-archive");
        extmap.put(".java", "application/octet-stream");
        extmap.put(".jck", "application/liquidmotion");
        extmap.put(".jcz", "application/liquidmotion");
        extmap.put(".jfif", "image/pjpeg");
        extmap.put(".jnlp", "application/x-java-jnlp-file");
        extmap.put(".jpb", "application/octet-stream");
        extmap.put(".jpe", "image/jpeg");
        extmap.put(".jpeg", "image/jpeg");
        extmap.put(".jpg", "image/jpeg");
        extmap.put(".js", "application/x-javascript");
        extmap.put(".jsx", "text/jscript");
        extmap.put(".jsxbin", "text/plain");
        extmap.put(".latex", "application/x-latex");
        extmap.put(".library-ms", "application/windows-library+xml");
        extmap.put(".lit", "application/x-ms-reader");
        extmap.put(".loadtest", "application/xml");
        extmap.put(".lpk", "application/octet-stream");
        extmap.put(".lsf", "video/x-la-asf");
        extmap.put(".lst", "text/plain");
        extmap.put(".lsx", "video/x-la-asf");
        extmap.put(".lzh", "application/octet-stream");
        extmap.put(".m13", "application/x-msmediaview");
        extmap.put(".m14", "application/x-msmediaview");
        extmap.put(".m1v", "video/mpeg");
        extmap.put(".m2t", "video/vnd.dlna.mpeg-tts");
        extmap.put(".m2ts", "video/vnd.dlna.mpeg-tts");
        extmap.put(".m2v", "video/mpeg");
        extmap.put(".m3u", "audio/x-mpegurl");
        extmap.put(".m3u8", "audio/x-mpegurl");
        extmap.put(".m4a", "audio/m4a");
        extmap.put(".m4b", "audio/m4b");
        extmap.put(".m4p", "audio/m4p");
        extmap.put(".m4r", "audio/x-m4r");
        extmap.put(".m4v", "video/x-m4v");
        extmap.put(".mac", "image/x-macpaint");
        extmap.put(".mak", "text/plain");
        extmap.put(".man", "application/x-troff-man");
        extmap.put(".manifest", "application/x-ms-manifest");
        extmap.put(".map", "text/plain");
        extmap.put(".master", "application/xml");
        extmap.put(".mda", "application/msaccess");
        extmap.put(".mdb", "application/x-msaccess");
        extmap.put(".mde", "application/msaccess");
        extmap.put(".mdp", "application/octet-stream");
        extmap.put(".me", "application/x-troff-me");
        extmap.put(".mfp", "application/x-shockwave-flash");
        extmap.put(".mht", "message/rfc822");
        extmap.put(".mhtml", "message/rfc822");
        extmap.put(".mid", "audio/mid");
        extmap.put(".midi", "audio/mid");
        extmap.put(".mix", "application/octet-stream");
        extmap.put(".mk", "text/plain");
        extmap.put(".mmf", "application/x-smaf");
        extmap.put(".mno", "text/xml");
        extmap.put(".mny", "application/x-msmoney");
        extmap.put(".mod", "video/mpeg");
        extmap.put(".mov", "video/quicktime");
        extmap.put(".movie", "video/x-sgi-movie");
        extmap.put(".mp2", "video/mpeg");
        extmap.put(".mp2v", "video/mpeg");
        extmap.put(".mp3", "audio/mpeg");
        extmap.put(".mp4", "video/mp4");
        extmap.put(".mp4v", "video/mp4");
        extmap.put(".mpa", "video/mpeg");
        extmap.put(".mpe", "video/mpeg");
        extmap.put(".mpeg", "video/mpeg");
        extmap.put(".mpf", "application/vnd.ms-mediapackage");
        extmap.put(".mpg", "video/mpeg");
        extmap.put(".mpp", "application/vnd.ms-project");
        extmap.put(".mpv2", "video/mpeg");
        extmap.put(".mqv", "video/quicktime");
        extmap.put(".ms", "application/x-troff-ms");
        extmap.put(".msi", "application/octet-stream");
        extmap.put(".mso", "application/octet-stream");
        extmap.put(".mts", "video/vnd.dlna.mpeg-tts");
        extmap.put(".mtx", "application/xml");
        extmap.put(".mvb", "application/x-msmediaview");
        extmap.put(".mvc", "application/x-miva-compiled");
        extmap.put(".mxp", "application/x-mmxp");
        extmap.put(".nc", "application/x-netcdf");
        extmap.put(".nsc", "video/x-ms-asf");
        extmap.put(".nws", "message/rfc822");
        extmap.put(".ocx", "application/octet-stream");
        extmap.put(".oda", "application/oda");
        extmap.put(".odc", "text/x-ms-odc");
        extmap.put(".odh", "text/plain");
        extmap.put(".odl", "text/plain");
        extmap.put(".odp", "application/vnd.oasis.opendocument.presentation");
        extmap.put(".ods", "application/oleobject");
        extmap.put(".odt", "application/vnd.oasis.opendocument.text");
        extmap.put(".one", "application/onenote");
        extmap.put(".onea", "application/onenote");
        extmap.put(".onepkg", "application/onenote");
        extmap.put(".onetmp", "application/onenote");
        extmap.put(".onetoc", "application/onenote");
        extmap.put(".onetoc2", "application/onenote");
        extmap.put(".orderedtest", "application/xml");
        extmap.put(".osdx", "application/opensearchdescription+xml");
        extmap.put(".p10", "application/pkcs10");
        extmap.put(".p12", "application/x-pkcs12");
        extmap.put(".p7b", "application/x-pkcs7-certificates");
        extmap.put(".p7c", "application/pkcs7-mime");
        extmap.put(".p7m", "application/pkcs7-mime");
        extmap.put(".p7r", "application/x-pkcs7-certreqresp");
        extmap.put(".p7s", "application/pkcs7-signature");
        extmap.put(".pbm", "image/x-portable-bitmap");
        extmap.put(".pcast", "application/x-podcast");
        extmap.put(".pct", "image/pict");
        extmap.put(".pcx", "application/octet-stream");
        extmap.put(".pcz", "application/octet-stream");
        extmap.put(".pdf", "application/pdf");
        extmap.put(".pfb", "application/octet-stream");
        extmap.put(".pfm", "application/octet-stream");
        extmap.put(".pfx", "application/x-pkcs12");
        extmap.put(".pgm", "image/x-portable-graymap");
        extmap.put(".pic", "image/pict");
        extmap.put(".pict", "image/pict");
        extmap.put(".pkgdef", "text/plain");
        extmap.put(".pkgundef", "text/plain");
        extmap.put(".pko", "application/vnd.ms-pki.pko");
        extmap.put(".pls", "audio/scpls");
        extmap.put(".pma", "application/x-perfmon");
        extmap.put(".pmc", "application/x-perfmon");
        extmap.put(".pml", "application/x-perfmon");
        extmap.put(".pmr", "application/x-perfmon");
        extmap.put(".pmw", "application/x-perfmon");
        extmap.put(".png", "image/png");
        extmap.put(".pnm", "image/x-portable-anymap");
        extmap.put(".pnt", "image/x-macpaint");
        extmap.put(".pntg", "image/x-macpaint");
        extmap.put(".pnz", "image/png");
        extmap.put(".pot", "application/vnd.ms-powerpoint");
        extmap.put(".potm", "application/vnd.ms-powerpoint.template.macroEnabled.12");
        extmap.put(".potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
        extmap.put(".ppa", "application/vnd.ms-powerpoint");
        extmap.put(".ppam", "application/vnd.ms-powerpoint.addin.macroEnabled.12");
        extmap.put(".ppm", "image/x-portable-pixmap");
        extmap.put(".pps", "application/vnd.ms-powerpoint");
        extmap.put(".ppsm", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12");
        extmap.put(".ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        extmap.put(".ppt", "application/vnd.ms-powerpoint");
        extmap.put(".pptm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12");
        extmap.put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        extmap.put(".prf", "application/pics-rules");
        extmap.put(".prm", "application/octet-stream");
        extmap.put(".prx", "application/octet-stream");
        extmap.put(".ps", "application/postscript");
        extmap.put(".psc1", "application/PowerShell");
        extmap.put(".psd", "application/octet-stream");
        extmap.put(".psess", "application/xml");
        extmap.put(".psm", "application/octet-stream");
        extmap.put(".psp", "application/octet-stream");
        extmap.put(".pub", "application/x-mspublisher");
        extmap.put(".pwz", "application/vnd.ms-powerpoint");
        extmap.put(".qht", "text/x-html-insertion");
        extmap.put(".qhtm", "text/x-html-insertion");
        extmap.put(".qt", "video/quicktime");
        extmap.put(".qti", "image/x-quicktime");
        extmap.put(".qtif", "image/x-quicktime");
        extmap.put(".qtl", "application/x-quicktimeplayer");
        extmap.put(".qxd", "application/octet-stream");
        extmap.put(".ra", "audio/x-pn-realaudio");
        extmap.put(".ram", "audio/x-pn-realaudio");
        extmap.put(".rar", "application/octet-stream");
        extmap.put(".ras", "image/x-cmu-raster");
        extmap.put(".rat", "application/rat-file");
        extmap.put(".rc", "text/plain");
        extmap.put(".rc2", "text/plain");
        extmap.put(".rct", "text/plain");
        extmap.put(".rdlc", "application/xml");
        extmap.put(".resx", "application/xml");
        extmap.put(".rf", "image/vnd.rn-realflash");
        extmap.put(".rgb", "image/x-rgb");
        extmap.put(".rgs", "text/plain");
        extmap.put(".rm", "application/vnd.rn-realmedia");
        extmap.put(".rmi", "audio/mid");
        extmap.put(".rmp", "application/vnd.rn-rn_music_package");
        extmap.put(".roff", "application/x-troff");
        extmap.put(".rpm", "audio/x-pn-realaudio-plugin");
        extmap.put(".rqy", "text/x-ms-rqy");
        extmap.put(".rtf", "application/rtf");
        extmap.put(".rtx", "text/richtext");
        extmap.put(".ruleset", "application/xml");
        extmap.put(".s", "text/plain");
        extmap.put(".safariextz", "application/x-safari-safariextz");
        extmap.put(".scd", "application/x-msschedule");
        extmap.put(".sct", "text/scriptlet");
        extmap.put(".sd2", "audio/x-sd2");
        extmap.put(".sdp", "application/sdp");
        extmap.put(".sea", "application/octet-stream");
        extmap.put(".searchConnector-ms", "application/windows-search-connector+xml");
        extmap.put(".setpay", "application/set-payment-initiation");
        extmap.put(".setreg", "application/set-registration-initiation");
        extmap.put(".settings", "application/xml");
        extmap.put(".sgimb", "application/x-sgimb");
        extmap.put(".sgml", "text/sgml");
        extmap.put(".sh", "application/x-sh");
        extmap.put(".shar", "application/x-shar");
        extmap.put(".shtml", "text/html");
        extmap.put(".sit", "application/x-stuffit");
        extmap.put(".sitemap", "application/xml");
        extmap.put(".skin", "application/xml");
        extmap.put(".sldm", "application/vnd.ms-powerpoint.slide.macroEnabled.12");
        extmap.put(".sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
        extmap.put(".slk", "application/vnd.ms-excel");
        extmap.put(".sln", "text/plain");
        extmap.put(".slupkg-ms", "application/x-ms-license");
        extmap.put(".smd", "audio/x-smd");
        extmap.put(".smi", "application/octet-stream");
        extmap.put(".smx", "audio/x-smd");
        extmap.put(".smz", "audio/x-smd");
        extmap.put(".snd", "audio/basic");
        extmap.put(".snippet", "application/xml");
        extmap.put(".snp", "application/octet-stream");
        extmap.put(".sol", "text/plain");
        extmap.put(".sor", "text/plain");
        extmap.put(".spc", "application/x-pkcs7-certificates");
        extmap.put(".spl", "application/futuresplash");
        extmap.put(".src", "application/x-wais-source");
        extmap.put(".srf", "text/plain");
        extmap.put(".SSISDeploymentManifest", "text/xml");
        extmap.put(".ssm", "application/streamingmedia");
        extmap.put(".sst", "application/vnd.ms-pki.certstore");
        extmap.put(".stl", "application/vnd.ms-pki.stl");
        extmap.put(".sv4cpio", "application/x-sv4cpio");
        extmap.put(".sv4crc", "application/x-sv4crc");
        extmap.put(".svc", "application/xml");
        extmap.put(".swf", "application/x-shockwave-flash");
        extmap.put(".t", "application/x-troff");
        extmap.put(".tar", "application/x-tar");
        extmap.put(".tcl", "application/x-tcl");
        extmap.put(".testrunconfig", "application/xml");
        extmap.put(".testsettings", "application/xml");
        extmap.put(".tex", "application/x-tex");
        extmap.put(".texi", "application/x-texinfo");
        extmap.put(".texinfo", "application/x-texinfo");
        extmap.put(".tgz", "application/x-compressed");
        extmap.put(".thmx", "application/vnd.ms-officetheme");
        extmap.put(".thn", "application/octet-stream");
        extmap.put(".tif", "image/tiff");
        extmap.put(".tiff", "image/tiff");
        extmap.put(".tlh", "text/plain");
        extmap.put(".tli", "text/plain");
        extmap.put(".toc", "application/octet-stream");
        extmap.put(".tr", "application/x-troff");
        extmap.put(".trm", "application/x-msterminal");
        extmap.put(".trx", "application/xml");
        extmap.put(".ts", "video/vnd.dlna.mpeg-tts");
        extmap.put(".tsv", "text/tab-separated-values");
        extmap.put(".ttf", "application/octet-stream");
        extmap.put(".tts", "video/vnd.dlna.mpeg-tts");
        extmap.put(".txt", "text/plain");
        extmap.put(".u32", "application/octet-stream");
        extmap.put(".uls", "text/iuls");
        extmap.put(".user", "text/plain");
        extmap.put(".ustar", "application/x-ustar");
        extmap.put(".vb", "text/plain");
        extmap.put(".vbdproj", "text/plain");
        extmap.put(".vbk", "video/mpeg");
        extmap.put(".vbproj", "text/plain");
        extmap.put(".vbs", "text/vbscript");
        extmap.put(".vcf", "text/x-vcard");
        extmap.put(".vcproj", "Application/xml");
        extmap.put(".vcs", "text/plain");
        extmap.put(".vcxproj", "Application/xml");
        extmap.put(".vddproj", "text/plain");
        extmap.put(".vdp", "text/plain");
        extmap.put(".vdproj", "text/plain");
        extmap.put(".vdx", "application/vnd.ms-visio.viewer");
        extmap.put(".vml", "text/xml");
        extmap.put(".vscontent", "application/xml");
        extmap.put(".vsct", "text/xml");
        extmap.put(".vsd", "application/vnd.visio");
        extmap.put(".vsi", "application/ms-vsi");
        extmap.put(".vsix", "application/vsix");
        extmap.put(".vsixlangpack", "text/xml");
        extmap.put(".vsixmanifest", "text/xml");
        extmap.put(".vsmdi", "application/xml");
        extmap.put(".vspscc", "text/plain");
        extmap.put(".vss", "application/vnd.visio");
        extmap.put(".vsscc", "text/plain");
        extmap.put(".vssettings", "text/xml");
        extmap.put(".vssscc", "text/plain");
        extmap.put(".vst", "application/vnd.visio");
        extmap.put(".vstemplate", "text/xml");
        extmap.put(".vsto", "application/x-ms-vsto");
        extmap.put(".vsw", "application/vnd.visio");
        extmap.put(".vsx", "application/vnd.visio");
        extmap.put(".vtx", "application/vnd.visio");
        extmap.put(".wav", "audio/wav");
        extmap.put(".wave", "audio/wav");
        extmap.put(".wax", "audio/x-ms-wax");
        extmap.put(".wbk", "application/msword");
        extmap.put(".wbmp", "image/vnd.wap.wbmp");
        extmap.put(".wcm", "application/vnd.ms-works");
        extmap.put(".wdb", "application/vnd.ms-works");
        extmap.put(".wdp", "image/vnd.ms-photo");
        extmap.put(".webarchive", "application/x-safari-webarchive");
        extmap.put(".webtest", "application/xml");
        extmap.put(".wiq", "application/xml");
        extmap.put(".wiz", "application/msword");
        extmap.put(".wks", "application/vnd.ms-works");
        extmap.put(".WLMP", "application/wlmoviemaker");
        extmap.put(".wlpginstall", "application/x-wlpg-detect");
        extmap.put(".wlpginstall3", "application/x-wlpg3-detect");
        extmap.put(".wm", "video/x-ms-wm");
        extmap.put(".wma", "audio/x-ms-wma");
        extmap.put(".wmd", "application/x-ms-wmd");
        extmap.put(".wmf", "application/x-msmetafile");
        extmap.put(".wml", "text/vnd.wap.wml");
        extmap.put(".wmlc", "application/vnd.wap.wmlc");
        extmap.put(".wmls", "text/vnd.wap.wmlscript");
        extmap.put(".wmlsc", "application/vnd.wap.wmlscriptc");
        extmap.put(".wmp", "video/x-ms-wmp");
        extmap.put(".wmv", "video/x-ms-wmv");
        extmap.put(".wmx", "video/x-ms-wmx");
        extmap.put(".wmz", "application/x-ms-wmz");
        extmap.put(".wpl", "application/vnd.ms-wpl");
        extmap.put(".wps", "application/vnd.ms-works");
        extmap.put(".wri", "application/x-mswrite");
        extmap.put(".wrl", "x-world/x-vrml");
        extmap.put(".wrz", "x-world/x-vrml");
        extmap.put(".wsc", "text/scriptlet");
        extmap.put(".wsdl", "text/xml");
        extmap.put(".wvx", "video/x-ms-wvx");
        extmap.put(".x", "application/directx");
        extmap.put(".xaf", "x-world/x-vrml");
        extmap.put(".xaml", "application/xaml+xml");
        extmap.put(".xap", "application/x-silverlight-app");
        extmap.put(".xbap", "application/x-ms-xbap");
        extmap.put(".xbm", "image/x-xbitmap");
        extmap.put(".xdr", "text/plain");
        extmap.put(".xht", "application/xhtml+xml");
        extmap.put(".xhtml", "application/xhtml+xml");
        extmap.put(".xla", "application/vnd.ms-excel");
        extmap.put(".xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
        extmap.put(".xlc", "application/vnd.ms-excel");
        extmap.put(".xld", "application/vnd.ms-excel");
        extmap.put(".xlk", "application/vnd.ms-excel");
        extmap.put(".xll", "application/vnd.ms-excel");
        extmap.put(".xlm", "application/vnd.ms-excel");
        extmap.put(".xls", "application/vnd.ms-excel");
        extmap.put(".xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
        extmap.put(".xlsm", "application/vnd.ms-excel.sheet.macroEnabled.12");
        extmap.put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        extmap.put(".xlt", "application/vnd.ms-excel");
        extmap.put(".xltm", "application/vnd.ms-excel.template.macroEnabled.12");
        extmap.put(".xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
        extmap.put(".xlw", "application/vnd.ms-excel");
        extmap.put(".xml", "text/xml");
        extmap.put(".xmta", "application/xml");
        extmap.put(".xof", "x-world/x-vrml");
        extmap.put(".XOML", "text/plain");
        extmap.put(".xpm", "image/x-xpixmap");
        extmap.put(".xps", "application/vnd.ms-xpsdocument");
        extmap.put(".xrm-ms", "text/xml");
        extmap.put(".xsc", "application/xml");
        extmap.put(".xsd", "text/xml");
        extmap.put(".xsf", "text/xml");
        extmap.put(".xsl", "text/xml");
        extmap.put(".xslt", "text/xml");
        extmap.put(".xsn", "application/octet-stream");
        extmap.put(".xss", "application/xml");
        extmap.put(".xtp", "application/octet-stream");
        extmap.put(".xwd", "image/x-xwindowdump");
        extmap.put(".z", "application/x-compress");
        extmap.put(".zip", "application/x-zip-compressed");
    }

    static {

        sm = new GDrive();

        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GDrive.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }


        Boolean newflag = false;
        java.io.File access = new java.io.File("tokens/lastaccess.txt");
        try {
            Scanner scanner = new Scanner(access);
            String[] split = scanner.nextLine().split("/");
            LocalDateTime localDateTime = LocalDateTime.of(Integer.parseInt(split[2]), Integer.parseInt(split[1]), Integer.parseInt(split[0]), 0, 0);

            LocalDateTime now = LocalDateTime.now();
            now = now.minusDays(4);

            newflag = false;
            if (now.isAfter(localDateTime)) {
                java.io.File del = new java.io.File("tokens/StoredCredential");
                del.delete();
                newflag = true;
            }
        }
        catch (FileNotFoundException e)
        {
            newflag = true;
        }




        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.


        if(newflag)
        {
            FileWriter fw = new FileWriter(access, false);
            fw.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            fw.close();
        }


        return credential;
    }
    private void initializesize()
    {
        long cur = 0;

        List<MyFile> files = GetAllFiles("");

        for(var i : files)
        {
            cur += Long.parseLong(i.getSize());
        }

        currentsize = cur;
    }

    private LocalDateTime ConvertGDate(String date){
        String[] split = date.split("T");
        String[] args = split[0].split("-");
        String[] args1 = split[1].split(":");
        return LocalDateTime.of(Integer.parseInt(args[0]), Integer.parseInt(args[1]),Integer.parseInt(args[2]), Integer.parseInt(args1[0]), Integer.parseInt(args1[1]));
    }

    @Override
    public void CreateStorage(Configuration configuration, String path) {
        try{
            FileList result = GetFromPath(path);
            if(result == null)
            {
                //throw new FileNotFoundException(path);
                try{
                    String parent = "root";
                    String[] split = path.split("/");
                    for(var query: split){
                        result = service.files().list()
                                .setQ("name ='" + query + "' and parents = '" + parent + "' and trashed = false")
                                .setPageSize(10)
                                .setFields("nextPageToken, files(id, name, createdTime, modifiedTime, size)")
                                .execute();
                        if (result.getFiles().size() == 0) {
                            //throw new FileNotFoundException(path);
                            File file = new File();
                            file.setName(query);
                            file.setParents(Collections.singletonList(parent));
                            file.setMimeType("application/vnd.google-apps.folder");
                            service.files()
                                    .create(file)
                                    .setFields("id, parents")
                                    .execute();
                        }
                        result = service.files().list()
                                .setQ("name ='" + query + "' and parents = '" + parent + "' and trashed = false")
                                .setPageSize(10)
                                .setFields("nextPageToken, files(id, name, createdTime, modifiedTime, size)")
                                .execute();
                        parent = result.getFiles().get(0).getId();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                //return;
            }
            String parentid = result.getFiles().get(0).getId();
            result = service.files().list()
                    .setQ("parents = '" + parentid + "' and trashed = false")
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            for(var file : result.getFiles())
            {
                if(file.getName().equalsIgnoreCase(configname))
                    throw new StorageAlreadyInitializedException(storageLocation);
            }
            java.io.File file = new java.io.File("src/main/resources/config.txt");
            FileWriter fw = new FileWriter(file);
            fw.write(configuration.toString());
            fw.close();

            File fileMetadata = new File();
            fileMetadata.setName(configname);
            fileMetadata.setParents(Collections.singletonList(parentid));

            FileContent configContent = new FileContent("text/plain", file);
            //upis u drajv
            try{
                File file1 = service.files().create(fileMetadata, configContent)
                        .setFields("id, parents")
                        .execute();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            //service.files().create()
            storageLocation = path.toString();
            currentconfig = configuration;

            initializesize();

            file.delete();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void LoadStorage(String path)
    {
        try
        {
            FileList filedr = GetFromPath(path + "/config.txt");
            FileOutputStream outputStream = new FileOutputStream("src/main/resources/config.txt");
            service.files().get(filedr.getFiles().get(0).getId())
                    .executeMediaAndDownloadTo(outputStream);

            java.io.File file = new java.io.File("src/main/resources/config.txt");
            //BufferedReader br = new BufferedReader(new FileReader(file));

            Scanner scanner = new Scanner(file);

            long maxsize = Long.parseLong(scanner.nextLine().split("=")[1]);

            String forbidden = scanner.nextLine().split("=")[1];
            forbidden = forbidden.substring(1, forbidden.length() - 1);

            //int maxcount = Integer.parseInt(br.readLine().split("=")[1]);

            Configuration configuration = new Configuration(maxsize, forbidden);


            while(scanner.hasNext())
            {
                String[] limit = scanner.nextLine().split("\\|");
                configuration.getPathlimit().put(limit[0], Integer.parseInt(limit[1]));
            }



            this.currentconfig = configuration;
            this.storageLocation = path;

            initializesize();

            file.delete();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void CreateDirectory(String path, String name) throws IOException{
        try{
        FileList fileList = GetFromPath(path);
        if(fileList == null)
        {
            throw new FileNotFoundException(path);
        }
        File file = new File();
        file.setName(name);
        file.setParents(Collections.singletonList(fileList.getFiles().get(0).getId()));
        file.setMimeType("application/vnd.google-apps.folder");
            FileList check = service.files().list()
                            .setQ("parents = '" + fileList.getFiles().get(0).getId() + "' and trashed = false")
                                    .execute();
            for(var i : check.getFiles())
            {
                if(i.getName().equalsIgnoreCase(name))
                {
                    throw new FileAlreadyExistsException("");
                }
            }

            service.files()
                    .create(file)
                    .setFields("id, parents")
                    .execute();
        }
        catch (FileAlreadyExistsException e){
            e.printStackTrace();
        }
    }

    @Override
    public void CreateDirectory(String path, String name, int filelimit) {
        try {
        FileList fileList = GetFromPath(path);
        if(fileList.getFiles() == null)
        {
            throw new FileNotFoundException(path);
        }
        File file = new File();
        file.setName(name);
        file.setParents(Collections.singletonList(fileList.getFiles().get(0).getId()));
        file.setMimeType("application/vnd.google-apps.folder");

            FileList check = service.files().list()
                    .setQ("parents = '" + fileList.getFiles().get(0).getId() + "' and trashed = false")
                    .execute();
            for (var i : check.getFiles()) {
                if (i.getName().equalsIgnoreCase(name)) {
                    throw new FileAlreadyExistsException("");
                }
            }

            service.files()
                    .create(file)
                    .setFields("id, parents")
                    .execute();
            //brisanje stare konfiguracije
            if(!path.equalsIgnoreCase(""))
                currentconfig.getPathlimit().put(path + "/" + name, filelimit);
            else currentconfig.getPathlimit().put(name, filelimit);
            FileList list = GetFromPath(configname);
            service.files().delete(list.getFiles().get(0).getId()).execute();

            FileList root = GetFromPath("");

            //upis nove konfiguracije u drajv
            java.io.File tempfile = new java.io.File("src/main/resources/config.txt");
            FileWriter fw = new FileWriter(tempfile);
            fw.write(currentconfig.toString());
            fw.close();

            File fileMetadata = new File();
            fileMetadata.setName(configname);
            fileMetadata.setParents(Collections.singletonList(root.getFiles().get(0).getId()));

            FileContent configContent = new FileContent("text/plain", tempfile);
            //upis u drajv
            try{
                File file1 = service.files().create(fileMetadata, configContent)
                        .setFields("id, parents")
                        .execute();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            //service.files().create()
            storageLocation = path.toString();

            tempfile.delete();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void StoreFile(String path, MyFile myFile) throws IOException{
        if(Long.parseLong(myFile.getSize()) + currentsize > currentconfig.getMaxsize()){
            throw new StorageSizeLimitException("");
        }
        if(currentconfig.getPathlimit().containsKey(path))
        {
            int count = CountFiles(path);
            if(count >= currentconfig.getPathlimit().get(path))
            {
                throw new StorageCountLimitException("");
            }
        }
        FileList parent = GetFromPath(path);
        File file = new File();
        file.setName(myFile.getFile().getName());
        file.setParents(Collections.singletonList(parent.getFiles().get(0).getId()));

        FileContent content = new FileContent(extmap.get(myFile.getType()), myFile.getFile());


            service.files()
                    .create(file, content)
                    .setFields("id, parents")
                    .execute();

    }

    @Override
    public void DeleteFromStorage(String s) throws IOException{
            FileList fileList = GetFromPath(s);
            if(fileList == null)
            {
                throw new FileNotFoundException();
            }
            if(Objects.isNull(fileList.getFiles().get(0).size())) {
                long size = fileList.getFiles().get(0).getSize();
                currentsize -=size;
            }
            service.files()
                    .delete(fileList.getFiles().get(0).getId())
                    .execute();
    }

    @Override
    public void MoveFile(String oldpath, String newpath) throws IOException{
        FileList oldlist = GetFromPath(oldpath);
        FileList newlist = GetFromPath(newpath);
            /*
            if (newlist.getFiles().size() == currentconfig.getMaxcount()) {
                throw new StorageCountLimitException("");
            }

         */
            if(currentconfig.getPathlimit().containsKey(newpath))
            {
                int count = CountFiles(newpath);
                if(count >= currentconfig.getPathlimit().get(newpath))
                {
                    //ovde baci gresku isto nije testirano lol
                    return;
                }
            }

            service.files().update(oldlist.getFiles().get(0).getId(), null)
                    .setAddParents(newlist.getFiles().get(0).getId())
                    .setRemoveParents(oldlist.getFiles().get(0).getParents().get(0))
                    .execute();

    }

    @Override
    public void DownloadFile(String sourcePath, String targetPath) {
        try
        {
            FileList file = GetFromPath(sourcePath);
            FileOutputStream outputStream = new FileOutputStream(targetPath);
            service.files().get(file.getFiles().get(0).getId())
                    .executeMediaAndDownloadTo(outputStream);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void Rename(String sourcePath, String newName) {
        try{
            FileList file = GetFromPath(sourcePath);

            //service.files().update(file.getFiles().get(0).getId(), file.getFiles().get(0).setName(newName)).execute();

            File newfile = new File();
            newfile.setName(newName);


            service.files().update(file.getFiles().get(0).getId(), newfile).execute();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public List<MyFile> GetFiles(String s) {
        List<MyFile> output = new ArrayList<>();
        try{
        FileList fileList = GetFromPath(s);
        File root = GetFromPath("").getFiles().get(0);

            FileList result = service.files().list()
                    .setQ("parents = '" + fileList.getFiles().get(0).getId() + "' and trashed = false")
                    .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                    .execute();
            List<File> files = result.getFiles();
                for (File file : files) {
                    output.add(new MyFile(new java.io.File(file.getName() + "." + file.getFileExtension()), file.getFileExtension(), file.getCreatedTime().toString(),
                            file.getModifiedTime().toString(), file.getSize().toString()));
                }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return output;
    }

    @Override
    public List<MyFile> GetAllFiles(String path) {
        List<MyFile> output = new ArrayList<>();

        List<String> idqueue = new ArrayList<>();
        try {
            FileList fl = GetFromPath(path);
            String parid = fl.getFiles().get(0).getId();
            FileList result = service.files().list()
                    .setPageSize(1000)
                    .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                    .setQ("parents in '" + parid + "' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                    .execute();
            List<File> files = result.getFiles();
            for(var file: files){
                idqueue.add(file.getId());
            }
            result = service.files().list()
                    .setPageSize(1000)
                    .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                    .setQ("parents in '" + parid + "' and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
                    .execute();

            files = result.getFiles();
            for (var file : files){
                if(file.getFileExtension()!=null)
                {
                    output.add(new MyFile(new java.io.File(file.getName() + "." + file.getFileExtension()), file.getFileExtension(), file.getCreatedTime().toString(),
                            file.getModifiedTime().toString(), file.getSize().toString()));
                }
            }


            while(!idqueue.isEmpty()){
                String curid = idqueue.get(0);
                result = service.files().list()
                        .setPageSize(1000)
                        .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                        .setQ("parents = '" + curid + "' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                        .execute();
                files = result.getFiles();
                for(var file:files){
                    if(file.getFileExtension() == null)
                        idqueue.add(file.getId());
                }
                result = service.files().list()
                        .setPageSize(1000)
                        .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                        .setQ("parents in '" + curid + "' and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
                        .execute();
                files = result.getFiles();
                for (var file : files){
                    if(file.getFileExtension()!=null)
                    {
                        output.add(new MyFile(new java.io.File(file.getName() + "." + file.getFileExtension()), file.getFileExtension(), file.getCreatedTime().toString(),
                                file.getModifiedTime().toString(), file.getSize().toString()));
                    }
                }
                idqueue.remove(0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return output;
    }

    @Override
    public List<MyFile> GetAllSubFiles(String path) {
        List<MyFile> output = new ArrayList<>();
        List<String> idqueue = new ArrayList<>();
        try {
            String parid = GetFromPath(path).getFiles().get(0).getId();
            FileList result = service.files().list()
                    .setPageSize(1000)
                    .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                    .setQ("parents in '" + parid + "' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                    .execute();
            List<File> files = result.getFiles();
            for(var file: files){
                idqueue.add(file.getId());
            }
            /*
            result = service.files().list()
                    .setPageSize(1000)
                    .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                    .setQ("parents in '" + parid + "' and mimeType != 'application/vnd.google-apps.folder'")
                    .execute();

            files = result.getFiles();
            for (var file : files){
                if(file.getFileExtension()!=null)
                {
                    output.add(new MyFile(new java.io.File(file.getName() + "." + file.getFileExtension()), file.getFileExtension(), file.getCreatedTime().toString(),
                            file.getModifiedTime().toString(), file.getSize().toString()));
                }
            }

             */


            while(!idqueue.isEmpty()){
                String curid = idqueue.get(0);
                result = service.files().list()
                        .setPageSize(1000)
                        .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                        .setQ("parents = '" + curid + "' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                        .execute();
                files = result.getFiles();
                for(var file:files){
                    if(file.getFileExtension() == null)
                        idqueue.add(file.getId());
                }
                result = service.files().list()
                        .setPageSize(1000)
                        .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                        .setQ("parents in '" + curid + "' and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
                        .execute();
                files = result.getFiles();
                for (var file : files){
                    if(file.getFileExtension()!=null)
                    {
                        output.add(new MyFile(new java.io.File(file.getName() + "." + file.getFileExtension()), file.getFileExtension(), file.getCreatedTime().toString(),
                                file.getModifiedTime().toString(), file.getSize().toString()));
                    }
                }
                idqueue.remove(0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return output;
    }

    @Override
    public List<MyFile> GetFilesType(String extension) {
        List<String> ext = Arrays.stream(extension.split(",")).collect(Collectors.toList());
        List<MyFile> output = new ArrayList<>();
        List<String> idqueue = new ArrayList<>();
        try {
            String parid = GetFromPath("").getFiles().get(0).getId();
            FileList result = service.files().list()
                    .setPageSize(1000)
                    .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                    .setQ("parents in '" + parid + "' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                    .execute();
            List<File> files = result.getFiles();
            for(var file: files){
                idqueue.add(file.getId());
            }
            result = service.files().list()
                    .setPageSize(1000)
                    .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                    .setQ("parents in '" + parid + "' and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
                    .execute();

            files = result.getFiles();
            for (var file : files){
                if(ext.contains(file.getFileExtension()))
                {
                    output.add(new MyFile(new java.io.File(file.getName() + "." + file.getFileExtension()), file.getFileExtension(), file.getCreatedTime().toString(),
                            file.getModifiedTime().toString(), file.getSize().toString()));
                }
            }


            while(!idqueue.isEmpty()){
                String curid = idqueue.get(0);
                result = service.files().list()
                        .setPageSize(1000)
                        .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                        .setQ("parents = '" + curid + "' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                        .execute();
                files = result.getFiles();
                for(var file:files){
                    if(file.getFileExtension() == null)
                    idqueue.add(file.getId());
                }
                result = service.files().list()
                        .setPageSize(1000)
                        .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                        .setQ("parents in '" + curid + "' and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
                        .execute();
                files = result.getFiles();
                for (var file : files){
                    if(ext.contains(file.getFileExtension()))
                    {
                        output.add(new MyFile(new java.io.File(file.getName() + "." + file.getFileExtension()), file.getFileExtension(), file.getCreatedTime().toString(),
                                file.getModifiedTime().toString(), file.getSize().toString()));
                    }
                }
                idqueue.remove(0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return output;
    }

    @Override
    public List<MyFile> GetFilesNamed(String name) {
        List<MyFile> output = new ArrayList<>();
        List<String> idqueue = new ArrayList<>();
        try {
            String parid = GetFromPath("").getFiles().get(0).getId();
            FileList result = service.files().list()
                    .setPageSize(1000)
                    .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                    .setQ("parents in '" + parid + "' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                    .execute();
            List<File> files = result.getFiles();
            for(var file: files){
                idqueue.add(file.getId());
            }
            result = service.files().list()
                    .setPageSize(1000)
                    .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                    .setQ("parents in '" + parid + "' and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
                    .execute();

            files = result.getFiles();
            for (var file : files){
                if(file.getName().contains(name))
                {
                    output.add(new MyFile(new java.io.File(file.getName() + "." + file.getFileExtension()), file.getFileExtension(), file.getCreatedTime().toString(),
                            file.getModifiedTime().toString(), file.getSize().toString()));
                }
            }


            while(!idqueue.isEmpty()){
                String curid = idqueue.get(0);
                result = service.files().list()
                        .setPageSize(1000)
                        .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                        .setQ("parents = '" + curid + "' and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                        .execute();
                files = result.getFiles();
                for(var file:files){
                    if(file.getFileExtension() == null)
                        idqueue.add(file.getId());
                }
                result = service.files().list()
                        .setPageSize(1000)
                        .setFields("nextPageToken, files(id, name, parents, fileExtension, createdTime, modifiedTime, size, mimeType)")
                        .setQ("parents in '" + curid + "' and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
                        .execute();
                files = result.getFiles();
                for (var file : files){
                    if(file.getName().contains(name))
                    {
                        output.add(new MyFile(new java.io.File(file.getName() + "." + file.getFileExtension()), file.getFileExtension(), file.getCreatedTime().toString(),
                                file.getModifiedTime().toString(), file.getSize().toString()));
                    }
                }
                idqueue.remove(0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return output;
    }


    //NOT TESTED YET
    @Override
    public boolean IsContained(String path, List<String> filenames) {
        try{
        List<MyFile> output = new ArrayList<>();
        FileList fileList = GetFromPath(path);
        File root = GetFromPath("").getFiles().get(0);
        Map<String, Boolean> map = new HashMap<>();
        for(var i : filenames)
        {
            map.put(i, false);
        }


            FileList result = service.files().list()
                    .setQ("parents = '" + fileList.getFiles().get(0).getId() + "' and trashed = false")
                    .execute();
            List<File> files = result.getFiles();
            for(var file : files)
            {
                if(map.get(file.getName())!=null)
                {
                    map.put(file.getName(), true);
                }
            }
            for(var i : map.keySet())
            {
                if(!map.get(i))
                    return false;
            }
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
        }


        return false;
    }

    @Override
    public String Locate(String name) {
        try {
                FileList result = service.files().list()
                        .setQ("name = '" + name + "' and trashed = false")
                        .setPageSize(10)
                        .setFields("nextPageToken, files(id, name, parents)")
                        .execute();
                if (result.isEmpty()) {
                    throw new FileNotFoundException(name);
                }
                String parent = result.getFiles().get(0).getParents().get(0).toString();
                result = service.files().list()
                    .setQ("id = '" + parent + "' and trashed = false")//popravi ovo kasnije umoran sam iskr
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
                return result.getFiles().get(0).getName();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<MyFile> GetFilesTimeCreated(String path, String begintime, String endtime) {
        List<MyFile> output = new ArrayList<>();
        try{
        String[] beginsplit = begintime.split("\\.");
        String[] endsplit = endtime.split("\\.");


        LocalDateTime begin = LocalDateTime.of(Integer.parseInt(beginsplit[2]),Integer.parseInt(beginsplit[1]),Integer.parseInt(beginsplit[0]),0,0,0);
        LocalDateTime end = LocalDateTime.of(Integer.parseInt(endsplit[2]),Integer.parseInt(endsplit[1]),Integer.parseInt(endsplit[0]),23,59,59);

        FileList list = GetFromPath(path);

            FileList result = service.files().list()
                    .setQ("parents = '" + list.getFiles().get(0).getId() + "' and trashed = false")
                    .setFields("nextPageToken, files(id, name, createdTime, modifiedTime, fileExtension)")
                    .execute();
            List<File> files = result.getFiles();
            for(var file: files)
            {
                LocalDateTime filetime = ConvertGDate(file.getCreatedTime().toString());
                if(begin.isBefore(filetime) && end.isAfter(filetime))
                {
                    Long size = 0L;
                    if(file.getSize()!=null)
                        size = file.getSize();
                    if(file.getFileExtension() != null) {
                        output.add(new MyFile(new java.io.File(path + "/" + file.getName()), file.getFileExtension(), file.getCreatedTime().toString(),
                                file.getModifiedTime().toString(), size.toString()));
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return output;
    }

    @Override
    public List<MyFile> GetFilesTimeModified(String path, String begintime, String endtime) {
        List<MyFile> output = new ArrayList<>();
        try{
        String[] beginsplit = begintime.split("\\.");
        String[] endsplit = endtime.split("\\.");


        LocalDateTime begin = LocalDateTime.of(Integer.parseInt(beginsplit[2]),Integer.parseInt(beginsplit[1]),Integer.parseInt(beginsplit[0]),0,0,0);
        LocalDateTime end = LocalDateTime.of(Integer.parseInt(endsplit[2]),Integer.parseInt(endsplit[1]),Integer.parseInt(endsplit[0]),23,59,59);

        FileList list = GetFromPath(path);


            FileList result = service.files().list()
                    .setQ("parents = '" + list.getFiles().get(0).getId() + "' and trashed = false")
                    .setFields("nextPageToken, files(id, name, createdTime, modifiedTime)")
                    .execute();
            List<File> files = result.getFiles();
            for(var file: files)
            {
                LocalDateTime filetime = ConvertGDate(file.getModifiedTime().toString());
                if(begin.isBefore(filetime) && end.isAfter(filetime))
                {
                    Long size = 0L;
                    if(file.getSize()!=null)
                        size = file.getSize();
                    output.add(new MyFile(new java.io.File(path + "/" + file.getName()), file.getFileExtension(), file.getCreatedTime().toString(),
                            file.getModifiedTime().toString(), size.toString()));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return output;
    }


}
