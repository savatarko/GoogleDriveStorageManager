package createdirectorytest;

import code.GDrive;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
public class MoveFileTest {

    private static GDrive gDrive;

    @Mock
    private Drive mockDrive;

    /*
    @BeforeAll
    public static void setUp() {
        gDrive = new GDrive();
    }

     */

    @Test
    public void test(){
        Drive mockDrive = Mockito.mock(Drive.class);
        //FileList mockFileList = Mockito.mock(FileList.class);
        gDrive = new GDrive(mockDrive);
        Drive.Files mockFiles = Mockito.mock(Drive.Files.class);
        Drive.Files.List mockList = Mockito.mock(Drive.Files.List.class);
        FileList mockfilelist = mock(FileList.class);
        File mockfile = mock(File.class);
        when(mockfile.getParents()).thenReturn(new ArrayList<>(List.of("123")));
        when(mockDrive.files()).thenReturn(mockFiles);
        //List<File> mockfilelist1 = mock(List.class);
        List<File> mockfilelist1 = new ArrayList<>();
        mockfilelist1.add(mockfile);
        Drive.Files.Create create = mock(Drive.Files.Create.class);
        when(mockDrive.files()).thenReturn(mockFiles);
        Drive.Files.Update update = mock(Drive.Files.Update.class);
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
            when(mockFiles.update(anyString(), any())).thenReturn(update);
            when(update.setAddParents(anyString())).thenReturn(update);
            when(update.setRemoveParents(anyString())).thenReturn(update);
            when(update.setFields(anyString())).thenReturn(update);
            when(update.execute()).thenReturn(mockfile);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        try {
            gDrive.MoveFile("test2", "test2");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
