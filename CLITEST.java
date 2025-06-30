package org.os;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
import static org.os.cli.pipe;
import java.nio.file.Paths;
import java.util.Comparator;

public class CLITEST {

    private String initialDirectory;
    private String tempDir;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final String testFile = "example.txt";
    private final String testFileName = "testOutput.txt";
    private final String commandToTest = "echo Hello World";
    private final String SAMPLE_TEXT = "Text to be appended to files.";
    private final String TEST_DIR = "testDir";
    private final String TEST_FILE_1 = TEST_DIR + "/File1.txt";
    private final String TEST_FILE_2 = TEST_DIR + "/File2.txt";
    private final String TEST_FILE_3 = TEST_DIR + "/File3.txt";
    private final String NEW_FILE_NAME = TEST_DIR + "/newFileName.txt";
    private final String DEST_DIR = TEST_DIR + "/destinationDir";
    private cli CLI;

    @BeforeEach
    public void setup() throws Exception {
        System.setOut(new PrintStream(outContent));
        tempDir = Files.createTempDirectory("testDir").toAbsolutePath().toString();
        System.setProperty("user.dir", tempDir);
        Files.createFile(Paths.get(tempDir, "file1.txt"));
        Files.createFile(Paths.get(tempDir, "file2.txt"));
        Files.createFile(Paths.get(tempDir, ".hiddenfile"));
        Files.createDirectory(Paths.get(tempDir, "subdir"));
        initialDirectory = cli.PWD();
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

    @AfterEach
    public void teardown() throws Exception {
        cli.CD(initialDirectory);
        Path tempPath = Path.of(tempDir);
        Files.walk(tempPath)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        deleteIfExists(TEST_FILE_1);
        deleteIfExists(TEST_FILE_2);
        deleteIfExists(NEW_FILE_NAME);
        deleteDirectoryRecursively(new File(DEST_DIR));
        deleteDirectoryRecursively(new File(TEST_DIR));
        System.setOut(System.out);
    }

    @Test
    public void PWD() {
        String expectedDirectory = System.getProperty("user.dir");
        String actualDirectory = cli.PWD();
        assertEquals(expectedDirectory, actualDirectory, "The PWD method should return the current working directory.");
    }

    @Test
    public void DefaultLS_NoOptions() {
        cli.default_LS("");
        String output = outContent.toString();
        assertTrue(output.contains("file1.txt"));
        assertTrue(output.contains("file2.txt"));
        assertFalse(output.contains(".hiddenfile"));
    }

    @Test
    public void LS_A() {
        cli.default_LS("-a");
        String output = outContent.toString();
        assertTrue(output.contains("file1.txt"));
        assertTrue(output.contains("file2.txt"));
        assertTrue(output.contains(".hiddenfile"));
    }

    @Test
    public void LS_r() {
        cli.default_LS("-r");
        String output = outContent.toString();
        assertTrue(output.indexOf("file2.txt") < output.indexOf("file1.txt"));
        assertFalse(output.contains(".hiddenfile"));
    }

    @Test
    public void LS_ar() {
        cli.default_LS("-ar");
        String output = outContent.toString();
        assertTrue(output.indexOf("file2.txt") < output.indexOf("file1.txt"));
        assertTrue(output.contains("file1.txt"));
        assertTrue(output.contains("file2.txt"));
        assertTrue(output.contains(".hiddenfile"));
    }

    @Test
    public void CD_ValidPath() {
        cli.CD("subdir");
        assertEquals(new File(tempDir, "subdir").getAbsolutePath(), System.getProperty("user.dir"));
    }

    @Test
    void CDWithAbsolutePath() {
        String absolutePath = "/usr/local";
        File result = cli.CD(absolutePath);

        if (new File(absolutePath).exists()) {
            assertEquals(absolutePath, result.getAbsolutePath(),
                    "Should change to the provided absolute directory.");
        } else {
            assertNull(result, "Result should be null if the absolute path does not exist.");
        }
    }

    @Test
    public void CD_InvalidPath() {
        cli.CD("invalidDir");
        String output = outContent.toString();
        assertTrue(output.contains("cd: no such file or directory: invalidDir"));
    }

    @Test
    public void CD_ParentDirectory() {
        cli.CD("subdir");
        cli.CD("..");
        assertEquals(tempDir, System.getProperty("user.dir"));
    }

    @Test
    void MkdirCreatesDirectory() throws IOException {
        String dirName = "testdir";
        Path dirPath = Path.of(dirName);
        if (Files.exists(dirPath)) {
            Files.walk(dirPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            assertFalse(Files.exists(dirPath), "Setup failed: Could not delete existing test directory.");
        }
        String result = cli.mkdir(dirName);
        assertEquals("Directory created: " + dirName, result, "Expected success message for directory creation.");
        assertTrue(Files.exists(dirPath), "Directory should exist after mkdir command.");
        Files.walk(dirPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        assertFalse(Files.exists(dirPath), "Test directory cleanup failed.");
    }

    @Test
    void MkdirFailsIfDirectoryExists() {
        String dirName = "existingdir";
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String result = cli.mkdir(dirName);
        assertEquals("Directory already exists!", result);
        dir.delete();
    }

    @Test
    void MkdirFailsForInvalidDirectoryName() {
        String invalidDirName = "invalid*dir";
        String result = cli.mkdir(invalidDirName);
        assertEquals("Failed to create directory!", result);
        File dir = new File(invalidDirName);
        assertFalse(dir.exists(), "Invalid directory should not be created.");
    }

    @Test
    void MkdirCreatesNestedDirectories() {
        String nestedDir = "parentDir/childDir";
        String result = cli.mkdir(nestedDir);
        assertEquals("Failed to create directory!", result);
        File dir = new File(nestedDir);
        if (dir.exists()) {
            dir.delete();
        }
    }

    @Test
    void RmdirRemovesEmptyDirectory() {
        String dirName = "testEmptyDir";
        File dir = new File(dirName);
        dir.mkdir();
        String result = cli.rmdir(dirName);
        assertEquals("Directory removed: " + dirName, result);
        assertFalse(dir.exists(), "Directory should have been deleted.");
    }

    @Test
    void RmdirFailsIfDirectoryDoesNotExist() {
        String nonExistentDir = "nonExistentDir";
        String result = cli.rmdir(nonExistentDir);
        assertEquals("Directory does not exist.", result);
    }

    @Test
    void RmdirFailsIfNotADirectory() {
        String fileName = "notADirectory.txt";
        File file = new File(fileName);
        try {
            file.createNewFile();
        } catch (Exception e) {
            fail("Failed to create test file.");
        }
        String result = cli.rmdir(fileName);
        assertEquals(fileName + " is not a directory.", result);
        file.delete();
    }

    @Test
    void RmdirFailsIfDirectoryNotEmpty() {
        String dirName = "nonEmptyDir";
        String fileName = "nonEmptyDir/file.txt";
        File dir = new File(dirName);
        File file = new File(fileName);
        dir.mkdir();
        try {
            file.createNewFile();
        } catch (Exception e) {
            fail("Failed to create test file in the directory.");
        }
        String result =cli.rmdir(dirName);
        assertEquals("Directory is not empty.", result);
        file.delete();
        dir.delete();
    }

    @Test
    void TouchCreatesNewFileInCurrentDirectory() throws IOException {
        String fileName = "example.txt";
        Path filePath = Path.of(fileName);
        Files.deleteIfExists(filePath);
        String result = cli.touch(fileName);
        assertTrue(Files.exists(filePath), "File should be created in the current directory.");
        assertEquals("File created: " + fileName, result, "Expected success message for file creation.");
        Files.deleteIfExists(filePath);
    }

    @Test
    void TouchFailsOnInvalidPath() {
        String invalidPath = "CON";
        String result =cli.touch(invalidPath);
        assertTrue(result.contains("Error: Invalid filename - CON"), "Expected failure message for invalid filename.");
    }

    @Test
    void CatExistingFile() throws IOException {
        Path tempFile = Files.createTempFile("testFile", ".txt");
        Files.write(tempFile, "Sample content".getBytes());
        String output = cli.catrun(tempFile.toString());
        assertTrue(output.contains("Sample content"));
        Files.deleteIfExists(tempFile);
    }

    @Test
    void CatNonExistingFile() {
        String output = cli.catrun("non_existing_file.txt");
        assertTrue(output.contains("Cannot read non_existing_file.txt"));
    }

    @Test
    void RedirectOutputToFile() {
        cli.redirectOutputToFile(commandToTest, testFileName);
        File file = new File(testFileName);
        assertTrue(file.exists(), "Output file should be created.");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            assertEquals(commandToTest, line, "File content should match the expected output.");
        } catch (IOException e) {
            fail("IOException occurred while reading the file: " + e.getMessage());
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

    private void deleteIfExists(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            Files.delete(file.toPath());
        }
    }

    private void deleteDirectoryRecursively(File dir) throws IOException {
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    deleteDirectoryRecursively(file);
                } else {
                    Files.deleteIfExists(file.toPath());
                }
            }
            Files.deleteIfExists(dir.toPath());
        }
    }

    @Test
    public void AppendToNonExistentFile() throws IOException {
        CLI.appendToFile(SAMPLE_TEXT, TEST_FILE_2);
        assertTrue(Files.exists(Paths.get(TEST_FILE_2)));
        String content = Files.readString(Paths.get(TEST_FILE_2));
        assertEquals(SAMPLE_TEXT + System.lineSeparator(), content);
    }

    @Test
    public void AppendToFile_FileExistsWithOriginalContent() throws IOException {
        Files.writeString(Paths.get(TEST_FILE_1), "Initial Content\n");
        CLI.appendToFile(SAMPLE_TEXT, TEST_FILE_1);
        String content = Files.readString(Paths.get(TEST_FILE_1));
        assertTrue(content.startsWith("Initial Content"));
        assertTrue(content.endsWith(SAMPLE_TEXT + System.lineSeparator()));
    }

    @Test
    public void AppendToFileMultipleTimes() throws IOException {
        CLI.appendToFile(SAMPLE_TEXT, TEST_FILE_2);
        CLI.appendToFile(SAMPLE_TEXT, TEST_FILE_2);
        String content = Files.readString(Paths.get(TEST_FILE_2));
        String expectedContent = SAMPLE_TEXT + System.lineSeparator() + SAMPLE_TEXT + System.lineSeparator();
        assertEquals(expectedContent, content);
    }

    @Test
    void PipeWithDefaultHeadCommand() {
        String result = pipe(TEST_FILE_1, "head");
        String expected = "Line1\nLine2\nLine3\nLine4\nLine5\nLine6\nLine7\nLine8\nLine9\nLine10"; // No newline at the end
        assertEquals(expected, result);
    }

    @Test
    void PipeWithInvalidCommandAfterPipe() {
        String result = pipe(TEST_FILE_1, "tail 5");
        assertEquals("Invalid command after pipe. Only 'head' is supported.", result);
    }

    @Test
    void PipeWithFileNotFound() {
        String result = pipe("nonexistent.txt", "head 5");
        assertTrue(result.startsWith("Cannot read nonexistent.txt"));
    }

}