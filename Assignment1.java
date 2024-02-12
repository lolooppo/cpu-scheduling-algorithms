/**********************************************************************************************************************************************************************************
    purpose : An OS interfaces with a user through CLI, A CLI is a sofware module capable of interpreting textual commands coming either from user's keyboard or from a script file.

    Commad Lines:
        1)args
        2)help
        3)date
        4)ls
        5)rls
        6)cd
        7)cp
        8)mv
        9)rm
        10)mkdir
        11)rmdir
        12)cat
        13)pwd

***********************************************************************************************************************************************************************************/




import java.io.*;
import java.nio.file.*;
import java.util.Objects;
import java.util.Scanner;

class Parser {
  String commandName;
  String[] args;

  public Parser() {
    commandName = new String();
    args = new String[0];
  }

  public boolean parse(String input) {
    if (input.isEmpty()) {
      return false;
    }
    String[] arr = input.trim().split(" +");
    commandName = arr[0];
    args = new String[arr.length - 1];
    for (int i = 1; i < arr.length; ++i) {
      args[i - 1] = arr[i];
    }
    return true;
  }

  public String getCommandName() {
    return commandName;
  }

  public String[] getArgs() {
    return args;
  }
}

class FileHandler {
  public static String readFile(String path) {
    StringBuilder data = new StringBuilder();
    try {
      FileReader reader = new FileReader(path);
      for (int c = reader.read(); c != -1; c = reader.read()) {
        data.append((char) c);
      }
      reader.close();
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    return data.toString();
  }

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

class History {
  private int nextLine;

  public History() {
    setNumberOfLines();
  }

  private void setNumberOfLines() {
    nextLine = 1;
    try {
      FileReader fileReader = new FileReader("history.csv");
      for (int c = fileReader.read(); c != -1; c = fileReader.read()) {
        if (c == '\n') {
          ++nextLine;
        }
      }
      fileReader.close();
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

  public void add(String commandName, String[] args) {
    StringBuilder line = new StringBuilder();
    line.append(nextLine++ + " " + commandName);
    for (String arg : args) {
      line.append(" " + arg);
    }
    line.append("\n");
    FileHandler.writeFile("history.csv", line.toString(), true);
  }

  public void print() {
    String data = FileHandler.readFile("history.csv");
    System.out.println(data);
  }
}

class Copy {

  public void copy(String[] args) {
    if (args.length == 0) {
      System.out.println("cp: no arguments passed");
      return;
    }
    if (args[0].equals("-r")) {
      if (args.length != 3) {
        System.out.println("cp: two directories paths must be passed");
        return;
      }
      copyDirectory(args[1], args[2]);
    } else {
      if (args.length != 2) {
        System.out.println("cp: two files paths must be passed");
        return;
      }
      copyFile(args[0], args[1]);
    }
  }

  private void copyDirectory(String src, String dest) {
    Path srcPath = Terminal.full_file_path(src), destPath = Terminal.full_file_path(dest);
    if (srcPath.getNameCount() == 0) {
      System.out.println("cp: can not copy root");
      return;
    }
    if (srcPath.equals(destPath)) {
      System.out.println("cp: can not copy directory into itself");
      return;
    }
    if (!Files.isDirectory(srcPath)) {
      System.out.println("cp: " + srcPath + ": is not a directory");
      return;
    }
    if (!Files.isDirectory(destPath)) {
      System.out.println("cp: " + destPath + ": is not a directory");
      return;
    }
    try {
      Files.walk(srcPath).forEach(path -> {
        int i = srcPath.getNameCount() - 1;
        int j = path.getNameCount();
        Path addedPath = destPath.resolve(path.subpath(i, j));
        try {
          if (Files.isDirectory(addedPath)) {
            return;
          }
          Files.copy(path, addedPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
          System.out.println(e.getMessage());
        }
      });
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private void copyFile(String src, String dest) {
    Path srcPath = Terminal.full_file_path(src), destPath = Terminal.full_file_path(dest);
    if (Files.notExists(srcPath) || Files.isDirectory(srcPath)) {
      System.out.println("cp: " + srcPath + ": does not exist or is not a file");
      return;
    }
    if ((destPath.getNameCount() > 1 && !Files.isDirectory(destPath.getParent())) || Files.isDirectory(destPath)) {
      System.out.println("cp: " + destPath + ": does not exist or is not a file");
      return;
    }
    try {
      Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

}

public class Terminal {
  Parser parser;
  static Path currentWorkingDirectory = Paths.get(System.getProperty("user.dir"));

  public static Path full_file_path(String file_name) {
    Path file_path = currentWorkingDirectory.resolve(file_name).normalize();
    return file_path;
  }

  public Terminal() {
    parser = new Parser();
  }

  public void echo(String[] args) {
    for (int i = 0; i < args.length; ++i) {
      System.out.print(args[i]);
      if (i + 1 != args.length) {
        System.out.print(" ");
      }
    }
    System.out.println();
  }

  public void ls_sorted() {
    File currentDirPath = new File(currentWorkingDirectory.toString());
    File[] files = currentDirPath.listFiles();
    if (files != null) {
      for (File file : files) {
        System.out.println(file.getName());
      }
    }
  }

  public void ls_reversed() {
    File currentDirPath = new File(currentWorkingDirectory.toString());
    File[] files = currentDirPath.listFiles();
    if (files != null) {
      for (int i = files.length - 1; i >= 0; i--) {
        System.out.println(files[i].getName());
      }
    }
  }

  public void ls(String[] args) {
    if (args.length == 0) {
      ls_sorted();
    } else if (args[0].equals("-r")) {
      ls_reversed();
    } else {
      ls_sorted();
    }
  }

  public void wc(String[] args) {
    if (args.length < 1) {
      System.out.println("wc: no arguments passed");
      return;
    } else if (args.length > 1) {
      System.out.println("wc: many arguments passed");
      return;
    }
    Path path = full_file_path(args[0]);
    File file = new File(path.toUri());
    if (!file.exists()) {
      System.out.println("wc: " + args[0] + ": file does not exist");
      return;
    }
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      int lineCount = 0;
      int wordCount = 0;
      int charCount = 0;

      String line;

      while ((line = reader.readLine()) != null) {
        lineCount++;
        charCount += line.length();
        String[] words = line.split("\\s+");
        wordCount += words.length;
      }

      String output = lineCount + " " + wordCount + " " + charCount + " " + path.getFileName();
      System.out.println(output);
    } catch (IOException e) {
      System.out.println("Error reading the file: " + e.getMessage());
    }
  }

  public void history() {
    History history = new History();
    history.print();
  }

  public void cp(String[] args) {
    Copy cpy = new Copy();
    cpy.copy(args);
  }

  public void mkdir(String[] args) {
    for (String arg : args) {
      Path path = full_file_path(arg);
      int pathLength = path.getNameCount();
      Path parentPath = path.getParent();
      if (pathLength > 1 && !Files.isDirectory(parentPath)) {
        System.out.println("mkdir: " + parentPath + ": does not exist or is not a directory");
        continue;
      }
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

  public void pwd() {
    System.out.println(currentWorkingDirectory.toString());
  }

  public void touch(String[] args) {
    if (args.length != 1) {
      System.out.println("touch: needs 1 argument");
      return;
    }
    Path file_path = full_file_path(args[0]);
    try {
      File new_file = new File(file_path.toString());
      if (!new_file.createNewFile()) {
        System.out.println("touch: file already exists");
      }
    } catch (IOException e) {
      System.out.println("touch: path does not exist");
    }
  }

  public void cd_home() {
    currentWorkingDirectory = Paths.get(System.getProperty("user.home"));
  }

  public void cd_path(String arg) {
    if (Objects.equals(arg, "..")) {
      Path parent = currentWorkingDirectory.getParent();
      currentWorkingDirectory = parent == null ? currentWorkingDirectory : parent;
    } else {
      try {
        Path new_curr_wor_dir = Paths.get(full_file_path(arg).toString()).toRealPath();
        if (!Files.isDirectory(new_curr_wor_dir)) {
          throw new IOException();
        }
        currentWorkingDirectory = new_curr_wor_dir;
      } catch (IOException e) {
        System.out.println("cd: path does not exist or is not a directory");
      }
    }
  }

  public void cd(String[] args) {
    if (args.length == 0) {
      cd_home();
    } else if (args.length == 1) {
      cd_path(args[0]);
    } else {
      System.out.println("cd: too much args");
    }
  }

  public void rm(String[] args) {
    if (args.length != 1) {
      System.out.println("rm: pass only 1 argument");
      return;
    }
    Path file_path = full_file_path(args[0]);
    try {
      File target_file = new File(file_path.toString());
      if (!target_file.exists() || target_file.isDirectory()) {
        throw new Exception("rm: " + target_file + ": does not exist or is a directory");
      }
      target_file.delete();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  public void cat(String file_name) {
    Path file_path = full_file_path(file_name);
    file_name = file_path.toString();
    try {
      File target_file = new File(file_name);
      Scanner file_reader = new Scanner(target_file);

      //get line by line
      while (file_reader.hasNextLine()) {
        String line = file_reader.nextLine();
        System.out.println(line);
      }
    } catch (FileNotFoundException e) {
      System.out.println("cat: " + file_name + ": file does not exist or is not a file");
    }
  }

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

  public void rmdir(String[] args) {
    if (args.length != 1) {
      System.err.println("ERROR! rmdir takes only one argument.");
      return;
    }

    String dir = args[0];

    if (dir.equals("*")) {
      File dir_file = new File(currentWorkingDirectory.toString());
      if (dir_file.isDirectory()) {
        File[] dir_files = dir_file.listFiles();
        if (dir_files != null) {
          for (File file : dir_files) {
            if (file.isDirectory()) {
              if (file.delete()) {
                System.out.println(file.getAbsolutePath() + " (Removed sucessfully.)");
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
      File dir_file = new File(full_file_path(dir).toString());
      if (dir_file.exists() && dir_file.isDirectory()) {
        if (dir_file.delete()) {
          System.out.println("Directory deleted successfully.");
        } else {
          System.err.println("ERROR! Failed please try again.");
        }
      } else {
        System.err.println("ERROR! the directory does not exist.");
      }
    }
  }

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

  public static void main(String[] args) {
    Terminal t = new Terminal();
    Scanner in = new Scanner(System.in);
    History history = new History();
    while (true) {
      System.out.print("CLI$ ");
      String line = in.nextLine();
      if (!t.parser.parse(line)) {
        continue;
      }
      if (t.parser.getCommandName().equals("exit")) {
        break;
      }
      history.add(t.parser.getCommandName(), t.parser.getArgs());
      t.chooseCommandAction();
    }
    System.out.println("exit");
  }
}
