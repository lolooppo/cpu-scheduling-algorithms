/**********************************************************************************************************************************************************************************
 Purpose:
 An OS interfaces with a user through CLI, A CLI is a software module capable of interpreting textual commands coming from the user's keyboard .

 Command Lines:
 1) args
 2) help
 3) date
 4) ls
 5) rls
 6) cd
 7) cp
 8) mv
 9) rm
 10) mkdir
 11) rmdir
 12) cat
 13) pwd
 ***********************************************************************************************************************************************************************************/

import java.io.*;
import java.nio.file.*;
import java.util.Objects;
import java.util.Scanner;

// Parser class for parsing user input
class Parser {
    String commandName;
    String[] args;

    public Parser() {
        commandName = "";
        args = new String[0];
    }

    // Parse the input command into command name and arguments
    public boolean parse(String input) {
        if (input.isEmpty()) {
            return false;
        }
        String[] arr = input.trim().split("\\s+");
        commandName = arr[0];
        args = new String[arr.length - 1];
        System.arraycopy(arr, 1, args, 0, arr.length - 1);
        return true;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }
}

// FileHandler class for reading/writing files
class FileHandler {
    // Read file contents
    public static String readFile(String path) {
        StringBuilder data = new StringBuilder();
        try {
            FileReader reader = new FileReader(path);
            int c;
            while ((c = reader.read()) != -1) {
                data.append((char) c);
            }
            reader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return data.toString();
    }

    // Write data to file
    public static void writeFile(String path, String data, boolean isAppend) {
        try {
            FileWriter writer = new FileWriter(path, isAppend);
            writer.append(data);
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

// History class for managing command history
class History {
    private int nextLine;

    public History() {
        setNumberOfLines();
    }

    // Count the number of lines in history file
    private void setNumberOfLines() {
        nextLine = 1;
        try {
            FileReader fileReader = new FileReader("history.csv");
            int c;
            while ((c = fileReader.read()) != -1) {
                if (c == '\n') {
                    nextLine++;
                }
            }
            fileReader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // Add command to history
    public void add(String commandName, String[] args) {
        StringBuilder line = new StringBuilder();
        line.append(nextLine++).append(" ").append(commandName);
        for (String arg : args) {
            line.append(" ").append(arg);
        }
        line.append("\n");
        FileHandler.writeFile("history.csv", line.toString(), true);
    }

    // Print command history
    public void print() {
        String data = FileHandler.readFile("history.csv");
        System.out.println(data);
    }
}
// Copy class for copying files and directories
class Copy {

    // Method to handle the copy command
    public void copy(String[] args) {
        // Check if arguments are provided
        if (args.length == 0) {
            System.out.println("cp: no arguments passed");
            return;
        }
        // Check if recursive flag is provided
        if ("-r".equals(args[0])) {
            // Check if two directory paths are provided
            if (args.length != 3) {
                System.out.println("cp: two directories paths must be passed");
                return;
            }
            // Copy directory recursively
            copyDirectory(args[1], args[2]);
        } else {
            // Check if two file paths are provided
            if (args.length != 2) {
                System.out.println("cp: two files paths must be passed");
                return;
            }
            // Copy file
            copyFile(args[0], args[1]);
        }
    }

    // Method to copy a directory recursively
    private void copyDirectory(String src, String dest) {
        // Normalize source and destination paths
        Path srcPath = Terminal.fullFilePath(src), destPath = Terminal.fullFilePath(dest);
        // Check if source is the root directory
        if (srcPath.getNameCount() == 0) {
            System.out.println("cp: cannot copy root");
            return;
        }
        // Check if source and destination are the same
        if (srcPath.equals(destPath)) {
            System.out.println("cp: cannot copy directory into itself");
            return;
        }
        // Check if source is a directory
        if (!Files.isDirectory(srcPath)) {
            System.out.println("cp: " + srcPath + ": is not a directory");
            return;
        }
        // Check if destination is a directory
        if (!Files.isDirectory(destPath)) {
            System.out.println("cp: " + destPath + ": is not a directory");
            return;
        }
        try {
            // Traverse source directory and copy files/directories
            Files.walk(srcPath).forEach(path -> {
                int i = srcPath.getNameCount() - 1;
                int j = path.getNameCount();
                Path addedPath = destPath.resolve(path.subpath(i, j));
                try {
                    // If the current path is a directory, skip
                    if (Files.isDirectory(addedPath)) {
                        return;
                    }
                    // Copy the file to the destination
                    Files.copy(path, addedPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Method to copy a file
    private void copyFile(String src, String dest) {
        // Normalize source and destination paths
        Path srcPath = Terminal.fullFilePath(src), destPath = Terminal.fullFilePath(dest);
        // Check if source file exists and is not a directory
        if (Files.notExists(srcPath) || Files.isDirectory(srcPath)) {
            System.out.println("cp: " + srcPath + ": does not exist or is not a file");
            return;
        }
        // Check if destination's parent directory exists and is not a directory
        if ((destPath.getNameCount() > 1 && !Files.isDirectory(destPath.getParent())) || Files.isDirectory(destPath)) {
            System.out.println("cp: " + destPath + ": does not exist or is not a file");
            return;
        }
        try {
            // Copy the source file to the destination
            Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
// Terminal class for implementing command-line interface functionality
class Terminal {
    Parser parser;
    static Path currentWorkingDirectory = Paths.get(System.getProperty("user.dir"));

    // Method to get the full file path
    public static Path fullFilePath(String fileName) {
        return currentWorkingDirectory.resolve(fileName).normalize();
    }

    // Constructor for Terminal class
    public Terminal() {
        parser = new Parser();
    }

    // Method to print echo arguments
    public void echo(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            System.out.print(args[i]);
            if (i + 1 != args.length) {
                System.out.print(" ");
            }
        }
        System.out.println();
    }

    // Method to list files in sorted order
    public void lsSorted() {
        File currentDirPath = new File(currentWorkingDirectory.toString());
        File[] files = currentDirPath.listFiles();
        if (files != null) {
            for (File file : files) {
                System.out.println(file.getName());
            }
        }
    }

    // Method to list files in reversed order
    public void lsReversed() {
        File currentDirPath = new File(currentWorkingDirectory.toString());
        File[] files = currentDirPath.listFiles();
        if (files != null) {
            for (int i = files.length - 1; i >= 0; i--) {
                System.out.println(files[i].getName());
            }
        }
    }

    // Method to list files based on arguments
    public void ls(String[] args) {
        if (args.length == 0) {
            lsSorted();
        } else if ("-r".equals(args[0])) {
            lsReversed();
        } else {
            lsSorted();
        }
    }

    // Method to count lines, words, and characters in a file
    public void wc(String[] args) {
        if (args.length < 1) {
            System.out.println("wc: no arguments passed");
            return;
        } else if (args.length > 1) {
            System.out.println("wc: many arguments passed");
            return;
        }
        // Get file path
        Path path = fullFilePath(args[0]);
        File file = new File(path.toUri());
        // Check if file exists
        if (!file.exists()) {
            System.out.println("wc: " + args[0] + ": file does not exist");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int lineCount = 0;
            int wordCount = 0;
            int charCount = 0;
            String line;

            // Read file line by line
            while ((line = reader.readLine()) != null) {
                lineCount++;
                charCount += line.length();
                String[] words = line.split("\\s+");
                wordCount += words.length;
            }

            // Print output with line count, word count, character count, and file name
            String output = lineCount + " " + wordCount + " " + charCount + " " + path.getFileName();
            System.out.println(output);
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }
    }

    // Method to print command history
    public void history() {
        History history = new History();
        history.print();
    }

    // Method to copy files or directories
    public void cp(String[] args) {
        Copy copy = new Copy();
        copy.copy(args);
    }

    // Method to create directories
    public void mkdir(String[] args) {
        for (String arg : args) {
            Path path = fullFilePath(arg);
            int pathLength = path.getNameCount();
            Path parentPath = path.getParent();
            // Check if parent directory exists
            if (pathLength > 1 && !Files.isDirectory(parentPath)) {
                System.out.println("mkdir: " + parentPath + ": does not exist or is not a directory");
                continue;
            }
            // Check if directory already exists
            if (Files.exists(path)) {
                System.out.println("mkdir: " + path + ": file already exists");
                continue;
            }
            try {
                Files.createDirectory(path);
            } catch (Exception e) {
                System.out.println("mkdir: " + parentPath + ": does not exist or is not a directory");
            }
        }
    }

    // Method to print current working directory
    public void pwd() {
        System.out.println(currentWorkingDirectory.toString());
    }

    // Method to create a new file
    public void touch(String[] args) {
        if (args.length != 1) {
            System.out.println("touch: needs 1 argument");
            return;
        }
        Path filePath = fullFilePath(args[0]);
        try {
            File newFile = new File(filePath.toString());
            if (!newFile.createNewFile()) {
                System.out.println("touch: file already exists");
            }
        } catch (IOException e) {
            System.out.println("touch: path does not exist");
        }
    }

    // Method to change directory to home directory
    public void cdHome() {
        currentWorkingDirectory = Paths.get(System.getProperty("user.home"));
    }

    // Method to change directory to a specified path
    public void cdPath(String arg) {
        if (Objects.equals(arg, "..")) {
            Path parent = currentWorkingDirectory.getParent();
            currentWorkingDirectory = parent == null ? currentWorkingDirectory : parent;
        } else {
            try {
                Path newCurrWorkingDir = Paths.get(fullFilePath(arg).toString()).toRealPath();
                if (!Files.isDirectory(newCurrWorkingDir)) {
                    throw new IOException();
                }
                currentWorkingDirectory = newCurrWorkingDir;
            } catch (IOException e) {
                System.out.println("cd: path does not exist or is not a directory");
            }
        }
    }

    // Method to change directory
    public void cd(String[] args) {
        if (args.length == 0) {
            cdHome();
        } else if (args.length == 1) {
            cdPath(args[0]);
        } else {
            System.out.println("cd: too many args");
        }
    }

    // Method to remove a file
    public void rm(String[] args) {
        if (args.length != 1) {
            System.out.println("rm: pass only 1 argument");
            return;
        }
        Path filePath = fullFilePath(args[0]);
        try {
            File targetFile = new File(filePath.toString());
            if (!targetFile.exists() || targetFile.isDirectory()) {
                throw new Exception("rm: " + targetFile + ": does not exist or is a directory");
            }
            targetFile.delete();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Method to print contents of a file
    public void cat(String fileName) {
        Path filePath = fullFilePath(fileName);
        fileName = filePath.toString();
        try {
            File targetFile = new File(fileName);
            Scanner fileReader = new Scanner(targetFile);

            // Read file line by line and print
            while (fileReader.hasNextLine()) {
                String line = fileReader.nextLine();
                System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("cat: " + fileName + ": file does not exist or is not a file");
        }
    }

    // Overloaded method to print contents of one or two files
    public void cat(String[] args) {
        if (args.length == 0 || args.length > 2) {
            System.out.println("error");
        } else if (args.length == 1) {
            cat(args[0]);
        } else {
            cat(args[0]);
            cat(args[1]);
        }
    }

    // Method to remove a directory
    public void rmdir(String[] args) {
        if (args.length != 1) {
            System.err.println("ERROR! rmdir takes only one argument.");
            return;
        }

        String dir = args[0];

        if (dir.equals("*")) {
            File dirFile = new File(currentWorkingDirectory.toString());
            if (dirFile.isDirectory()) {
                File[] dirFiles = dirFile.listFiles();
                if (dirFiles != null) {
                    for (File file : dirFiles) {
                        if (file.isDirectory()) {
                            if (file.delete()) {
                                System.out.println(file.getAbsolutePath() + " (Removed successfully.)");
                            } else {
                                System.err.println(file.getAbsolutePath() + "(ERROR! not removed.)");
                            }
                        } else {
                            System.err.println(file.getAbsolutePath() + "(ERROR! directory does not exist.)");
                        }
                    }
                }
            }
        } else {
            File dirFile = new File(fullFilePath(dir).toString());
            if (dirFile.exists() && dirFile.isDirectory()) {
                if (dirFile.delete()) {
                    System.out.println("Directory deleted successfully.");
                } else {
                    System.err.println("ERROR! Failed please try again.");
                }
            } else {
                System.err.println("ERROR! the directory does not exist.");
            }
        }
    }

    // Method to choose and execute command action
    public void chooseCommandAction() {
        switch (parser.getCommandName()) {
            case "echo":
                echo(parser.getArgs());
                break;
            case "pwd":
                pwd();
                break;
            case "cd":
                cd(parser.getArgs());
                break;
            case "ls":
                ls(parser.getArgs());
                break;
            case "mkdir":
                mkdir(parser.getArgs());
                break;
            case "rmdir":
                rmdir(parser.getArgs());
                break;
            case "touch":
                touch(parser.getArgs());
                break;
            case "cp":
                cp(parser.getArgs());
                break;
            case "rm":
                rm(parser.getArgs());
                break;
            case "cat":
                cat(parser.getArgs());
                break;
            case "wc":
                wc(parser.getArgs());
                break;
            case "history":
                history();
                break;
            default:
                System.out.println(parser.getCommandName() + ": command not found");
                break;
        }
    }
}




public class Main {
    // Main method to run the command-line interface
    public static void main(String[] args) {
        // Initialize terminal, scanner, and history objects
        Terminal terminal = new Terminal();
        Scanner scanner = new Scanner(System.in);
        History history = new History();

        // Continuous loop to accept and process user commands until "exit" is entered
        while (true) {
            // Display command-line prompt
            System.out.print("CLI$ ");
            // Read user input
            String line = scanner.nextLine();
            // Parse the input command
            if (!terminal.parser.parse(line)) {
                continue; // If parsing fails, continue to next iteration
            }
            // Check if user wants to exit the CLI
            if ("exit".equals(terminal.parser.getCommandName())) {
                break; // Exit the loop if "exit" command is entered
            }
            // Add command to history
            history.add(terminal.parser.getCommandName(), terminal.parser.getArgs());
            // Execute the parsed command
            terminal.chooseCommandAction();
        }
        // Print "exit" when the user exits the CLI
        System.out.println("exit");
    }
}
