package code;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import exceptions.StorageAlreadyInitializedException;
import exceptions.StorageCountLimitException;
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

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.security.GeneralSecurityException;
import java.util.*;

//import java.io.File;
import java.io.IOException;

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
    /*
    private static final List<String> SCOPES =
            Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);

     */
    private static final List<String> SCOPES =
            Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static NetHttpTransport HTTP_TRANSPORT;

    private static Drive service;

    //gets the file from the given path
    private FileList GetFromPath(String path)
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
                            .setQ("name ='" + query + "' and parents = '" + parent + "'")
                            .setPageSize(10)
                            .setFields("nextPageToken, files(id, name)")
                            .execute();
                    if (result.isEmpty()) {
                        throw new FileNotFoundException(path);
                    }
                    parent = result.getFiles().get(0).getId();
                }
            }
            if(path.length() == 0)
                return result;
            for (var query : split) {
                result = service.files().list()
                        .setQ("name ='" + query + "' and parents = '" + parent + "'")
                        .setPageSize(10)
                        .setFields("nextPageToken, files(id, name)")
                        .execute();
                if (result.isEmpty()) {
                    throw new FileNotFoundException(path);
                }
                parent = result.getFiles().get(0).getId();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //System.out.println(result.getFiles().get(0).getId());
        return result;
    }
    //gets the path to the root from the given file
    private String GetPathFromRoot(File file)
    {
        List<String> path = new ArrayList<>();



        return null;
    }


    static {
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
        return credential;
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        //final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        /*
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
                .setPageSize(50)
                .setFields("nextPageToken, files(id, name, parents)")
                .execute();
        List<com.google.api.services.drive.model.File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (com.google.api.services.drive.model.File file : files) {
                System.out.printf("%s (%s) %s\n", file.getName(), file.getId(), file.getParents());
            }
        }

       */

        StorageManager test = new GDrive();
        test.LoadStorage("test2");

        //test.CreateStorage("test2");//test.DownloadFile("test2/config.txt", "D:/projtest/test.txt");
        // System.out.println(test.Locate("config.txt"));

        //test.Rename("file/drive.txt", "hello");

        //test.GetFiles("file");

        test.CreateDirectory("subdir", "newdir");
    }


    @Override
    public void CreateStorage(Configuration configuration, String path) {
        try{
            /*
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                    .createScoped(Arrays.asList(DriveScopes.DRIVE_FILE));
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
                    credentials);

             */

            //final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport(); //samo da pitam jel ovo uvek treba da se pokrece?
            FileList result = GetFromPath(path);
            if(result == null)
            {
                throw new FileNotFoundException(path);
            }
            String parentid = result.getFiles().get(0).getId();
            result = service.files().list()
                    .setQ("parents = '" + parentid + "'")
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
            BufferedReader br = new BufferedReader(new FileReader(file));

            long maxsize = Long.parseLong(br.readLine().split("=")[1]);

            String forbidden = br.readLine().split("=")[1];
            forbidden = forbidden.substring(1, forbidden.length() - 1);

            int maxcount = Integer.parseInt(br.readLine().split("=")[1]);

            Configuration configuration = new Configuration(maxsize, forbidden, maxcount);

            this.currentconfig = configuration;
            this.storageLocation = path;

            file.delete();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void CreateDirectory(String path, String name) {
        FileList fileList = GetFromPath(path);
        File file = new File();
        file.setName(name);
        file.setParents(Collections.singletonList(fileList.getFiles().get(0).getId()));
        file.setMimeType("application/vnd.google-apps.folder");
        try{
            FileList check = service.files().list()
                            .setQ("parents = '" + fileList.getFiles().get(0).getId() + "'")
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
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void StoreFile(String path, MyFile myFile) {

    }

    @Override
    public void DeleteFromStorage(String s) {
        try {
            FileList fileList = GetFromPath(s);
            service.files()
                    .delete(fileList.getFiles().get(0).getId())
                    .execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //NOT TESTED YET!!
    @Override
    public void MoveFile(String oldpath, String newpath) {
        FileList oldlist = GetFromPath(oldpath);
        FileList newlist = GetFromPath(newpath);

        try {
            if (newlist.getFiles().size() == currentconfig.getMaxcount()) {
                throw new StorageCountLimitException("");
            }

            service.files().update(oldlist.getFiles().get(0).getId(), null)
                    .setAddParents(newlist.getFiles().get(0).getId())
                    .setRemoveParents(oldlist.getFiles().get(0).getParents().get(0))
                    .execute();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
        FileList fileList = GetFromPath(s);
        File root = GetFromPath("").getFiles().get(0);

        try{
            FileList result = service.files().list()
                    .setQ("parents = '" + fileList.getFiles().get(0).getId() + "'")
                    .execute();
            //test
            List<File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files:");
                for (com.google.api.services.drive.model.File file : files) {
                    System.out.printf("%s (%s) %s\n", file.getName(), file.getId(), file.getParents());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    public List<MyFile> GetAllFiles(String s) {
        return null;
    }

    @Override
    public List<MyFile> GetAllSubFiles(String s) {
        return null;
    }

    @Override
    public List<MyFile> GetFilesType(String s, String s1) {
        return null;
    }

    @Override
    public List<MyFile> GetFilesNamed(String s, String s1) {
        return null;
    }


    //NOT TESTED YET
    @Override
    public boolean IsContained(String path, List<String> filenames) {
        List<MyFile> output = new ArrayList<>();
        FileList fileList = GetFromPath(path);
        File root = GetFromPath("").getFiles().get(0);
        Map<String, Boolean> map = new HashMap<>();
        for(var i : filenames)
        {
            map.put(i, false);
        }

        try{
            FileList result = service.files().list()
                    .setQ("parents = '" + fileList.getFiles().get(0).getId() + "'")
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
                        .setQ("name = '" + name + "'")
                        .setPageSize(10)
                        .setFields("nextPageToken, files(id, name, parents)")
                        .execute();
                if (result.isEmpty()) {
                    throw new FileNotFoundException(name);
                }
                String parent = result.getFiles().get(0).getParents().get(0).toString();
                result = service.files().list()
                    .setQ("id = '" + parent + "'")//popravi ovo kasnije umoran sam iskr
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
    public List<MyFile> GetFilesTime(String s, String s1, String s2) {
        return null;
    }

}
