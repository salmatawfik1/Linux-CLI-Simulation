package org.os;

import java.io.*;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Arrays;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class cli {
    public static String PWD() {
        return System.getProperty("user.dir");
    }

    public static void default_LS(String choice ) {

        File directory_files = new File(PWD());


        if ("-a".equals(choice)) {
            LS_A(directory_files );

        } else if ("-r".equals(choice)) {
            LS_r(directory_files );
        }
        else if ("-ar".equals(choice)) {
            LS_ar(directory_files );

        }
        else {


            String[] list_of_dirandfiles = directory_files .list();
            if (list_of_dirandfiles != null && list_of_dirandfiles.length > 0) {
                int i = 0;
                while (i < list_of_dirandfiles.length) {
                    String file_and_dir_Name = list_of_dirandfiles[i];
                    File dir_file = new File(directory_files, file_and_dir_Name);
                    if (!dir_file.isHidden() && !dir_file.getName().startsWith(".")) {
                        System.out.println(dir_file.getName());
                    }
                    i++;
                }

            } else {
                System.out.println("there is no files or directories exit");
            }
        }
    }

    public static void LS_A(File directory) {
        String [] list_of_dirandfiles = directory.list();
        if (list_of_dirandfiles != null && list_of_dirandfiles.length > 0) {
            for ( String file_and_dir_Name : list_of_dirandfiles) {
                System.out.println(file_and_dir_Name);
            }
        } else {
            System.out.println("there is no files or directories exit");
        }
    }

    public static void LS_r(File directory) {

        File[]  file_and_dir_Name = directory.listFiles();
        if ( file_and_dir_Name != null &&  file_and_dir_Name.length > 0) {
            Arrays.sort(file_and_dir_Name, (f1, f2) -> f2.getName().compareTo(f1.getName()));
            int i = 0; // i for index
            while (i < file_and_dir_Name.length) {
                File dir_file = file_and_dir_Name[i];
                if (!dir_file.isHidden() && !dir_file.getName().startsWith(".")) {
                    System.out.println(dir_file.getName());
                }
                i++;
            }

        }

        else {
            System.out.println("there is no files or directories exit");
        }
    }

    public static void LS_ar(File directory) {

        File[] file_and_dir_Name = directory.listFiles();
        if (file_and_dir_Name != null && file_and_dir_Name.length > 0) {
            Arrays.sort(file_and_dir_Name, (f1, f2) -> f2.getName().compareTo(f1.getName()));
            for (File dir_file : file_and_dir_Name) {
                System.out.println(dir_file.getName());

            }

        }
        else {
            System.out.println("there is no files or directories exit");
        }
    }

    public static File CD(String path) {
        File newDirectory;

        if (path.equals("..")) {
            File currentDir = new File(PWD());
            File parentDir = currentDir.getParentFile();

            if (parentDir != null) {
                System.setProperty("user.dir", parentDir.getAbsolutePath());
                newDirectory = parentDir;
            } else {
                System.out.println( PWD());
                newDirectory = currentDir;
            }
        } else {
            newDirectory = new File(path).isAbsolute() ? new File(path) : new File(PWD(), path);

            if (newDirectory.exists() && newDirectory.isDirectory()) {
                System.setProperty("user.dir", newDirectory.getAbsolutePath());
            } else {
                System.out.println("cd: no such file or directory: " + path);
                newDirectory = null;
            }
        }

        return newDirectory;
    }

    public static void mv(String[] paths) {
        int len = paths.length;
        if (len < 2) {
            System.out.println("Error: Insufficient arguments. Provide source and destination paths.");
            return;
        }

        String destPath = paths[len - 1].trim();
        File dest = new File(destPath);

        if (!dest.isAbsolute()) {
            dest = new File(PWD(), destPath);  // Resolve relative to the current directory
        }
        // Case 1: Multiple source files with a destination directory
        if (len > 2) {
            if (!dest.isDirectory()) {
                System.out.println("Error: Destination must be a directory when moving multiple files.");
                return;
            }

            for (int i = 0; i < len - 1; i++) {
                File source = new File(paths[i]);

                // Ensure source exists and is a file
                if (!source.exists() || !source.isFile()) {
                    System.out.println("Error: " + source.getPath() + " does not exist or is not a regular file.");
                    continue;
                }
                File destinationFile = new File(dest, source.getName());

                // Move each file to the directory
                try {
                    Files.move(source.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Moved: " + source.getName() + " to " + dest.getPath());
                } catch (IOException e) {
                    System.out.println("Error moving " + source.getPath() + ": " + e.getMessage());
                }
            }
        }

        // Case 2: Single source file and destination file/rename
        else {
            File source = new File(paths[0]);

            // Check that the source exists and is a file
            if (!source.exists() || !source.isFile()) {
                System.out.println("Error: Source file does not exist or is not a regular file.");
                return;
            }

            if (dest.exists() && dest.isDirectory()) {
                dest = new File(dest, source.getName());
            }

            try {
                Files.move(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Moved/Renamed: " + source.getName() + " to " + dest.getName());
            } catch (IOException e) {
                System.out.println("Error moving/renaming " + source.getPath() + ": " + e.getMessage());
            }
        }
    }

    public static void rm(String[] filesToRemove) {
        if (filesToRemove.length < 1) {
            System.out.println("Error: 'rm' command requires at least one argument.");
            return;
        }
        for (String file1 : filesToRemove) {
            File file = new File(file1);
            if (!file.exists()) {
                System.out.println("Error: '" + file.getName() + "' does not exist.");
                continue;
            }
            if (file.isDirectory()) {
                System.out.println("Error: '" + file.getName() + "' is a directory.");
                continue;
            }

            if (file.delete()) {
                System.out.println("Successfully removed '" + file.getName() + "'.");
            } else {
                System.out.println("Error: Can't delete '" + file.getName() + "'.");
            }
        }
    }

    public static void appendToFile(String text, String fileName) {
        File file1 = new File(fileName);

        try (FileWriter writer = new FileWriter(file1, true)) {
            writer.write(text + System.lineSeparator());
            System.out.println("Appended '" + text + "' to " + fileName);
        } catch (IOException e) {
            System.out.println("Error appending to file: " + e.getMessage());
        }
    }

    public static void OverWrite(String command) {
        if (command.contains(">")) {
            String[] parts = command.split(">");
            if (parts.length == 2) {
                String cmdToExecute = parts[0].trim();
                String fileName = parts[1].trim();
                if (cmdToExecute.equals("pwd")) {
                    String output = PWD();
                    redirectOutputToFile(output, fileName);
                } else {
                    System.out.println("Unsupported command for redirection: " + cmdToExecute);
                }
            } else {
                System.out.println("Invalid command format. Use: command > filename");
            }
        }
    }

    public static void redirectOutputToFile(String output, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(output);
            System.out.println("Output redirected to file: " + fileName);
        } catch (IOException e) {
            System.out.println("Error writing to file: " + fileName);
            e.printStackTrace();
        }
    }

    public static String mkdir(String directoryName) {
        File dir = new File(directoryName);
        if (dir.exists()) {
            return "Directory already exists!";
        }
        boolean isCreated = dir.mkdir();
        if (isCreated) {
            return "Directory created: " + directoryName;
        } else {
            return "Failed to create directory!";
        }
    }

    public static String rmdir(String directoryName) {
        File dir = new File(directoryName);
        if (!dir.exists()) {
            return "Directory does not exist.";
        }
        if (!dir.isDirectory()) {
            return directoryName + " is not a directory.";
        }
        if (dir.listFiles().length > 0) {
            return "Directory is not empty.";
        }
        boolean isDeleted = dir.delete();
        if (isDeleted) {
            return "Directory removed: " + directoryName;
        } else {
            return "Failed to remove directory.";
        }
    }

    private static final String[] RESERVED_NAMES = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "LPT1", "LPT2", "LPT3"};

    public static String touch(String filename) {
        for (String reserved : RESERVED_NAMES) {
            if (filename.equalsIgnoreCase(reserved)) {
                return "Error: Invalid filename - " + filename + " is a reserved name on Windows.";
            }
        }
        File file = new File(filename);
        try {
            if (file.exists()) {
                boolean success = file.setLastModified(System.currentTimeMillis());
                return success ? "File timestamp updated: " + file.getPath() : "Failed to update timestamp for: " + file.getPath();
            } else {
                boolean created = file.createNewFile();
                return created ? "File created: " + file.getPath() : "Failed to create file: " + file.getPath();
            }
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    public static String pipe(String filename, String headCommandPart) {
        String content = catrun(filename);
        if (content.startsWith("Cannot read") || content.startsWith("Error reading")) {
            return content;
        }
        if (headCommandPart.startsWith("head")) {
            String[] headParts = headCommandPart.split(" ");
            int numLines = (headParts.length > 1) ? Integer.parseInt(headParts[1]) : 10;
            return headCommand(content, numLines);
        } else {
            return "Invalid command after pipe. Only 'head' is supported.";
        }
    }

    private static String headCommand(String input, int numLines) {
        StringBuilder result = new StringBuilder();
        String[] lines = input.split(System.lineSeparator());

        for (int i = 0; i < Math.min(numLines, lines.length); i++) {
            result.append(lines[i]);
            if (i < Math.min(numLines, lines.length) - 1) {
                result.append(System.lineSeparator());
            }
        }
        return result.toString();
    }

    public static String catrun(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            return "Cannot read " + filename + ": " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter 'exit' to quit or 'help' for displaying available commands and their usage.");
        while (true) {
            System.out.print("Enter command: ");
            String input = scanner.nextLine().trim();
            if (input.equals("exit")) {
                System.out.println("Exiting.");
                scanner.close();
                return;
            }
            if (input.equals("help")) {
                System.out.println(
                        "Supported commands:\n" +
                                "pwd: Prints the working directory.\n" +
                                "cd <dir>: Changes the current directory. Arguments: directories\n" +
                                "ls: Lists the contents (files & directories) of the current directory.\n" +
                                "ls -a: Lists the contents (files & directories) with hidden items.\n" +
                                "ls -r: Lists the contents (files & directories) in reverse order.\n" +
                                "ls -ar: Lists the contents (files & directories) with hidden items, in reverse order.\n" +
                                "mkdir <dir>: Creates a directory.\n" +
                                "rmdir <dir>: Removes a directory (must be empty).\n" +
                                "touch <file>: Creates a file with each given name.\n" +
                                "mv <src> <dest>: Moves one or more files/directories to a directory or renames a given file.\n" +
                                "rm <file>: Removes each given file.\n" +
                                "cat <file>: Concatenates the content of the files and prints it.\n" +
                                ">: Redirects the output of the first command to be written to a file.\n" +
                                ">>: Like > but appends to the file if it exists.\n" +
                                "|: Redirects the output of the previous command as input to another command.\n" +
                                "help: Displays this help message.\n" +
                                "exit: Exits the command line interface."
                );
                continue;
            }
            if (input.contains("|")) {
                String[] pipedCommands = input.split("\\|");
                if (pipedCommands.length == 2 && pipedCommands[0].trim().startsWith("cat ")) {
                    String filename = pipedCommands[0].substring(4).trim();
                    System.out.println(pipe(filename, pipedCommands[1].trim()));
                } else {
                    System.out.println("Invalid pipe command.");
                }
                continue;
            }
            if (input.contains(">") && !input.contains(">>")) {
                OverWrite(input);
                continue;
            }
            if (input.contains(">>")) {
                String[] redirectionParts = input.split(">>");
                if (redirectionParts[0].trim().equals("pwd")) {
                    appendToFile(PWD(), redirectionParts[1].trim());
                } else {
                    System.out.println("Invalid command before '>>'.");
                }
                continue;
            }
            String[] parts = input.split(" ", 2);
            String command = parts[0];
            String argument = (parts.length > 1) ? parts[1].trim() : "";
            switch (command) {
                case "pwd":
                    System.out.println(PWD());
                    break;
                case "ls":
                    switch (argument) {
                        case "":
                            default_LS("");
                            break;
                        case "-a":
                            default_LS("-a");
                            break;
                        case "-r":
                            default_LS("-r");
                            break;
                        case "-ar":
                            default_LS("-ar");
                            break;
                        default:
                            System.out.println("Invalid option for ls.");
                    }
                    break;
                case "cd":
                    if (!argument.isEmpty()) {
                        CD(argument);
                    } else {
                        System.out.println("Error 'cd' command requires a path.");
                    }
                    break;
                case "mkdir":
                    if (!argument.isEmpty()) {
                        System.out.println(mkdir(argument));
                    } else {
                        System.out.println("Error 'mkdir' command requires a directory name.");
                    }
                    break;
                case "touch":
                    if (!argument.isEmpty()) {
                        System.out.println(touch(argument));
                    } else {
                        System.out.println("Error 'touch' command requires a filename.");
                    }
                    break;
                case "mv":
                    String[] mvPaths = argument.split(" ");
                    if (mvPaths.length >= 2) {
                        mv(mvPaths);
                    } else {
                        System.out.println("Error 'mv' command requires a source and destination.");
                    }
                    break;
                case "rm":
                    if (!argument.isEmpty()) {
                        String[] filesToRemove = argument.split(" ");
                        rm(filesToRemove);
                    } else {
                        System.out.println("Error 'rm' command requires at least one filename.");
                    }
                    break;
                case "rmdir":
                    if (!argument.isEmpty()) {
                        System.out.println(rmdir(argument));
                    } else {
                        System.out.println("Error 'rmdir' command requires a directory name.");
                    }
                    break;
                case "head":
                    String[] headParts = argument.split(" ");
                    if (headParts.length > 0) {
                        String filename = headParts[0];
                        int numLines = (headParts.length > 1) ? Integer.parseInt(headParts[1]) : 10;
                        System.out.println(headCommand(catrun(filename), numLines));
                    } else {
                        System.out.println("Error 'head' command requires a filename.");
                    }
                    break;
                case "cat":
                    if (!argument.isEmpty()) {
                        System.out.println(catrun(argument));
                    } else {
                        System.out.println("Error 'cat' command requires a filename.");
                    }
                    break;
                default:
                    System.out.println("Invalid command.");
            }
        }
    }
}