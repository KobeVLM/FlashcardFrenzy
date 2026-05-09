# SYSTEM DESIGN DOCUMENT
### ⚠️ DO NOT write any code without reading this document first.

---

## Project Overview

| Field | Value |
|-------|-------|
| **Project Name** | Flashcard Frenzy |
| **Domain** | Education / EdTech |
| **Version** | 1.0 |
| **Architecture Pattern** | MVP (Model–View–Presenter) — Vertical Slice |

> **Architecture Note:**
> This project uses the **MVP (Model–View–Presenter)** pattern, NOT MVVM.
> Code is organized using a **Vertical Slicing** approach — each feature (auth, deck,
> flashcard, quiz, admin) is self-contained in its own package, holding its own
> Model, Presenter (business logic), Controller (View), Repository, and DTOs.
> Do NOT organize by layer (e.g., a single `controllers/` folder for everything).

---

## Technology Stack

### Backend
- **Language:** Java 17
- **Framework:** Spring Boot 3.x
- **Security:** Spring Security (JWT-based)
- **ORM:** Spring Data JPA
- **Build Tool:** Maven
- **Dependencies in use:**
  - Spring Data JPA Starter
  - Spring Boot Validation Starter
  - Spring Boot Web MVC Starter
  - Spring Boot Security Starter
  - PostgreSQL JDBC Driver
  - Project Lombok
  - JJWT (io.jsonwebtoken) — for JWT generation and validation

### Database
- **Engine:** PostgreSQL 14+
- **Hosted via:** Supabase
- **Query constraint:** All queries must complete within **500ms**

### Web Frontend
- **Framework:** React 18
- **Language:** TypeScript
- **Styling:** Tailwind CSS
- **HTTP Client:** Axios

### Mobile
- **Language:** Kotlin
- **UI:** Kotlin Android Views (no Jetpack Compose)
- **HTTP Client:** Retrofit
- **Min Android API Level:** 24 (Android 7.0+)

### Deployment
- **Backend:** Render (preferred) or Railway
- **Web Frontend:** Vercel or Netlify
- **Mobile:** APK distribution

> **Deployment Note:**
> The backend and frontend are deployed **separately** as two independent services.
> The Spring Boot API is deployed on Render and exposes a public URL
> (e.g., `https://flashcard-frenzy.onrender.com`). The React app is deployed on
> Vercel and communicates with the backend via that URL. The frontend does NOT
> run on the same server as the backend.

---

## Architecture

- **Pattern:** Three-tier architecture (Backend API → Web Frontend + Android App)
- **Design:** MVP — Vertical Slice per feature
- **Communication:** RESTful APIs, JSON only
- **Base URL:** `/api/v1`
- **Auth mechanism:** Bearer token (JWT) in `Authorization` header

### Vertical Slice Package Structure (Backend)
```
com.flashcardfrenzy/
├── auth/           ← User entity, AuthController, AuthPresenter, AuthDto, UserRepository
├── deck/           ← Deck entity, DeckController, DeckPresenter, DeckDto, DeckRepository
├── flashcard/      ← Flashcard entity, FlashcardController, FlashcardPresenter, FlashcardDto, FlashcardRepository
├── quiz/           ← QuizResult entity, QuizController, QuizPresenter, QuizDto, QuizResultRepository
├── admin/          ← AdminController, AdminPresenter, AdminDto
└── common/
    ├── response/   ← ApiResponse (shared envelope)
    ├── exception/  ← GlobalExceptionHandler, custom exceptions
    └── security/   ← JwtUtil, JwtAuthFilter, SecurityConfig
```

### MVP Role Mapping
| MVP Role | Backend Equivalent | Responsibility |
|----------|--------------------|----------------|
| **Model** | JPA Entity + Repository | Data structure and database access |
| **Presenter** | `*Presenter.java` (Service layer) | All business logic and data transformation |
| **View** | `*Controller.java` (REST Controller) | HTTP routing, status codes, response wrapping only |

---

## API Constraints

### Standard Response Structure
All responses — success and error alike — must follow this exact structure:

```json
{
  "success": true,
  "data": { },
  "error": null,
  "timestamp": "2026-05-09T10:00:00Z"
}
```

On error:
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "AUTH-001",
    "message": "Invalid credentials",
    "details": null
  },
  "timestamp": "2026-05-09T10:00:00Z"
}
```

> **Rule:** `data` must be `null` when `success` is `false`.
> `error` must be `null` when `success` is `true`.

---

## Endpoints

### Authentication
| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/auth/register` | No | Register a new user account |
| POST | `/auth/login` | No | Login and receive a JWT token |
| POST | `/auth/logout` | Yes | Logout (client discards token; stateless) |
| POST | `/auth/refresh` | No (uses refresh token) | Refresh an expired JWT *(see note below)* |
| GET | `/auth/me` | Yes | Get the currently authenticated user's profile |

> **`/auth/logout` Note:** JWT is stateless. The server does not maintain a
> session, so logout is handled client-side by discarding the token from storage.
> This endpoint exists to satisfy the API contract and can be used to log the event
> server-side if needed.

> **`/auth/refresh` Note:** When the access token expires, the client receives
> `AUTH-002`. Without a refresh endpoint, the user would be forced to re-login
> every time. This endpoint accepts the refresh token and returns a new access token.
> Refresh tokens should have a longer expiry (e.g., 7 days) and be stored securely.

### Decks
| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| GET | `/decks` | No | List all decks. Supports `?search=keyword` for title/category filtering |
| GET | `/decks/my` | Yes | List only the currently authenticated user's decks |
| GET | `/decks/{id}` | No | Get a single deck by ID |
| POST | `/decks` | Yes | Create a new deck |
| PUT | `/decks/{id}` | Yes (owner only) | Update a deck |
| DELETE | `/decks/{id}` | Yes (owner only) | Delete a deck and all its flashcards |

> **`DELETE /decks/{id}` Note:** This endpoint was missing from the original SDD.
> Without it, users can create decks but never delete them. Cascade deletion of
> all associated flashcards must also occur when a deck is deleted.

> **`GET /decks/my` Note:** `GET /decks` returns all public decks globally.
> The frontend's "My Decks" page needs a scoped endpoint. This was missing from
> the original SDD.

> **`?search=keyword` Note:** Applies case-insensitive partial match on `title`
> and `category` fields. Example: `GET /api/v1/decks?search=math`

### Flashcards
| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| GET | `/decks/{id}/cards` | No | Get all flashcards in a deck |
| GET | `/cards/{cardId}` | No | Get a single flashcard by ID |
| POST | `/decks/{id}/cards` | Yes (deck owner only) | Add a flashcard to a deck |
| PUT | `/cards/{cardId}` | Yes (card owner only) | Update a flashcard |
| DELETE | `/cards/{cardId}` | Yes (card owner only) | Delete a flashcard |

> **Auth consistency note:** `GET /decks` and `GET /decks/{id}` are public, so
> it is intentional that `GET /decks/{id}/cards` is also public. If a deck is
> publicly browsable, its cards should be too. Only write operations require auth.

> **`GET /cards/{cardId}` Note:** This was missing from the original SDD.
> Fetching a single card by ID is a standard REST expectation and needed when
> the frontend links to or pre-fills an edit form for a specific card.

### Quiz
| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/quizzes/results` | Yes | Submit a quiz result (score + time) |
| GET | `/quizzes/history` | Yes | Get the authenticated user's quiz history |

> **Quiz session note:** There is no `/quizzes/start` endpoint. Quiz sessions
> are managed entirely client-side. The frontend fetches cards via
> `GET /decks/{id}/cards`, runs the quiz locally, then submits the final
> result via `POST /quizzes/results`. This is intentional to keep the backend stateless.

### Admin
| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| GET | `/admin/stats` | Admin only | Get platform-wide stats (users, decks, cards, quizzes) |
| GET | `/admin/users` | Admin only | List all registered users |
| DELETE | `/admin/users/{id}` | Admin only | Delete a user account |

---

## HTTP Status Codes

| Code | Meaning | When to Use |
|------|---------|-------------|
| 200 | OK | Successful GET, PUT, DELETE |
| 201 | Created | Successful POST that creates a resource |
| 400 | Bad Request | Validation failure, malformed request body |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Valid token but insufficient permissions |
| 404 | Not Found | Resource does not exist |
| 409 | Conflict | Duplicate entry (e.g., email already registered) |
| 500 | Internal Server Error | Unexpected server-side failure |

---

## Error Codes

| Code | Meaning | HTTP Status |
|------|---------|-------------|
| AUTH-001 | Invalid credentials | 401 |
| AUTH-002 | Token expired | 401 |
| AUTH-003 | Insufficient permissions | 403 |
| VALID-001 | Validation failed (see `details` for field errors) | 400 |
| DB-001 | Resource not found | 404 |
| DB-002 | Duplicate entry | 409 |
| SYSTEM-001 | Internal server error | 500 |

> **`VALID-001` note:** When this error code is returned, the `details` field
> in the error object must contain a map of field names to their specific
> validation error messages. Example:
> ```json
> "details": { "email": "Must be a valid email", "password": "Minimum 8 characters" }
> ```

---

## Database Schema

### Relationships
- `users` → `decks`: One-to-Many (one user owns many decks)
- `decks` → `flashcards`: One-to-Many (one deck has many cards; cascade delete)
- `users` → `quiz_results`: One-to-Many
- `quiz_results` → `decks`: Many-to-One

### Table Definitions

```sql
users (
  id             BIGSERIAL PRIMARY KEY,
  email          VARCHAR(255) NOT NULL UNIQUE,
  password_hash  VARCHAR(255) NOT NULL,
  full_name      VARCHAR(255) NOT NULL,
  role           VARCHAR(20)  NOT NULL DEFAULT 'USER',  -- 'USER' or 'ADMIN'
  created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
)

decks (
  id           BIGSERIAL PRIMARY KEY,
  user_id      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  title        VARCHAR(255) NOT NULL,
  category     VARCHAR(100),
  description  TEXT,
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
)

flashcards (
  id          BIGSERIAL PRIMARY KEY,
  deck_id     BIGINT  NOT NULL REFERENCES decks(id) ON DELETE CASCADE,
  question    TEXT    NOT NULL,
  answer      TEXT    NOT NULL,
  tags        VARCHAR(500),   -- Comma-separated string. e.g., "math,algebra,equations"
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
)

quiz_results (
  id          BIGSERIAL PRIMARY KEY,
  user_id     BIGINT  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  deck_id     BIGINT  NOT NULL REFERENCES decks(id) ON DELETE CASCADE,
  score       INTEGER NOT NULL CHECK (score >= 0 AND score <= 100),
  time_spent  INTEGER,          -- Duration in seconds
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
)
```

> **Schema corrections from original:**
> - `quiz_results.date_taken` has been renamed to `created_at` for consistency
>   with all other tables. All tables now use `created_at` as the timestamp column.
> - `ON DELETE CASCADE` has been explicitly defined for foreign keys.
> - `tags` format is now defined: comma-separated string (e.g., `"math,algebra"`).
>   This avoids ambiguity in implementation.
> - Column types and constraints have been made explicit for clarity.

### User Roles
| Role | Description |
|------|-------------|
| `USER` | Standard learner — can manage their own decks, cards, and quizzes |
| `ADMIN` | Platform administrator — can access `/admin/**` endpoints |

---

## Security Constraints

- All communications must use **HTTPS** — HTTP must be rejected or redirected
- Passwords must be hashed with **bcrypt** (salt rounds = 12)
- Admin-prefixed endpoints (`/admin/**`) must enforce **ROLE_ADMIN** role verification
- Must implement **SQL injection prevention** — use parameterized queries via JPA; never concatenate user input into queries
- Must implement **XSS protection** — sanitize or escape user input on the frontend; use `Content-Security-Policy` headers on the backend
- JWT tokens must be used for all authenticated sessions
- JWT access token expiry: **24 hours** (`86400000ms`)
- JWT refresh token expiry: **7 days**
- **Rate limiting** must be applied to `POST /auth/login` to prevent brute-force attacks (e.g., max 10 attempts per minute per IP)
- JWT secret key must be **at least 256 bits** (32+ characters) and stored as an environment variable — never hardcoded in source code
- Database credentials and secrets must be stored in **environment variables**, not in `application.properties` committed to version control

---

## Features In Scope

- User registration and authentication (email/password only)
- JWT-based session management with token refresh
- Deck listing with search functionality (`?search=keyword`)
- Authenticated user's own deck listing (`GET /decks/my`)
- Flashcard CRUD (add, update, delete, single fetch)
- Deck deletion with cascading flashcard removal
- Quiz session (client-managed) with score and time submission
- Quiz history per user
- Responsive web interface
- Native Android mobile application
- Admin panel (user management + platform statistics)

## Features Out of Scope (Do NOT implement)

- Social media / OAuth login
- Payment gateway integration
- Push notifications
- Advanced collaboration tools
- Real-time features (WebSockets, SSE)
- Email verification on registration
- Password reset via email

---

## Capacity Constraints

- Must support **100 concurrent users**
- All database queries must complete within **500ms**

## Browser & OS Compatibility

- **Browsers:** Chrome, Firefox, Safari, Edge (latest 2 versions)
- **OS:** Windows 10+
- **Screen sizes:** Mobile (360px+), Tablet (768px+), Desktop (1024px+)

---

## Environment Variables Reference

The following environment variables must be configured in the deployment
environment (Render dashboard). Never commit real values to version control.

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | Supabase PostgreSQL JDBC URL | `jdbc:postgresql://...` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `your_password` |
| `JWT_SECRET` | JWT signing secret (32+ chars) | `your_secret_key` |
| `JWT_EXPIRATION` | Access token expiry in ms | `86400000` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins | `https://your-app.vercel.app` |
