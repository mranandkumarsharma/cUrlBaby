# cUrlBaby Examples Guide

![Baby playing with keyboard](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExczI3d3Q2OHI0MDJuZjdnYXd0aHA1MGszc3I5eDlsYXBub29wcXU2eiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/IgxtITEF3o0iA/giphy.gif)

*Let's put cUrlBaby to work on some real-world examples!*

## Basic HTTP Requests

### GET Request

Fetch user data from a public API:

```bash
> get jsonplaceholder.typicode.com/users/1
```

![Mind blown](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExdjc2eDRkbGc3dXdzOTdlNmJwOTRwbXRheWpkbm92YmxhbTl6bmxwZCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/26ufdipQqU2lhNA4g/giphy.gif)

### POST Request

Create a new user:

```bash
> post jsonplaceholder.typicode.com/users

# When prompted for Content-Type, press Enter to use the default application/json
# When prompted for request body, type 'json' to open the JSON editor
```

In the JSON editor, enter:

```json
{
  "name": "Baby McBabyface",
  "email": "baby@example.com",
  "username": "curlbaby"
}
```

Type `:s` to save and exit the editor.

### PUT Request

Update an existing user:

```bash
> put jsonplaceholder.typicode.com/users/1
```

In the JSON editor, enter:

```json
{
  "id": 1,
  "name": "Modified User",
  "email": "modified@example.com"
}
```

### DELETE Request

Delete a user:

```bash
> delete jsonplaceholder.typicode.com/users/1
```

![Delete all the things](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExaGl1a3R5ODJnaGNrMzMwMmpzaTY5MzluNXR4YXRscnVrcm41Zm5jZiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/5xaOcLCBzBw4PUY7JSM/giphy.gif)

## Working with API Groups

### Creating a Group for Your API Tests

```bash
> group create WeatherAPI "Weather forecast API endpoints"
```

### Saving API Requests

Let's save a weather API request:

```bash
> api save WeatherAPI GetCurrentWeather
```

When prompted:
- HTTP Method: `GET`
- URL: `api.weatherapi.com/v1/current.json?key=YOUR_API_KEY&q=London`
- Add header? `n`
- Description: `Get current weather for London`

### Running Saved Requests

```bash
> group list
# Note the ID of your WeatherAPI group

> api list WeatherAPI
# Note the ID of your GetCurrentWeather request

> run 1
# Replace 1 with the actual ID of your saved request
```

![Weather API](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExbHV3ZXVzY3l6bTExdnBhNDQ5anRiNHR6ZTI5ZXF5cGYzenB1dGFpdCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/xUOwGoNa2uX2M3SJoI/giphy.gif)

## JSON Editor Tricks

When working with the JSON editor for request bodies:

### Multi-line JSON Input

```bash
> post api.example.com/data
# Choose 'json' at the body prompt
```

In the editor, enter `:paste` to start paste mode, then paste your JSON:

```json
{
  "complex": {
    "nested": {
      "data": [1, 2, 3, 4],
      "options": {
        "enabled": true,
        "visible": false
      }
    }
  },
  "name": "Example Object"
}
```

Type a single `.` on a new line to end paste mode.

![JSON Editor](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExcGVjdGJqcGU3MTd1cmh0ejkzbHp6NXZhc3drd3hyZHF3eG52dG9xcyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/mGK1g88HZRa2FlKGkv/giphy.gif)

### Formatting Ugly JSON

If you've pasted in unformatted JSON, use `:f` to auto-format it to be more readable.

## Real-World Example: GitHub API

Let's query the GitHub API to get repository information:

```bash
> get api.github.com/repos/octocat/hello-world
```

To add an authorization header for private repos:

```bash
> get api.github.com/user/repos
# When asked "Add header? (y/n):", enter y
# Header name: Authorization
# Header value: token YOUR_PERSONAL_ACCESS_TOKEN
```

![GitHub API](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExZnRjZ2RmM2d3c2E2ZXpxMGgzeGM1ODRqenhrdHlsYjBlODZtdnZwZSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/du3J3cXyzhj75IOgvA/giphy.gif)

## Organizing Multiple API Endpoints

Create groups for different projects:

```bash
> group create AuthService "Authentication API endpoints"
> group create UserService "User management endpoints"
> group create ContentAPI "Content delivery endpoints"
```

Save commonly used requests:

```bash
> api save AuthService Login
# Method: POST
# URL: api.example.com/auth/login
# Body: {"username": "demo", "password": "demo123"}

> api save UserService GetProfile
# Method: GET 
# URL: api.example.com/users/profile
# Header: Authorization: Bearer ${TOKEN}
```

![Organization](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExbHlhZ3RoZWc1M2VlcThyd3ZjNDUzYjZ0OHEwZTI2cjV2ZWc5b2JhdyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/1msDUoLUGfuCRFuZlJ/giphy.gif)

## Command History Tricks

Use the up/down arrow keys to navigate through your command history. This is super useful for repeating or modifying previous commands!

```bash
> get api.example.com/v1/resource
# Press up arrow to get this command back
# Edit and run again
```

## Tips & Tricks

1. **Quick JSON Formatting**: If you receive an unformatted JSON response from an external source, you can use cUrlBaby to format it nicely! Just save it to a text file and use the JSON editor's formatting capability.

2. **Testing Authentication Flows**:
   - Save login request in one API entry
   - Extract token from response
   - Use token in subsequent requests

3. **URL Parameters**: For GET requests with many parameters, build the URL carefully with proper encoding:
   ```
   > get api.example.com/search?q=test+query&limit=10&offset=0
   ```

4. **API Development Workflow**:
   - Use cUrlBaby to test your API as you develop it
   - Save common test cases as API entries
   - Quickly verify fixes by re-running saved requests

![Developer workflow](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExcnZiNjRkNHhkOG05MGk3ZXh6MnZoZGsxNTVueGN0Zm10ZTV3NnNtZyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/hrRJ41JB2zlgZiYcCw/giphy.gif)

## Conclusion

cUrlBaby makes API testing so easy, it's like child's play!

![Happy dancing](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExZnlqM3phYzB2MjJpNGhyM3JlMm9yMnVlbDZxYXFjb3JnbTV1YTN5YiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/l4JySAWfMaY7w88sU/giphy.gif)

Now go forth and test APIs with the power of cUrlBaby! Check the [API Reference](api-reference.md) for more details on all available commands.