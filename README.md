# Todo spring

## Back

### Non functional

Each user have list of todo lists
Users can:

- Create new one
- Update existing
- Delete todos
- Mark todo list as finished

### Functional

Table `TodoUser`

- id
- username
- password

Table `TodoItem`:

- id
- name
- content
- isDone
- userId

- Auth with jwt
- Fetch list of todos for a given user
- Todo actions:
  - Fetch list (no content)
  - Fetch full todo info
  - Update
  - Create
  - Mark as done
  - Delete

#### Controllers And Api:

- `AuthController`
  - POST `/api/v1/auth/signup` with only `username` and `password`
  - POST `/api/v1/auth/login` with `username` and `password`
- `TodoController`
  - GET `/api/v1/todo/all`. (fetch all)
  - GET `/api/v1/todo/{id}`. (fetch full todo by id)
  - POST `/api/v1/todo/create`. (create todo)
  - PUT `/api/v1/todo/update/{id}`. (update todo content)
  - POST `/api/v1/todo/done/{id}`. (mark todo as done, it becomes read only)
  - DELETE `/api/v1/todo/delete/{id}`. (delete todo item)

### Testing

Run with `./mvnw test` (`mvnw.cmd test` on Windows). Tests use an in-memory H2
database (`src/test/resources/application.properties`), so PostgreSQL is not needed.

- `AuthController`
  - signup
  - login
  - JWT handling
- `TodoController`
  - create todo
  - update todo
  - mark as done
  - delete todo
