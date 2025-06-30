package org.os;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

public class MvRmTest {

        private static final String TEST_DIR = "testDir";
        private static final String TEST_FILE_1 = TEST_DIR + "/File1.txt";
        private static final String TEST_FILE_2 = TEST_DIR + "/File2.txt";
        private static final String TEST_FILE_3 = TEST_DIR + "/File3.txt";
        private static final String DEST_DIR = TEST_DIR + "/destinationDir";
        private static final String NEW_FILE_NAME = TEST_DIR + "/newFileName.txt";

        private cli CLI;

        @BeforeEach
        public void setUp() throws IOException {
            CLI = new cli();


            File testDir = new File(TEST_DIR);
            if (!testDir.exists()) {
                Files.createDirectory(testDir.toPath());
            }


            createFileIfNotExists(TEST_FILE_1, "Line1\nLine2\nLine3\nLine4\nLine5\nLine6\nLine7\nLine8\nLine9\nLine10");
            createFileIfNotExists(TEST_FILE_2);


            File destDir = new File(DEST_DIR);
            if (!destDir.exists()) {
                Files.createDirectory(destDir.toPath());
            }
        }

        private void createFileIfNotExists(String filePath, String content) throws IOException {
            File file = new File(filePath);
            if (!file.exists()) {
                Files.writeString(file.toPath(), content);
            }
        }

        private void createFileIfNotExists(String filePath) throws IOException {
            File file = new File(filePath);
            if (!file.exists()) {

                file.createNewFile();
            }
        }

        @Test
        public void mv_InsufficientArguments() {
            String[] paths = {TEST_FILE_1};
            CLI.mv(paths);
        }

        @Test
        public void mv_OverwriteExistingFile()  {
            String[] paths = {TEST_FILE_1, TEST_FILE_2};
            CLI.mv(paths);

            assertTrue(new File(TEST_FILE_2).exists());
            assertFalse(new File(TEST_FILE_1).exists());
        }

        @Test
        public void mv_RenameFile()  {
            String[] paths = {TEST_FILE_1, NEW_FILE_NAME};
            CLI.mv(paths);

            assertTrue(new File(NEW_FILE_NAME).exists());
            assertFalse(new File(TEST_FILE_1).exists());
        }

        @Test
        public void mv_MoveMultipleFilesToDirectory() {
            String[] paths = {TEST_FILE_1, TEST_FILE_2, DEST_DIR};
            CLI.mv(paths);

            assertTrue(new File(DEST_DIR + "/File1.txt").exists());
            assertTrue(new File(DEST_DIR + "/File2.txt").exists());
            assertFalse(new File(TEST_FILE_1).exists());
            assertFalse(new File(TEST_FILE_2).exists());
        }

        @Test
        public void mv_MoveInvalidSourceFile() {
            String[] paths = {"invalidFile.txt", DEST_DIR};
            CLI.mv(paths);

        }

        @Test
        public void mv_MoveToInvalidDestination() {
            String[] paths = {TEST_FILE_1, TEST_FILE_2, TEST_FILE_3};
            CLI.mv(paths);

        }

        @Test
        public void mv_MoveDirectoryAsSource() {
            String[] paths = {DEST_DIR, TEST_FILE_1};
            CLI.mv(paths);

        }

        @Test
        public void rm_InsufficientArguments() {
            String[] noArgs = {}; // No arguments provided
            cli.rm(noArgs);
            // Expected: Error message about arguments
        }

        @Test
        public void rm_RemoveFileSuccessfully() throws IOException {
            File testFile = new File("testFile.txt");
            testFile.createNewFile();

            cli.rm(new String[]{"testFile.txt"});

            assertFalse(testFile.exists());
        }

        @Test
        public void rm_RemoveMultipleFiles() {
            cli.rm(new String[]{TEST_FILE_1, TEST_FILE_2});
            assertFalse(new File(TEST_FILE_1).exists());
            assertFalse(new File(TEST_FILE_2).exists());
        }

        @Test
        public void rm_RemoveFilesAndDirectory() {
            cli.rm(new String[]{TEST_FILE_1, TEST_DIR});
            assertFalse(new File(TEST_FILE_1).exists());
            assertTrue(new File(TEST_DIR).exists());
        }

        @Test
        public void rm_deleteMixedFilesIncludingNonExistent() {
            cli.rm(new String[]{TEST_FILE_1, "File55.txt"});
            assertFalse(new File(TEST_FILE_1).exists());
            assertFalse(new File("File55.txt").exists());
        }
}
