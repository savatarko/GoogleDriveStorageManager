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
public class StoreFileIntegrationTest {
    GDrive gDrive = new GDrive();

    @Test
    public void test(){
        File file = new File("src/test/java/createdirectorytest/test.txt");
        gDrive.LoadStorage("testdirectory");
        MyFile myFile = new MyFile(file);
        try {
            gDrive.StoreFile("", myFile);
            assertTrue(gDrive.IsContained("", List.of("test.txt") ));
            gDrive.DeleteFromStorage("test.txt");
        } catch (Exception e) {
            e.printStackTrace();
            assertNull(e);
        }

    }
}
