package createdirectorytest;

import code.GDrive;
import com.google.api.services.drive.Drive;
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

    @BeforeAll
    public static void setUp() {
        gDrive = new GDrive();
    }



    @Test
    public void folderNotFoundTest() {

        Drive mockDrive = Mockito.mock(Drive.class);
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


        //when(gDrive.GetFromPath("Test")).thenReturn(null);


        assertThrows(Exception.class, () -> {
            gDrive.CreateDirectory("Test", "test");
        });
    }

}
