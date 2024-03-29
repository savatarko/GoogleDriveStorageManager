package createdirectorytest;

import code.GDrive;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import exceptions.MyFileNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
public class CreateDirectoryUnitTest {
    private static GDrive gDrive;

    //@MockBean
    //private Drive driveService;



    @Test
    public void folderNotFoundTest() {

        Drive mockDrive = Mockito.mock(Drive.class);
        gDrive = new GDrive(mockDrive);
        //FileList mockFileList = Mockito.mock(FileList.class);
        Drive.Files mockFiles = Mockito.mock(Drive.Files.class);
        Drive.Files.List mockList = Mockito.mock(Drive.Files.List.class);
        FileList mockfilelist = mock(FileList.class);
        when(mockDrive.files()).thenReturn(mockFiles);
        try {
            when(mockFiles.list()).thenReturn(mockList);
            when(mockList.setQ(anyString())).thenReturn(mockList);
            when(mockList.setPageSize(anyInt())).thenReturn(mockList);
            when(mockList.setFields(anyString())).thenReturn(mockList);
            when(mockList.execute()).thenReturn(mockfilelist);
            //when(mockFileList.getFiles()).thenReturn(null);
            given(mockfilelist.getFiles()).willReturn(null);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        assertThrows(Exception.class, () -> {
            gDrive.CreateDirectory("Test", "test");
        });
        try {
            verify(mockDrive, times(1)).files();
            verify(mockFiles, times(1)).list();
            verify(mockList, times(1)).setQ(anyString());
            verify(mockList, times(1)).setPageSize(anyInt());
            verify(mockList, times(1)).setFields(anyString());
            verify(mockList, times(1)).execute();
            verify(mockfilelist, times(1)).getFiles();
        }
        catch (Exception e){
            e.printStackTrace();
        }
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
        }
        catch (Exception e){
            e.printStackTrace();
        }
        try {
            gDrive.CreateDirectory("Test", "test");
        }

        catch (Exception e){
            e.printStackTrace();
        }
    }

}
