# SYSTEM DESIGN CONSTRAINTS
### ⚠️ DO NOT write any code without reading this document first.
 
---
 
## Project Overview
**Project Name:** Flashcard Frenzy
**Domain:** Education / EdTech
**Version:** 1.0
 
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
  - PostgreSQL JDBC Driver
  - Project Lombok
 
### Database
- **Engine:** PostgreSQL 14+
- **Hosted via:** Supabase
- **Query constraint:** All queries must complete within **500ms**
 
### Web Frontend
- **Framework:** React 18
- **Language:** TypeScript
- **Styling:** Tailwind CSS
- **HTTP Client:** Axios
- **Build Tool / Bundler:** Vite (for improved performance and fast builds)
- **Styling:** Tailwind CSS
- **Component Library:** shadcn/ui  ← ADD HERE
- **HTTP Client:** Axios
 
### Mobile
- **Language:** Kotlin
- **Architecture:** MVP (Model-View-Presenter)
- **UI:** XML Layouts with View Binding
- **HTTP Client:** Retrofit
- **Min Android API Level:** 24 (Android 7.0+)
- **MVP Structure:**
  - **Model** — Data classes, Repository classes, and Retrofit API calls (e.g., `DeckRepository`, `FlashcardRepository`)
  - **View** — `Activity` / `Fragment` classes that implement a `View` contract interface (e.g., `DeckListActivity : DeckListContract.View`)
  - **Presenter** — Plain Kotlin classes that hold a reference to the View interface and contain all presentation logic (e.g., `DeckListPresenter(view: DeckListContract.View)`)
  - **Contract Interface** — Each screen defines a `Contract` interface with nested `View` and `Presenter` interfaces to enforce the MVP boundary
 
### Deployment
- **Backend:** Railway or Heroku
- **Web:** Vercel or Netlify
- **Mobile:** APK distribution
 
---
 
## Architecture
 
- **Pattern:** Three-tier architecture (Backend API → Web Frontend + Android App)
- **Mobile Pattern:** MVP (Model-View-Presenter)
- **Communication:** RESTful APIs, JSON only
- **Base URL:** `/api/v1`
- **Auth mechanism:** Bearer token (JWT) in `Authorization` header
 
---
 
## API Constraints
 
### Standard Response Structure
All responses must follow this structure:
```json
{
  "success": boolean,
  "data": object | null,
  "error": {
    "code": string,
    "message": string,
    "details": object | null
  },
  "timestamp": string
}
```
 
### Endpoints
 
#### Authentication
| Method | Endpoint | Auth Required |
|--------|----------|---------------|
| POST | `/auth/register` | No |
| POST | `/auth/login` | No |
| POST | `/auth/logout` | Yes |
 
#### Decks
| Method | Endpoint | Auth Required |
|--------|----------|---------------|
| GET | `/decks` | No |
| GET | `/decks/{id}` | No |
| POST | `/decks` | Yes |
| PUT | `/decks/{id}` | Yes |
 
#### Flashcards
| Method | Endpoint | Auth Required |
|--------|----------|---------------|
| GET | `/decks/{id}/cards` | Yes |
| POST | `/decks/{id}/cards` | Yes |
| PUT | `/cards/{cardId}` | Yes |
| DELETE | `/cards/{cardId}` | Yes |
 
#### Quiz & Admin
| Method | Endpoint | Auth Required |
|--------|----------|---------------|
| POST | `/quizzes/results` | Yes |
| GET | `/quizzes/history` | Yes |
| GET | `/admin/stats` | Admin only |
| GET | `/admin/users` | Admin only |
| DELETE | `/admin/users/{id}` | Admin only |
 
### HTTP Status Codes to Use
| Code | Meaning |
|------|---------|
| 200 | OK |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict (duplicate) |
| 500 | Internal Server Error |
 
### Error Codes
| Code | Meaning |
|------|---------|
| AUTH-001 | Invalid credentials |
| AUTH-002 | Token expired |
| AUTH-003 | Insufficient permissions |
| VALID-001 | Validation failed |
| DB-001 | Resource not found |
| DB-002 | Duplicate entry |
| SYSTEM-001 | Internal server error |
 
---
 
## Database Schema
 
### Relationships
- `users` → `decks`: One-to-Many
- `decks` → `flashcards`: One-to-Many
- `users` → `quiz_results`: One-to-Many
- `quiz_results` → `decks`: Many-to-One
 
### Table Definitions
```
users: id, email, password_hash, full_name, role, created_at
decks: id, user_id, title, category, description, created_at
flashcards: id, deck_id, question, answer, tags, created_at
quiz_results: id, user_id, deck_id, score, time_spent, date_taken
```
 
### Roles
- `USER` — standard student/learner
- `ADMIN` — platform administrator
 
---
 
## Security Constraints
- All communications must use **HTTPS**
- Passwords must be hashed with **bcrypt** (salt rounds = 12)
- Admin-prefixed endpoints must enforce **role verification**
- Must implement **SQL injection prevention** and **XSS protection**
- JWT tokens must be used for all authenticated sessions
 
---
 
## Features In Scope
- User registration and authentication (email/password only)
- Deck listing with search functionality
- Flashcard CRUD (add, update, delete)
- Quiz session and scoring
- Responsive web interface
- Native Android mobile application
- Admin panel (user management + system stats)
 
## Features Out of Scope (Do NOT implement)
- Social media / OAuth login
- Payment gateway integration
- Push notifications
- Advanced collaboration tools
 
---
 
## Capacity Constraints
- Must support **100 concurrent users**
 
## Browser & OS Compatibility
- **Browsers:** Chrome, Firefox, Safari, Edge (latest 2 versions)
- **OS:** Windows 10+
- **Screen sizes:** Mobile (360px+), Tablet (768px+), Desktop (1024px+)
