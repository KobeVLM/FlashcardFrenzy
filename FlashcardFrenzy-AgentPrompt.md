# AI Agent Prompt — Flashcard Frenzy Web & Mobile Design Implementation

---

## YOUR ROLE

You are a senior frontend developer and UI/UX engineer working on **Flashcard Frenzy**, an EdTech flashcard web and Android application. You have access to:

- **Figma** — the official design files for both the web app and Android app
- **Supabase** — the PostgreSQL database (read schema, validate data shapes, test queries)

Your job is to:
1. Read the Figma design files to understand the intended visual language (colors, typography, spacing, component styles)
2. Implement each page exactly as specified in this prompt — what components go where, what data is shown, and what actions are available
3. **Fix all sidebar inconsistencies** (see Section 3) before touching any individual page
4. Ensure every page uses the correct, unified sidebar and is consistent with the rest of the app

Do not invent new UI patterns. If something is not in the Figma designs and not specified in this prompt, ask before implementing.

---

## SECTION 1 — PROJECT CONTEXT

| Field | Value |
|-------|-------|
| Project Name | Flashcard Frenzy |
| Type | EdTech platform — flashcard study and quiz tool |
| Web Stack | React 18, TypeScript, Tailwind CSS, Axios |
| Backend | Spring Boot 3.x REST API |
| Database | PostgreSQL via Supabase |
| Base API URL | `/api/v1` |
| Auth | JWT Bearer token stored in localStorage |

### User Roles
| Role | Access |
|------|--------|
| Guest (not logged in) | Browse decks, view deck details, view cards |
| USER | Everything a guest can do + create/edit/delete own decks and cards, take quizzes, view quiz history |
| ADMIN | Everything a USER can do + access Admin Panel |

### Supabase Schema (for reference when binding data)
```
users       : id, email, password_hash, full_name, role, created_at
decks       : id, user_id, title, category, description, created_at
flashcards  : id, deck_id, question, answer, tags, created_at
quiz_results: id, user_id, deck_id, score, time_spent, created_at
```

---

## SECTION 2 — GLOBAL DESIGN RULES

Read the Figma design file first and extract the following before writing any code. Apply them globally:

- **Color tokens** — primary, secondary, background, surface, error, text colors
- **Typography scale** — heading sizes (h1–h4), body, caption, label
- **Border radius** — buttons, cards, inputs, modals
- **Spacing scale** — padding and margin units used throughout
- **Shadow levels** — card shadow, dropdown shadow, modal shadow
- **Component styles** — how buttons, inputs, badges, and tags look

### Global Layout
Every page must use this shell:

```
┌─────────────────────────────────────────────┐
│                  TOP NAV BAR                │  ← always visible
├────────────┬────────────────────────────────┤
│            │                                │
│  SIDEBAR   │         PAGE CONTENT           │
│ (fixed,    │         (scrollable)           │
│  left)     │                                │
│            │                                │
└────────────┴────────────────────────────────┘
```

On mobile (below 768px): sidebar collapses into a hamburger menu in the top nav bar.

---

## SECTION 3 — SIDEBAR (FIX THIS FIRST)

> ⚠️ PRIORITY: The sidebar currently has different buttons on different pages. This is wrong.
> The sidebar must be a SINGLE shared component (`<Sidebar />`) rendered identically on every page.
> The only thing that changes is which item is marked as "active" based on the current route.

### Sidebar Component Spec

The sidebar must always show these items, in this order, with visibility rules:

| # | Label | Route | Icon | Visible to |
|---|-------|--------|------|------------|
| 1 | Browse Decks | `/decks` | deck/cards icon | Everyone |
| 2 | My Decks | `/decks/my` | folder icon | Logged-in USER + ADMIN |
| 3 | Quiz History | `/quiz/history` | history/clock icon | Logged-in USER + ADMIN |
| 4 | Admin Panel | `/admin` | shield/settings icon | ADMIN only |
| — | *(divider)* | — | — | — |
| 5 | Login | `/login` | user icon | Guest only |
| 6 | Register | `/register` | user-plus icon | Guest only |
| 7 | Logout | (action, no route) | logout icon | Logged-in USER + ADMIN |

**Rules:**
- Active route item must use the "active" style from the Figma design (highlight, bold, or accent color)
- Logout is a button that calls the logout API, clears the token, and redirects to `/login`
- The sidebar width, font, icon size, and item padding must match the Figma design exactly
- Do NOT add any extra buttons or remove any buttons per individual page
- Build this as a standalone reusable `<Sidebar />` component and import it into the shared layout wrapper

---

## SECTION 4 — PAGE SPECIFICATIONS

Implement each page below exactly. For each page, refer to its corresponding screen in the Figma design file for visual details (layout proportions, card styles, empty states, etc.).

---

### PAGE 1: Login — `/login`

**Purpose:** Authenticate an existing user.

**Layout:** Centered card (no sidebar on this page). The sidebar is hidden on auth pages.

**Components:**
- App logo / name at the top of the card
- Heading: "Welcome Back"
- Email input field (type=email, placeholder "Email address")
- Password input field (type=password, placeholder "Password", with show/hide toggle)
- "Login" primary button (full width)
- Error message area (shows API error e.g. "Invalid credentials" in red)
- Loading spinner inside the button while request is in progress (disables button)
- Link below the button: "Don't have an account? Register" → navigates to `/register`

**On Success:**
- Save `accessToken` and `refreshToken` to localStorage
- Save `email`, `fullName`, `role` to localStorage
- Redirect to `/decks`

**API Call:** `POST /api/v1/auth/login` with `{ email, password }`

**Validation (client-side, before API call):**
- Email must not be empty and must be a valid email format
- Password must not be empty
- Show inline field errors, not toast notifications

---

### PAGE 2: Register — `/register`

**Purpose:** Create a new user account.

**Layout:** Centered card (no sidebar). Same card style as Login.

**Components:**
- App logo / name at the top
- Heading: "Create your account"
- Full Name input (type=text, placeholder "Full name")
- Email input (type=email, placeholder "Email address")
- Password input (type=password, placeholder "Password", show/hide toggle)
- "Create Account" primary button (full width)
- Error message area
- Loading state on button
- Link: "Already have an account? Login" → `/login`

**On Success:**
- Same token/user storage as Login
- Redirect to `/decks`

**API Call:** `POST /api/v1/auth/register` with `{ fullName, email, password }`

**Validation:**
- Full name: not empty
- Email: valid format
- Password: minimum 8 characters
- Show inline field errors

---

### PAGE 3: Browse Decks — `/decks`

**Purpose:** Public deck discovery with search. The home page of the app.

**Layout:** Full layout (with sidebar). Sidebar item "Browse Decks" is active.

**Top of content area:**
- Page heading: "Browse Decks"
- Search bar (full width or prominent width) with placeholder "Search by title or category..."
  - Calls `GET /api/v1/decks?search=<keyword>` as user types (debounced 400ms)
  - Clears to show all decks when search is cleared
- If logged in: "Create Deck" button (primary, top-right of content area) → navigates to `/decks/create`

**Main content:**
- Deck grid or list (match Figma design — likely cards in a responsive grid)
- Each deck card shows:
  - Deck title (bold, prominent)
  - Category (badge/tag style)
  - Owner name ("by [fullName]")
  - Created date (formatted e.g. "May 9, 2026")
  - Clicking the card → navigate to `/decks/:id`

**Empty state:**
- If no decks found: illustration + "No decks found. Try a different search." message
- If search returns nothing: "No results for '[keyword]'"

**Loading state:** Skeleton cards while fetching

**API Call:** `GET /api/v1/decks` (or with `?search=` param)

---

### PAGE 4: My Decks — `/decks/my`

**Purpose:** Show only the logged-in user's own decks. Requires auth.

**Layout:** Full layout (with sidebar). Sidebar item "My Decks" is active.

**Guard:** If not logged in, redirect to `/login`.

**Top of content area:**
- Page heading: "My Decks"
- "Create Deck" button (primary, top-right) → `/decks/create`

**Main content:**
- Same deck card grid as Browse Decks
- Each card additionally shows an action menu or edit/delete buttons (since these are the user's own decks)
  - "Edit" → `/decks/:id/edit`
  - "Delete" → confirmation dialog → `DELETE /api/v1/decks/:id` → remove card from list

**Empty state:**
- "You haven't created any decks yet."
- "Create your first deck" button (primary) → `/decks/create`

**API Call:** `GET /api/v1/decks/my`

---

### PAGE 5: Deck Detail — `/decks/:id`

**Purpose:** Show full deck details and its flashcards. Public page.

**Layout:** Full layout (with sidebar). No sidebar item is "active" (or "Browse Decks" stays active).

**Top section — Deck Info Card:**
- Deck title (large heading)
- Category badge
- Description (full text)
- "By [ownerName]" with created date
- "Start Quiz" button (primary, prominent) — always visible if deck has at least 1 card
  - If deck has 0 cards: button is disabled with tooltip "Add cards to start a quiz"
- If current user is the owner:
  - "Edit Deck" button (secondary) → `/decks/:id/edit`
  - "Delete Deck" button (danger/outlined) → confirmation dialog → `DELETE /api/v1/decks/:id` → redirect to `/decks/my`

**Flashcards Section:**
- Section heading: "Flashcards ([count])"
- If owner is logged in: "Add Card" button (secondary, top-right of section) → opens create card modal or navigates to `/decks/:id/cards/create`
- Flashcard list — each card shows:
  - Question (front)
  - Answer (back, collapsed by default — tap/click to reveal)
  - Tags (small badges, comma-separated from the tags string)
  - If owner: Edit icon + Delete icon per card
    - Edit → opens edit card modal or navigates to `/cards/:cardId/edit`
    - Delete → confirmation → `DELETE /api/v1/cards/:cardId` → remove from list

**Empty cards state:**
- "This deck has no flashcards yet."
- If owner: "Add your first card" button

**API Calls:**
- `GET /api/v1/decks/:id`
- `GET /api/v1/decks/:id/cards`

---

### PAGE 6: Create Deck — `/decks/create`

**Purpose:** Create a new deck. Requires auth.

**Layout:** Full layout (with sidebar). No sidebar item active (or "My Decks").

**Guard:** Redirect to `/login` if not authenticated.

**Form card (centered, reasonable max-width):**
- Page heading: "Create New Deck"
- Title field (required, placeholder "Deck title")
- Category field (optional, placeholder "e.g. Mathematics, History, Science")
- Description field (optional, textarea, placeholder "What is this deck about?")
- "Create Deck" primary button
- "Cancel" text link → back to `/decks/my`
- Loading state on button while submitting
- Inline field error for Title if empty on submit

**On Success:** Redirect to `/decks/:id` (the newly created deck)

**API Call:** `POST /api/v1/decks` with `{ title, category, description }`

---

### PAGE 7: Edit Deck — `/decks/:id/edit`

**Purpose:** Edit an existing deck. Requires auth and ownership.

**Layout:** Same as Create Deck.

**Guard:** Redirect to `/login` if not authenticated. Show 403 error message if not owner.

**Form:** Pre-filled with existing deck data.
- Page heading: "Edit Deck"
- Same fields as Create Deck, pre-populated
- "Save Changes" primary button
- "Cancel" text link → back to `/decks/:id`

**API Calls:**
- `GET /api/v1/decks/:id` (to pre-fill)
- `PUT /api/v1/decks/:id` on submit

---

### PAGE 8: Create / Edit Flashcard — Modal or `/decks/:id/cards/create` and `/cards/:cardId/edit`

**Purpose:** Add a new card to a deck or edit an existing one. Requires auth and deck ownership.

> Check the Figma design — this might be implemented as a modal overlay on the Deck Detail page rather than a separate route. Implement whichever pattern the design shows.

**Form fields:**
- Question textarea (required, placeholder "Enter the question or prompt")
- Answer textarea (required, placeholder "Enter the answer")
- Tags field (optional, placeholder "Comma-separated e.g. algebra, equations, math")
  - Display entered tags as removable badge chips below the input as user types
- "Save Card" primary button
- "Cancel" button or close modal icon

**Validation:**
- Question: not empty
- Answer: not empty

**API Calls:**
- Create: `POST /api/v1/decks/:id/cards`
- Edit: `PUT /api/v1/cards/:cardId` (pre-fill by calling `GET /api/v1/cards/:cardId`)

---

### PAGE 9: Quiz Session — `/quiz/:deckId`

**Purpose:** Client-side quiz session. No server-side session.

**Layout:** Full layout (with sidebar). No sidebar item active.

**Guard:** Redirect to `/login` if not authenticated.

**Flow:**
1. On load: fetch all cards from `GET /api/v1/decks/:deckId/cards`, shuffle them
2. If 0 cards: show "This deck has no cards" message and a back button
3. Quiz session loop per card:

```
┌─────────────────────────────────────┐
│  Card 3 of 10          [Progress]   │
│  [Progress bar: 30%]                │
├─────────────────────────────────────┤
│                                     │
│   [Question text, large, centered]  │
│                                     │
│         [Show Answer]               │
└─────────────────────────────────────┘

After "Show Answer" is clicked:

┌─────────────────────────────────────┐
│  Card 3 of 10          [Progress]   │
│  [Progress bar: 30%]                │
├─────────────────────────────────────┤
│   [Question text]                   │
│   ──────────────                    │
│   [Answer text, revealed]           │
│                                     │
│    [✗ Missed]      [✓ Got it]       │
└─────────────────────────────────────┘
```

4. After all cards are done → show Results screen (same page, new view):

```
┌─────────────────────────────────────┐
│         Quiz Complete! 🎉           │
│                                     │
│   Score:  7 / 10  (70%)            │
│   Time:   1m 24s                    │
│                                     │
│  [View History]   [Try Again]       │
└─────────────────────────────────────┘
```

5. On completion: call `POST /api/v1/quizzes/results` with `{ deckId, score (0-100 percentage), timeSpent (seconds) }` silently in background

**Component state:** Track: cards array, currentIndex, correctCount, isAnswerRevealed, startTime, isFinished

**No API call for individual card answers — all handled client-side.**

---

### PAGE 10: Quiz History — `/quiz/history`

**Purpose:** Show the logged-in user's past quiz results. Requires auth.

**Layout:** Full layout (with sidebar). Sidebar item "Quiz History" is active.

**Guard:** Redirect to `/login` if not authenticated.

**Top of content area:**
- Page heading: "Quiz History"

**Main content:**
- Results list (newest first — API returns them ordered)
- Each result row / card shows:
  - Deck title (clickable → `/decks/:deckId`)
  - Score as percentage: e.g. "85%" with a color indicator (green ≥70%, yellow 40–69%, red <40%)
  - Time spent: e.g. "1m 24s" (convert seconds to mm:ss)
  - Date taken: formatted e.g. "May 9, 2026"

**Empty state:**
- "No quiz history yet."
- "Browse Decks to start your first quiz" button → `/decks`

**API Call:** `GET /api/v1/quizzes/history`

---

### PAGE 11: Admin Panel — `/admin`

**Purpose:** Platform management for ADMIN users only.

**Layout:** Full layout (with sidebar). Sidebar item "Admin Panel" is active.

**Guard:** Redirect to `/login` if not authenticated. Show 403 message if role is not ADMIN.

**Stats Section (top):**
- Section heading: "Platform Overview"
- 4 stat cards in a row (or 2×2 on smaller screens):
  - Total Users (number)
  - Total Decks (number)
  - Total Flashcards (number)
  - Total Quizzes Taken (number)
- Each stat card: icon + label + large number

**User Management Section (below stats):**
- Section heading: "Registered Users"
- Table or card list of all users:

| Full Name | Email | Role | Joined | Actions |
|-----------|-------|------|--------|---------|
| John Doe | john@email.com | USER | May 9, 2026 | [Delete] |

- ADMIN role users: show role as a distinct badge (e.g. purple "ADMIN" vs grey "USER")
- Delete button per user row:
  - Opens confirmation dialog: "Delete [name]? This cannot be undone."
  - On confirm: `DELETE /api/v1/admin/users/:id`
  - Remove row from table immediately on success
- Do NOT allow deleting your own account (hide Delete on your own row)

**API Calls:**
- `GET /api/v1/admin/stats`
- `GET /api/v1/admin/users`
- `DELETE /api/v1/admin/users/:id`

---

## SECTION 5 — ANDROID APP PAGES

The Android app (Kotlin, XML layouts, MVP) mirrors the web app in functionality. Reference the mobile screens in the Figma design file for the mobile visual language (which differs from web).

| Screen | Route equivalent | Key UI elements |
|--------|-----------------|-----------------|
| Login | `/login` | Email, password fields, login button, link to register |
| Register | `/register` | Full name, email, password, register button |
| Deck List | `/decks` | RecyclerView of deck cards, search bar at top, FAB to create (if logged in) |
| My Decks | `/decks/my` | Same as deck list but scoped + edit/delete per card |
| Deck Detail | `/decks/:id` | Deck info header, flashcard list, Start Quiz button, Add Card FAB (owner) |
| Create/Edit Deck | form | Title, category, description fields |
| Create/Edit Card | form | Question, answer, tags fields |
| Quiz Session | quiz screen | Question display, reveal button, correct/wrong buttons, results screen |
| Quiz History | history list | RecyclerView of past results with score, time, date |
| Admin Panel | admin screen | Stats cards + user list with delete |

**Bottom Navigation Bar (Android):**
- Browse Decks (home icon)
- My Decks (folder icon) — hidden if not logged in
- Quiz History (history icon) — hidden if not logged in
- Admin (shield icon) — hidden if not ADMIN

> The bottom nav bar is the Android equivalent of the web sidebar. It must be consistent across all screens the same way the sidebar is on web.

---

## SECTION 6 — CONSISTENCY RULES TO ENFORCE EVERYWHERE

Apply these rules across every page, web and mobile:

1. **Sidebar/nav consistency** — single shared component, identical on every page, only active state changes
2. **Button hierarchy** — every page has at most one primary CTA button; secondary actions use outlined or ghost buttons; destructive actions use red/danger style
3. **Loading states** — every data fetch shows a skeleton or spinner; every form submit disables the button and shows a spinner inside it
4. **Empty states** — every list/grid page has a designed empty state (illustration or icon + message + optional CTA button)
5. **Error states** — API errors show a toast notification (non-blocking) or inline message depending on context; network errors show a retry option
6. **Confirmation dialogs** — all destructive actions (delete deck, delete card, delete user) must show a confirmation dialog before proceeding
7. **Auth guards** — any page requiring auth must silently redirect to `/login` with the intended URL saved so the user is redirected back after login
8. **Token expiry** — if any API call returns 401 with code `AUTH-002`, call `POST /api/v1/auth/refresh` with the stored refresh token; if refresh succeeds, retry the original request; if refresh fails, clear tokens and redirect to `/login`
9. **Responsive layout** — web must work at 360px (mobile), 768px (tablet), 1024px+ (desktop) per the SDD
10. **Tags display** — flashcard tags are comma-separated strings; always split by comma and render each as an individual badge/chip

---

## SECTION 7 — WHAT NOT TO DO

- Do NOT implement social/OAuth login
- Do NOT implement payment, push notifications, or real-time features
- Do NOT add pages or components not specified here or in the Figma designs
- Do NOT change the sidebar structure per page — it must be identical everywhere
- Do NOT store the JWT secret or Supabase credentials in frontend code
- Do NOT use the Supabase JS client directly in the frontend — all data goes through the Spring Boot REST API
