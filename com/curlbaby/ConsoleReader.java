package com.curlbaby;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
 
public class ConsoleReader {
    private final CommandHistory history;
    private final UIManager uiManager;
     
    private static final String CLEAR_LINE = "\u001b[2K";
    private static final String RETURN_TO_LINE_START = "\r";
     
    private static final int ARROW_PREFIX1 = 27;  
    private static final int ARROW_PREFIX2 = 91;  
    private static final int UP_ARROW = 65;      
    private static final int DOWN_ARROW = 66;    
    private static final int RIGHT_ARROW = 67;   
    private static final int LEFT_ARROW = 68;    
    private static final int BACKSPACE = 127;   
    private static final int ENTER = 10;        
    private static final int CTRL_C = 3;        
     
    private final BlockingQueue<Integer> keyPressQueue = new LinkedBlockingQueue<>();
    private volatile boolean isReading = false;
    
    public ConsoleReader(UIManager uiManager) {
        this.history = new CommandHistory();
        this.uiManager = uiManager;
    }
    
    public void startKeyListener() {
        Thread keyListenerThread = new Thread(() -> {
            try {
                
                String[] rawCmd = {"/bin/sh", "-c", "stty raw -echo </dev/tty"};
                Runtime.getRuntime().exec(rawCmd).waitFor();
                
                while (isReading) {
                    try {
                        int key = System.in.read();
                        if (key != -1) {   
                            keyPressQueue.put(key);
                            
                             
                            if (key == CTRL_C) {
                                System.out.println();
                                System.out.println("^C");
                                System.exit(0);
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading input: " + e.getMessage());
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in key listener: " + e.getMessage());
            } finally {
                try {
                     
                    String[] resetCmd = {"/bin/sh", "-c", "stty sane </dev/tty"};
                    Runtime.getRuntime().exec(resetCmd).waitFor();
                } catch (Exception e) {
                    System.err.println("Failed to reset terminal: " + e.getMessage());
                }
            }
        });
        
        keyListenerThread.setDaemon(true);
        isReading = true;
        keyListenerThread.start();
    }

    public void stopKeyListener() {
        isReading = false;
        try {
             
            String[] resetCmd = {"/bin/sh", "-c", "stty sane </dev/tty"};
            Runtime.getRuntime().exec(resetCmd).waitFor();
        } catch (Exception e) {
            System.err.println("Failed to reset terminal: " + e.getMessage());
        }
    }
    
    public String readLine() {
        startKeyListener();
        
        StringBuilder buffer = new StringBuilder();
        int cursorPosition = 0;
        
        uiManager.printPrompt();
        
        try {
            while (true) {
                Integer key = keyPressQueue.take();
                
                if (key == ENTER) {
                    System.out.println();  
                    break;
                } else if (key == BACKSPACE) { 
                    if (cursorPosition > 0) {
                        buffer.deleteCharAt(cursorPosition - 1);
                        cursorPosition--;
                         
                        System.out.print(CLEAR_LINE + RETURN_TO_LINE_START);
                        uiManager.printPrompt();
                        System.out.print(buffer.toString());
                         
                        if (cursorPosition < buffer.length()) {
                            System.out.print("\u001b[" + (buffer.length() - cursorPosition) + "D");
                        }
                    }
                } else if (key == ARROW_PREFIX1) { 
                     
                    Integer prefix2 = keyPressQueue.take();
                    if (prefix2 == ARROW_PREFIX2) {
                        
                        Integer arrowCode = keyPressQueue.take();
                        
                        if (arrowCode == UP_ARROW) { 
                            String previousCommand = history.getPreviousCommand();
                            if (!previousCommand.isEmpty()) {
                                
                                System.out.print(CLEAR_LINE + RETURN_TO_LINE_START);
                                uiManager.printPrompt();
                                 
                                buffer = new StringBuilder(previousCommand);
                                System.out.print(buffer.toString());
                                cursorPosition = buffer.length();
                            }
                        } else if (arrowCode == DOWN_ARROW) { 
                            String nextCommand = history.getNextCommand();
                             
                            System.out.print(CLEAR_LINE + RETURN_TO_LINE_START);
                            uiManager.printPrompt();
                             
                            buffer = new StringBuilder(nextCommand);
                            System.out.print(buffer.toString());
                            cursorPosition = buffer.length();
                        } else if (arrowCode == LEFT_ARROW) { 
                            if (cursorPosition > 0) {
                                cursorPosition--;
                                System.out.print("\u001b[1D");  
                            }
                        } else if (arrowCode == RIGHT_ARROW) { 
                            if (cursorPosition < buffer.length()) {
                                cursorPosition++;
                                System.out.print("\u001b[1C");  
                            }
                        }
                    }
                } else {
                    char c = (char) key.intValue(); 
                    if (c >= 32 && c < 127) {  
                        if (cursorPosition == buffer.length()) {
                            // Append at the end
                            buffer.append(c);
                            System.out.print(c);
                        } else { 
                            buffer.insert(cursorPosition, c);
                            
                            System.out.print(buffer.substring(cursorPosition));
                            
                            
                            int charsToMoveBack = buffer.length() - cursorPosition - 1;
                            if (charsToMoveBack > 0) {
                                System.out.print("\u001b[" + charsToMoveBack + "D");
                            }
                        }
                        cursorPosition++;
                    }
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Input reading interrupted: " + e.getMessage());
        } finally {
            stopKeyListener();
        }
        
        String result = buffer.toString();
        if (!result.isEmpty()) {
            history.addCommand(result);
        }
        return result;
    }
    
    public CommandHistory getHistory() {
        return history;
    }
}