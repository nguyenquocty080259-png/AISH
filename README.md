# <div align="center">

# 

# \# HiveMind — AI Study Hub

# 

# \*\*A collaborative document platform for students, powered by AI-driven search, chat, and recommendations.\*\*

# 

# \[!\[Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk\&logoColor=white)](https://openjdk.org/)

# \[!\[Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot\&logoColor=white)](https://spring.io/projects/spring-boot)

# \[!\[React](https://img.shields.io/badge/React-18-61DAFB?logo=react\&logoColor=black)](https://react.dev/)

# \[!\[PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql\&logoColor=white)](https://www.postgresql.org/)

# \[!\[License](https://img.shields.io/badge/License-Academic-lightgrey)]()

# 

# \*Built as the capstone project for SWP391 — Software Development Project.\*

# 

# \[Features](#-features) • \[Tech Stack](#-tech-stack) • \[Architecture](#-architecture) • \[Getting Started](#-getting-started) • \[Team](#-team)

# 

# </div>

# 

# \---

# 

# \## 📖 About

# 

# \*\*HiveMind\*\* is a study-material hub where students upload, organize, and share documents — and where an integrated \*\*AI assistant\*\* actually understands those documents. Instead of digging through folders, users can chat with an AI that retrieves answers directly from their own library using \*\*RAG (Retrieval-Augmented Generation)\*\*, complete with citations that link back to the exact source document.

# 

# Key ideas behind the project:

# 

# \- \*\*Your documents become your knowledge base.\*\* Every upload is automatically chunked, embedded, and made searchable by the AI — no manual indexing.

# \- \*\*AI answers with receipts.\*\* Chat responses include citations (document, page, chunk) so users can jump straight to the source.

# \- \*\*Community with guardrails.\*\* Public sharing goes through AI-assisted content moderation, with a human admin making the final call and a full appeal flow for owners.

# 

# \## ✨ Features

# 

# \### 🔐 Authentication \& Accounts

# \- Email registration with \*\*OTP verification\*\* (resend with cooldown)

# \- Login via email/password or \*\*Google OAuth2\*\*

# \- Forgot-password flow (OTP → reset), JWT access + refresh tokens

# \- User profile viewing and editing

# 

# \### 📄 Document Management

# \- Upload to \*\*local storage or Cloudinary\*\* (cloud), with title / subject / tags metadata

# \- In-browser \*\*PDF \& text preview\*\*, download tracking, edit metadata

# \- \*\*Soft-delete with Trash\*\*: restore or permanently delete

# \- Public/Private visibility toggle — going public triggers AI moderation review

# \- Comments, \*\*1–5 star ratings\*\* (upsert, no double-counting), and favorites

# 

# \### 🗂 Organization \& Discovery

# \- \*\*Collections\*\*: group documents into named sets (references, not copies)

# \- \*\*Recently viewed\*\* history surfaced on the dashboard

# \- \*\*Community feed\*\* of approved public documents with search, subject/tag/rating filters, and sorting

# \- \*\*Share documents\*\* with specific users (view + comment permissions), plus a "Shared with me" inbox

# \- Search across My Documents, Favorites, and Collections

# 

# \### 🤖 AI Assistant (RAG)

# \- \*\*Global AI chat\*\* available on every page via a floating chat box

# \- \*\*"Ask AI" about a specific document\*\* — answers cite the exact document, page, and chunk

# \- Layered knowledge retrieval: current document → my documents → favorites → collections → public docs → system knowledge

# \- \*\*Automatic embedding pipeline\*\* on upload (extract → chunk → embed → store)

# \- \*\*Related-document recommendations\*\* on detail pages and personalized suggestions on the dashboard

# \- Conversation history persisted per user

# 

# \### 🛡 Moderation \& Administration

# \- \*\*AI-assisted moderation\*\*: uploads are auto-analyzed and flagged content is queued for admin review — the admin always makes the final decision

# \- Admin dashboard: system-wide document list, takedown with owner notification, system statistics

# \- \*\*Appeal flow\*\*: owners of removed documents can appeal; admins approve (restore) or reject

# \- Subject (course) management CRUD

# 

# \## 🛠 Tech Stack

# 

# | Layer | Technology |

# |---|---|

# | \*\*Backend\*\* | Java, Spring Boot, Spring Security (JWT + OAuth2), Spring Data JPA |

# | \*\*Frontend\*\* | React, React Router, Context API |

# | \*\*Database\*\* | PostgreSQL (with vector embeddings for semantic search) |

# | \*\*AI / LLM\*\* | LLM API integration (chat + embeddings), RAG pipeline with citation tracking |

# | \*\*Storage\*\* | Local file system + Cloudinary (pluggable via `StoragePort`) |

# | \*\*Email\*\* | SMTP (OTP delivery for registration \& password reset) |

# 

# \## 🏗 Architecture

# 

# The backend follows a \*\*layered architecture\*\* with clear module boundaries:

# 

# ```

# backend/src/main/java/com/aish/mvc/

# ├── controller/        # REST endpoints

# │   ├── auth/          # Registration, login, OAuth2, profile

# │   ├── admin/         # Admin dashboard, moderation, subjects

# │   ├── doc/           # Documents, collections, community, share

# │   └── ai/            # AI chat, RAG, recommendations

# ├── service/           # Business logic (same module split)

# ├── repository/        # Spring Data JPA repositories

# └── entity/            # JPA entities

# 

# frontend/src/

# ├── pages/             # Route-level pages (Dashboard, Community, ...)

# ├── components/        # Reusable UI (SearchBar, viewers, modals, ...)

# ├── api/               # HTTP client per feature

# └── context/           # Auth \& app-wide state

# ```

# 

# \*\*Design decisions worth noting:\*\*

# 

# \- \*\*Ports \& Adapters at module boundaries.\*\* The AI module never calls document services directly — all cross-module access goes through `DocumentAccessPort`, keeping the AI and Document domains decoupled and independently testable. File storage is likewise abstracted behind `StoragePort` (local / Cloudinary adapters).

# \- \*\*Intentional FK-lite references.\*\* Interaction records (favorites, ratings, downloads, view history, collection items) store plain `documentId`/`userId` values instead of JPA relations. This keeps the soft-delete/trash flow simple: a document can be trashed or restored without cascading constraint headaches.

# \- \*\*Permission-aware RAG.\*\* Every embedding lookup is filtered through the user's access rights — the AI can only cite documents the user is actually allowed to see.

# 

# \## 🚀 Getting Started

# 

# \### Prerequisites

# 

# \- \*\*Java 17+\*\* and \*\*Maven\*\*

# \- \*\*Node.js 18+\*\* and npm

# \- \*\*PostgreSQL 15+\*\* running locally

# \- API keys: an LLM provider key (for chat + embeddings), Cloudinary credentials, and an SMTP account (e.g., Gmail app password)

# 

# \### 1. Clone the repository

# 

# ```bash

# git clone https://github.com/<your-username>/<your-repo>.git

# cd <your-repo>

# ```

# 

# \### 2. Set up the database

# 

# ```sql

# CREATE DATABASE hivemind;

# ```

# 

# The schema is managed by Hibernate (`ddl-auto=update`) — tables are created automatically on first run.

# 

# \### 3. Configure the backend

# 

# Copy the example config and fill in your own credentials:

# 

# ```bash

# cd backend

# cp src/main/resources/application-example.properties src/main/resources/application.properties

# ```

# 

# ```properties

# \# Database

# spring.datasource.url=jdbc:postgresql://localhost:5432/hivemind

# spring.datasource.username=YOUR\_DB\_USER

# spring.datasource.password=YOUR\_DB\_PASSWORD

# 

# \# AI provider

# ai.api.key=YOUR\_LLM\_API\_KEY

# 

# \# Cloudinary

# cloudinary.cloud-name=YOUR\_CLOUD\_NAME

# cloudinary.api-key=YOUR\_API\_KEY

# cloudinary.api-secret=YOUR\_API\_SECRET

# 

# \# SMTP (OTP emails)

# spring.mail.username=YOUR\_EMAIL

# spring.mail.password=YOUR\_APP\_PASSWORD

# 

# \# Google OAuth2

# spring.security.oauth2.client.registration.google.client-id=YOUR\_CLIENT\_ID

# spring.security.oauth2.client.registration.google.client-secret=YOUR\_CLIENT\_SECRET

# ```

# 

# > ⚠️ \*\*Never commit real credentials.\*\* `application.properties` is git-ignored; only the `-example` template is tracked.

# 

# \### 4. Run the backend

# 

# ```bash

# cd backend

# mvn spring-boot:run

# ```

# 

# The API starts at `http://localhost:8080`.

# 

# \### 5. Run the frontend

# 

# ```bash

# cd frontend

# npm install

# npm run dev

# ```

# 

# Open `http://localhost:5173` in your browser.

# 

# \## 📸 Screenshots

# 

# <!-- Replace the placeholders below with real screenshots -->

# 

# | Dashboard | AI Chat with Citations |

# |---|---|

# | !\[Dashboard](docs/screenshots/dashboard.png) | !\[AI Chat](docs/screenshots/ai-chat.png) |

# 

# | Document Detail | Community |

# |---|---|

# | !\[Document Detail](docs/screenshots/document-detail.png) | !\[Community](docs/screenshots/community.png) |

# 

# \## 👥 Team

# 

# Developed by a team of 4 students for \*\*SWP391 @ FPT University\*\*.

# 

# | Member | Role | Responsibilities |

# |---|---|---|

# | \*\*Member 1\*\* | Backend/Frontend | Authentication, Profile, Admin dashboard, Subject management |

# | \*\*Member 2\*\* | Backend/Frontend | Document core: upload, preview, trash, comments, ratings, favorites, search |

# | \*\*Member 3\*\* | Backend/Frontend | Cloud storage, Collections, Recently viewed, Community, Sharing |

# | \*\*Member 4\*\* | Team Lead | AI module (chat, RAG, embeddings, recommendations, moderation), appeals, schema steward |

# 

# \### Workflow

# 

# \- Single shared `develop` branch with small, frequent commits (`pull --rebase` before every push)

# \- Cross-module changes are announced to the team before merging

# \- Database schema changes go through a designated \*\*schema steward\*\* for review

# \- API contract changes (URLs, field names) require team-wide sign-off

# 

# \## 📄 License

# 

# This project was built for academic purposes as part of the SWP391 course. Not intended for production use.

# 

# \---

# 

# <div align="center">

# <sub>Made with ☕ and too many late nights by the HiveMind team</sub>

# </div>

