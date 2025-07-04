# cUrlBaby API Reference 🍼

![Baby API](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExZDZ2N25rM2p0djhtZ3oweXV3MzY5MDB5ZmpodmQxdXpoZWJmaWlwMCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/LHZyixOnHwDDy/giphy.gif)

*All the commands you need to make your API requests cry with joy!*

## HTTP Request Commands

### GET Request

Like a baby grabbing everything in sight - GET requests fetch data from an API.

**Syntax:**
```
get <url>
```

**Example:**
```
get jsonplaceholder.typicode.com/users/1
```

![GET request](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExbW5jemtsNXhiN29ubTY0bndjYW4zZjYxMXp1enZlb3Bjemh1c21qaiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/xT5LMzIK1AdZJ4cYW4/giphy.gif)

**Notes:**
- If the URL doesn't include "http://" or "https://", "http://" will be added automatically (because we're helpful like that)
- Response headers and body will be displayed in all their colorful glory
- JSON responses are automatically formatted to look pretty (not unlike baby photos)

### POST Request

Like delivering a gift - POST requests send data to create something new.

**Syntax:**
```
post <url>
```

**Interactive Prompts:**
- Content-Type (defaults to application/json, because who doesn't love JSON?)
- Request body (enter 'json' for the magical JSON editor)
- Additional headers (for the fancy requests)

**Example:**
```
post jsonplaceholder.typicode.com/users
```

![POST request](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExem52OWRnbDdva2JlbzVnMDFqYnV3anZ0eG41YXY0eW14b2psMzVmNSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/lXiRnUAT7ib2o/giphy.gif)

### PUT Request

Like changing a baby's outfit - PUT requests update existing data.

**Syntax:**
```
put <url>
```

**Interactive Prompts:**
- Content-Type (defaults to application/json)
- Request body (enter 'json' to open the JSON editor)
- Additional headers (optional)

**Example:**
```
put jsonplaceholder.typicode.com/users/1
```

![PUT request](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExcDU3bXF2dnd5d3BvdnVtNnh0ZHJwdHdqcWs5N215ODF3Y2I2czhjcSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/sRFEa8lbeC7zbcIZZR/giphy.gif)

### DELETE Request

Like removing that toy your baby shouldn't be playing with - DELETE requests remove data.

**Syntax:**
```
delete <url>
```

**Example:**
```
delete jsonplaceholder.typicode.com/users/1
```

![DELETE request](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExcnhhdHhpOGw5ZmlzaWlhenM4aG9oeGdheGJ1YW0yeWVuOGRlaDIzYSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/l2JhORT5IFnj6ioko/giphy.gif)

## API Group Management Commands

### Create Group

Create a new family (group) for your API requests.

**Syntax:**
```
group create <n> [description]
```

**Example:**
```
group create UserAPI "User management APIs"
```

![Create Group](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExajd0enF0dnI3eDNmaXptejFrdzlrMm1zYWdyNWVpem5yd2k0amNnMCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/l0amJzVHIAfl7jMDos/giphy.gif)

### List Groups

Show all your API request families.

**Syntax:**
```
group list
```

### Show Group

Check out all the details about one of your API groups.

**Syntax:**
```
group show <id|name>
```

**Example:**
```
group show 1
group show UserAPI
```

![Show Group](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExdnJvd20xb3JwcGVjNDF0c3d2MnkzODVkbGduMWVmMWVnNGk1N294NiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/l0HlQXlQ3nHyLMvte/giphy.gif)

### Rename Group

Decided on a better name for your baby? Rename that group!

**Syntax:**
```
group rename <id> <new_name>
```

**Example:**
```
group rename 1 UserManagementAPI
```

### Delete Group

Sometimes you just need to clean house.

**Syntax:**
```
group delete <id>
```

**Example:**
```
group delete 1
```

![Delete Group](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExNnFuaHk4bHI3aGZpcmlkbmJwMjc1c2lzNm5vZHZqNG03NWVtcTBmcCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/fGbbcXC8lbwI5FpvVH/giphy.gif)

## API Request Management Commands

### Save API Request

Save that perfect API request to use again and again (like taking a million baby photos).

**Syntax:**
```
api save <group_id|group_name> <n>
```

**Example:**
```
api save UserAPI GetUserProfile
```

**Interactive Prompts:**
- HTTP Method (GET, POST, PUT, DELETE)
- URL (where to find your data)
- Headers (optional fancy stuff)
- Request body (for POST/PUT)
- Description (so you remember what this does later)

### List API Requests

See all the API requests in a specific group.

**Syntax:**
```
api list <group_id|group_name>
```

**Example:**
```
api list UserAPI
```

![API List](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExOXUxZWRmZGtsNGxqdjdhcjZ2dDl4am93anA1ZWw3cTc2OHo3dGZxYSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/3oriO04qxVReM5rJEA/giphy.gif)

### Show API Request

Check out the details of your beautiful API request.

**Syntax:**
```
api show <id>
```

**Example:**
```
api show 5
```

### Delete API Request

Sometimes API requests need to go bye-bye.

**Syntax:**
```
api delete <id>
```

**Example:**
```
api delete 5
```

### Run Saved Request

Execute a saved request without all the typing!

**Syntax:**
```
run <id>
```

**Example:**
```
run 5
```

![Run API](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExNDJhbXJld2g5N2t5Mjh5eHFrYWRpcWs0cDliZWs0czFwemdrMnhxbSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/nbNY9AaZyKzwkn5aPF/giphy.gif)

## History Commands

### Show History

Remembering what commands you ran, because your memory is as reliable as a toddler's promise not to make a mess.

**Syntax:**
```
history
```

### Clear History

Clean slate, just like convincing a baby they never saw that cookie.

**Syntax:**
```
history clear
```

![Clear History](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExZnExcmswbHN0djFhb3dyeGR5bW56Z2xyYXFlNnIwOWRrcmdzc3YyZSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/Rl9Yqavfj2Ula/giphy.gif)

## Utility Commands

### Help

When you're as confused as a baby trying to understand taxes.

**Syntax:**
```
help
```

### Exit

Naptime for cUrlBaby.

**Syntax:**
```
exit
```

![Exit](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExbG1jNTFhbjlnYm5najg5dWk1ZW1uZWpydTk1MGh1bHJraDNhNzNzbCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/l2SpZtF9J1hNOmoFO/giphy.gif)

## JSON Editor Commands

The magical JSON editor that makes editing JSON as fun as playing with blocks!

| Command      | What It Does                                         | Baby Equivalent                                |
|--------------|------------------------------------------------------|------------------------------------------------|
| `:h`         | Shows help (because we all need help sometimes)      | Like asking "how does this toy work?"          |
| `:p`         | Previews current JSON                                | Peek-a-boo with your data                      |
| `:l`         | Lists lines with numbers                             | Counting your toys                             |
| `:e <line>`  | Edits a specific line                                | Fixing that one block in your tower            |
| `:d <line>`  | Deletes a line                                       | Throwing a toy out of the crib                 |
| `:i <line>`  | Inserts at a line                                    | Sneaking a new toy into the toybox             |
| `:c`         | Clears all content                                   | Knocking everything off the table              |
| `:f`         | Formats JSON beautifully                             | Organizing all the toys by color               |
| `:s`         | Saves and exits                                      | "I'm done playing now"                         |
| `:q`         | Quits without saving                                 | Tantrum and abandon everything                 |
| `:paste`     | Enters paste mode for multiple lines                 | Getting help from a grown-up                   |

![JSON Editor](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExNTEzZjI0bWd6c2FyOXk1aTk3ZWRmZWJhaWQ2Y2thcWs5NWlzazRpbyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/QPQ3xlJhqR1BXl89RG/giphy.gif)

## Conclusion

Now you know all the commands to make cUrlBaby do your bidding. Go forth and API with confidence!

![Happy Baby](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExOW4weG1mMzJnbno4aW96NGswbXVybGNxYm0zaHcxMjR1N2RxcXhrbiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/l0MYxpBgBn0ISC7u0/giphy.gif)

Remember: cUrlBaby makes API testing so easy, it's literally child's play!