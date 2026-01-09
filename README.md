# AI Movie Recommendation Backend

Spring Boot REST API for movies/series, favorites, and AI-powered recommendations. It syncs content from TMDB and uses Gemini to generate personalized suggestions.

## Features
- JWT-based auth (register/login)
- Movie and series catalog from TMDB
- Favorites (movies/series)
- Personalized recommendations via Gemini
- Recommendation history per user
- Basic erotic-content filtering during TMDB sync

## Tech Stack
- Java 17 + Spring Boot 3.5
- Spring Web, WebFlux, Spring Security
- Spring Data JPA + PostgreSQL
- JWT (jjwt + java-jwt)
- Maven

## Local Setup
Prereqs: Java 17, PostgreSQL, and a TMDB API key.

1) Create a database (default name is `moviedb`).
2) Configure secrets and connection settings in `src/main/resources/application.properties` or via environment variables.
3) Run the app:
```
./mvnw spring-boot:run
```

Server defaults to `http://localhost:8080`.

## Configuration
Current defaults live in `src/main/resources/application.properties`. Consider moving secrets to env vars before pushing to GitHub.

Common overrides (Spring relaxed binding):
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `TMDB_API_KEY`
- `GEMINI_API_KEY`
- `APP_JWT_SECRET`
- `APP_JWT_EXPIRATION`

Optional:
- `OPENROUTER_API_KEY`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`

## API Overview
Base path: `/api`

Auth:
- `POST /auth/register`
- `POST /auth/login`
- `GET /auth/me` (requires `Authorization: Bearer <token>`)

Movies:
- `GET /movies`
- `POST /movies/sync-all`
- `DELETE /movies/erotic`
- `GET /movies/recommendations/history/movie` (auth)

Series:
- `GET /series`
- `POST /series/sync`
- `DELETE /series/clear`
- `GET /series/recommendations/history/serie` (auth)

Favorites (auth):
- `POST /favorites/movie/{movieId}`
- `DELETE /favorites/movie/{movieId}`
- `GET /favorites/movie`
- `POST /favorites/serie/{serieId}`
- `DELETE /favorites/serie/{serieId}`
- `GET /favorites/serie`

Recommendations (auth):
- `GET /recommendations/personal`

TMDB helpers:
- `POST /tmdb/sync-all`
- `GET /tmdb/all`
- `POST /tmdb/scan`

## Notes
- CORS is currently set to allow `http://localhost:3000`.
- `tmdb_progress.json` is used to track TMDB sync progress.
