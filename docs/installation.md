# cUrlBaby Installation Guide

![Baby Installation](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExYjRmMXczcnd0aGd6enJmaDR0ZTRwN2xuMG4zc2ZrcWZuOWMzaGFlaiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/3o7TKDEhasCwJD9bDG/giphy.gif)

*When installing cUrlBaby is easier than assembling baby furniture...*

## Prerequisites

Before you can rock the cUrlBaby, you'll need:

- Java 8 or higher (JDK or JRE)
- Basic terminal skills
- A desire to make API calls look fabulous

## Installation Methods

### Method 1: The "I Want It Now" Approach

This is for the impatient devs who just want to get things running ASAP!

```bash
# Clone the repository
git clone https://github.com/yourusername/curlbaby.git

# Enter the project directory
cd curlbaby

# Make the script executable
chmod +x curlbaby.sh

# Run it!
./curlbaby.sh
```

![That was easy](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExOWt0MDVvZjNlZXlpMmptN2N0aml0aWNmNGNieHM4eTd0YTJibmxvZCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/3o7TKqnN349CAN3E1O/giphy.gif)

### Method 2: The "I'm a Responsible Developer" Approach

For those who like to understand what they're installing:

1. **Download the Source**:
   ```bash
   git clone https://github.com/yourusername/curlbaby.git
   ```

2. **Review the Code** (optional, but hey, security matters):
   ```bash
   cd curlbaby
   # Take a look at what you're about to run
   cat curlbaby.sh
   ```

3. **Make It Executable**:
   ```bash
   chmod +x curlbaby.sh
   ```

4. **Run It**:
   ```bash
   ./curlbaby.sh
   ```

## System-Specific Notes

### Linux Users

You're good to go! The terminal is your natural habitat anyway.

### macOS Users

Should work out of the box! If you encounter permission issues:

```bash
sudo chmod +x curlbaby.sh
```

![Mac Users](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMjdqdWV5NXUxOGp3ejVxdGE1ZTU3dmU2aWhubDhmNTJsanc2Y3plbiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/l44QzsOLXxcVKe4Mw/giphy.gif)

### Windows Users

You have a couple of options:

1. **Using WSL (Windows Subsystem for Linux)**:
   - Install WSL if you haven't already
   - Follow the Linux instructions above

2. **Using Git Bash or Similar**:
   - Install Git Bash
   - Clone the repository
   - Run the bash script like on Linux/macOS

3. **Using PowerShell**:
   - You'll need to modify the execution slightly:
   ```powershell
   # Navigate to directory
   cd path\to\curlbaby
   
   # Run Java directly (may need to adjust)
   java -cp "target\classes;lib\*" com.curlbaby.CurlBabyApp
   ```

![Windows users trying to run bash scripts](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExcjRhbmhuY2J6NHRjaDJheGZsaGIxYWN5ZnkwY2Fnc2I0c3VlZHQyeiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/DHqth0FVuWCcM/giphy.gif)

## Verification

To verify your installation worked correctly:

```bash
./curlbaby.sh

# You should see the welcome screen:
#  
# ┌───────────────────────────────────────────────────┐
# │                                                   │
# │                 🍼  cUrlBaby  🍼                  │
# │                                                   │
# │         API Testing from the Command Line         │
# │                                                   │
# │             ~ Make API Calls Simple ~             │
# │                                                   │
# └───────────────────────────────────────────────────┘
```

Try a simple command:

```
> get jsonplaceholder.typicode.com/todos/1
```

If you see a nicely formatted JSON response, you're all set!

![Success!](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExb3Jvc2plbzE1aGN4eHFtendkd256bmxzNWd5N3c5cjU5ZTkwZmZ2diZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/ely3apij36BJhoZ234/giphy.gif)

## Troubleshooting

### "Java not found" Error

If you see `Error: Java is not installed or not in PATH`:

1. Install Java (JRE 8 or later)
2. Make sure Java is in your PATH
3. Verify with `java -version`

### Permission Denied

```bash
# Try this
chmod +x curlbaby.sh
```

### Nothing is Colorful

Some terminal emulators don't support ANSI colors. Try upgrading your terminal or using one that supports colors like:
- iTerm2 (macOS)
- Windows Terminal (Windows)
- Terminator (Linux)

### Other Issues

![Have you tried turning it off and on again?](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExN25vc2V5ZmV1cmppZ3hvaTJwc2xsa3g2eGU0anQ5eTJweGxmeGNrdyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/FspLvJQlQACXu/giphy.gif)

## Next Steps

Now that you've got cUrlBaby installed:

- [Check out the examples](examples.md)
- [Learn about all the commands](api-reference.md)
- [Setup API Groups for your project](api-groups.md)

Happy API Testing!