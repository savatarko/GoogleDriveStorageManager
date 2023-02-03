package createdirectorytest;

import code.GDrive;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = GDrive.class)
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class DeleteFromStorageUnitTest {

    private static GDrive gDrive;

    //@MockBean
    //private Drive driveService;

    @BeforeAll
    public static void setUp() {
        gDrive = new GDrive();
    }

    @Test
    public void test(){
        Drive mockDrive = Mockito.mock(Drive.class);
        //FileList mockFileList = Mockito.mock(FileList.class);
        Drive.Files mockFiles = Mockito.mock(Drive.Files.class);
        Drive.Files.List mockList = Mockito.mock(Drive.Files.List.class);
        FileList mockfilelist = mock(FileList.class);
        File mockfile = mock(File.class);
        //List<File> mockfilelist1 = mock(List.class);
        List<File> mockfilelist1 = new ArrayList<>();
        mockfilelist1.add(mockfile);
        Drive.Files.Create create = mock(Drive.Files.Create.class);
        Drive.Files.Delete delete = mock(Drive.Files.Delete.class);
        when(mockDrive.files()).thenReturn(mockFiles);
        try {
            when(mockFiles.list()).thenReturn(mockList);
            when(mockList.setQ(anyString())).thenReturn(mockList);
            when(mockList.setPageSize(anyInt())).thenReturn(mockList);
            when(mockList.setFields(anyString())).thenReturn(mockList);
            when(mockList.execute()).thenReturn(mockfilelist);
            //when(mockFileList.getFiles()).thenReturn(mockfilelist1);
            when(mockfilelist.getFiles()).thenReturn(mockfilelist1);
            //when(mockfilelist1.get(0)).thenReturn(mockfile);
            when(mockfile.getId()).thenReturn("testid");
            when(mockFiles.create(any())).thenReturn(create);
            when(create.setFields(anyString())).thenReturn(create);
            when(create.execute()).thenReturn(mockfile);
            when(mockFiles.delete(anyString())).thenReturn(delete);
            doNothing().when(delete).execute();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        try {
            gDrive.DeleteFromStorage("test");
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    //TODO: ovo baca exception iz nekog razloga, tj pokusava da pozove gdrive?
    @Test
    public void fileNotFoundTest(){
        Drive mockDrive = Mockito.mock(Drive.class);
        //FileList mockFileList = Mockito.mock(FileList.class);
        Drive.Files mockFiles = Mockito.mock(Drive.Files.class);
        Drive.Files.List mockList = Mockito.mock(Drive.Files.List.class);
        FileList mockfilelist = mock(FileList.class);
        File mockfile = mock(File.class);
        //List<File> mockfilelist1 = mock(List.class);
        List<File> mockfilelist1 = new ArrayList<>();
        mockfilelist1.add(mockfile);
        Drive.Files.Create create = mock(Drive.Files.Create.class);
        Drive.Files.Delete delete = mock(Drive.Files.Delete.class);
        when(mockDrive.files()).thenReturn(mockFiles);
        try {
            when(mockFiles.list()).thenReturn(mockList);
            when(mockList.setQ(anyString())).thenReturn(mockList);
            when(mockList.setPageSize(anyInt())).thenReturn(mockList);
            when(mockList.setFields(anyString())).thenReturn(mockList);
            when(mockList.execute()).thenReturn(mockfilelist);
            //when(mockFileList.getFiles()).thenReturn(mockfilelist1);
            when(mockfilelist.getFiles()).thenReturn(null);
            //when(mockfilelist1.get(0)).thenReturn(mockfile);
            when(mockfile.getId()).thenReturn("testid");
            when(mockFiles.create(any())).thenReturn(create);
            when(create.setFields(anyString())).thenReturn(create);
            when(create.execute()).thenReturn(mockfile);
            when(mockFiles.delete(anyString())).thenReturn(delete);
            doNothing().when(delete).execute();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        assertThrows(FileNotFoundException.class, ()->gDrive.DeleteFromStorage("test"));
    }
}
