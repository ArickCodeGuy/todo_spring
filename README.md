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
- user_id

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
  - `/signup` with only `username` and `password`
  - `/auth` with `username` and `password`
- `TodoController`
  - GET `/api/v1/todo/all`. (fetch all)
  - GET `/api/v1/todo/{id}`. (fetch full todo by id)
  - POST `/api/v1/create`. (create todo)
  - POST `/api/v1/todo/update/{id}`. (update todo content)
  - POST `/api/v1/todo/done/{id}`. (mark todo as read only)
  - DELETE `/api/v1/todo/delete/{id}`. (delete todo item)

### Testing

Optional

Test REST `api`

- `AuthController`
  - signup
  - login
- `TodoController`
  - create todo
  - update todo
  - delete todo
