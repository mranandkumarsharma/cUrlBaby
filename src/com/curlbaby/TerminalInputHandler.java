package com.curlbaby;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TerminalInputHandler {
    private final UIManager uiManager;
    private final CommandHistoryDatabase historyDb;
    
    private static final int ESC = 27;
    private static final int BRACKET = 91;
    private static final int UP_ARROW = 65;
    private static final int DOWN_ARROW = 66;
    private static final int BACKSPACE = 127;
    private static final int DELETE = 126;
    private static final int ENTER = 10;
    
    private final BlockingQueue<Integer> keyPressQueue = new LinkedBlockingQueue<>();
    private volatile boolean isReading = false;
    
    public TerminalInputHandler(UIManager uiManager, CommandHistoryDatabase historyDb) {
        this.uiManager = uiManager;
        this.historyDb = historyDb;
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
                        }
                    } catch (IOException | InterruptedException e) {
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
                        
                        System.out.print("\u001b[2K\r");
                        uiManager.printPrompt();
                        System.out.print(buffer.toString());
                        
                        if (cursorPosition < buffer.length()) {
                            System.out.print("\u001b[" + (buffer.length() - cursorPosition) + "D");
                        }
                    }
                } else if (key == ESC) {
                    Integer next = keyPressQueue.poll();
                    if (next != null && next == BRACKET) {
                        Integer arrowKey = keyPressQueue.poll();
                        if (arrowKey == null) continue;
                        
                        if (arrowKey == UP_ARROW) {
                            String previousCommand = historyDb.getPreviousCommand();
                            if (!previousCommand.isEmpty()) {
                                buffer = new StringBuilder(previousCommand);
                                cursorPosition = buffer.length();
                                
                                System.out.print("\u001b[2K\r");
                                uiManager.printPrompt();
                                System.out.print(buffer.toString());
                            }
                        } else if (arrowKey == DOWN_ARROW) {
                            String nextCommand = historyDb.getNextCommand();
                            buffer = new StringBuilder(nextCommand);
                            cursorPosition = buffer.length();
                            
                            System.out.print("\u001b[2K\r");
                            uiManager.printPrompt();
                            System.out.print(buffer.toString());
                        }
                    }
                } else {
                    char c = (char) key.intValue();
                    if (c >= 32 && c < 127) {
                        if (cursorPosition == buffer.length()) {
                            buffer.append(c);
                            System.out.print(c);
                        } else {
                            buffer.insert(cursorPosition, c);
                            System.out.print(buffer.substring(cursorPosition));
                            System.out.print("\u001b[" + (buffer.length() - cursorPosition - 1) + "D");
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
            historyDb.addCommand(result);
        }
        return result;
    }
}