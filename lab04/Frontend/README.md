# Frontend

A single-page React application that lets users manually trigger the three gateway endpoints and inspect the raw HTTP response — status code and body — in real time.

## Description

The interface exposes three buttons, one per gateway endpoint (GET `/api/waiters/{id}`, POST `/api/routes`, POST `/api/stores`). Clicking a button fires the corresponding request to the API Gateway, and the full response is displayed in a response panel below. A payload textarea lets you edit the waiter ID or JSON body before submitting.

## Tech stack

| Concern         | Technology                        |
| --------------- | --------------------------------- |
| Framework       | React 19 + TypeScript             |
| Build tool      | Vite 8                            |
| Compiler        | React Compiler (via Babel plugin) |
| Linting         | ESLint 9 + typescript-eslint      |
| Package manager | npm                               |

## Configuration

Copy `.env.example` to `.env` and fill in the values:

```dotenv
GATEWAY_URL="http://localhost:8079"   # Base URL of the API Gateway
GATEWAY_KEY=""                         # A valid key from Backend/config/api_keys.json
```

## Setup

```bash
# 1. Install dependencies
npm install

# 2. Copy and fill in environment variables
# Windows
copy .env.example .env
# Linux/Mac
cp .env.example .env
```

## Running

```bash
# Start the development server (default: http://localhost:5173)
npm run dev
```

## Project structure

```
Frontend/
├── src/
│   ├── App.tsx          # Main component – buttons, payload editor, response viewer
│   ├── App.css          # Component styles
│   ├── index.css        # Global styles
│   └── main.tsx         # React entry point
├── .env                 # Environment variables (git-ignored)
├── .env.example
├── index.html
├── package.json
└── vite.config.ts
```
