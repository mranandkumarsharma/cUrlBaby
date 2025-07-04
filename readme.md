# cUrlBaby 🍼

A lightweight HTTP client with a colorful CLI interface for API testing - making API calls as simple as child's play!

## Features

- 🚀 HTTP request execution (GET, POST, PUT, DELETE)
- 🎨 Beautiful command-line interface with colors
- 📋 Response headers display
- 🔍 JSON response formatting
- 🛡️ Error handling
- 📁 API Group Management
- 📝 Command history with navigation

## Quick Start

### Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/yourusername/curlbaby.git
   cd curlbaby
   ```

2. Make the script executable:
   ```bash
   chmod +x curlbaby.sh
   ```

3. Run cUrlBaby:
   ```bash
   ./curlbaby.sh
   ```

### Example Usage

```bash
> get jsonplaceholder.typicode.com/users/1

🔄 Executing GET request to http://jsonplaceholder.typicode.com/users/1

📊 Status: 200 OK

📋 Response Headers:
  Cache-Control: max-age=43200
  Content-Type: application/json; charset=utf-8
  ... (more headers)

📄 Response Body:
{
  "id": 1,
  "name": "Leanne Graham",
  "username": "Bret",
  "email": "Sincere@april.biz",
  ...
}
```

## Available Commands

### Basic Commands
- `help` - Display help information
- `exit` - Exit the application

### Request Commands
- `get <url>` - Execute a GET request to the specified URL
- `post <url>` - Execute a POST request with interactive body editor
- `put <url>` - Execute a PUT request with interactive body editor
- `delete <url>` - Execute a DELETE request to the specified URL

### API Group Management Commands
- `group create <name>` - Create a new API group
- `group list` - List all API groups
- `group show <id|name>` - Show details of a specific group
- `group rename <id> <new_name>` - Rename a group
- `group delete <id>` - Delete a group

### API Request Management Commands
- `api save <group_id|group_name> <name>` - Save current or new API request to a group
- `api list <group_id|group_name>` - List all APIs in a group
- `api show <id>` - Show details of a specific API request
- `api delete <id>` - Delete an API request
- `run <id>` - Execute a saved API request

### History Commands
- `history` - Display command history
- `history clear` - Clear command history

## JSON Editor Commands

When using the JSON editor for request bodies:

- `:h` - Help
- `:p` - Preview current JSON
- `:l` - List all lines with numbers
- `:e <line>` - Edit specific line number
- `:d <line>` - Delete specific line number
- `:i <line>` - Insert at specific line number
- `:c` - Clear all content
- `:f` - Format JSON
- `:s` - Save and exit
- `:q` - Quit without saving
- `:paste` - Enter paste mode (end with a line containing only '.')

## Directory Structure

```
curlbaby/
├── curlbaby.sh
├── README.md
└── src/
    └── main/
        └── java/
            └── com/
                └── curlbaby/
                    ├── ApiCollectionCommands.java
                    ├── ApiCollectionManager.java
                    ├── CommandHistoryDatabase.java
                    ├── CommandProcessor.java
                    ├── CommandHistory.java
                    ├── ConsoleReader.java
                    ├── CurlBabyApp.java
                    ├── HttpRequestHandler.java
                    ├── JsonFormatter.java
                    ├── SimpleJsonEditor.java
                    ├── TerminalInputHandler.java
                    └── UIManager.java
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.