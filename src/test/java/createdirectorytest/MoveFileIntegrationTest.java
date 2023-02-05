package createdirectorytest;

import code.GDrive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import spec.MyFile;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(classes = GDrive.class)
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class MoveFileIntegrationTest {

    GDrive gDrive = new GDrive();

    @Test
    public void test(){
        gDrive.LoadStorage("testdirectory");
        try {
            File file = new File("src/test/java/createdirectorytest/test.txt");
            MyFile myFile = new MyFile(file);
            gDrive.StoreFile("test1", myFile);
            gDrive.MoveFile("test1/test.txt", "");
            assertTrue(gDrive.IsContained("", List.of("test.txt") ));
            gDrive.DeleteFromStorage("test.txt");
        } catch (Exception e) {
            e.printStackTrace();
            assertNull(e);
        }
    }
}
