# RoadVault - Frontend Integration

This document explains how the Vue.js frontend is integrated with the Spring Boot backend.

## Architecture

The application uses a single JAR deployment approach where:
- The Vue.js frontend is built using npm commands via `exec-maven-plugin`
- Built frontend assets are copied to Spring Boot's `src/main/resources/static/` directory
- A `SpaController` handles client-side routing for the SPA
- All API endpoints are prefixed with `/api/` to distinguish from frontend routes

## Prerequisites

- **Node.js** (v16 or higher) must be installed on the build machine
- **npm** (v8 or higher) must be available
- For CI/CD: Use setup-node action to install Node.js in GitHub Actions

## Build Profiles

### Development Profile (`dev`)
- Skips frontend build for faster Maven builds
- Use when doing backend development
- Frontend should be run separately with `npm run dev`

```bash
mvn clean package -Pdev
# or
./build.sh dev
```

### Production Profile (`prod`) - Default
- Builds and packages the frontend with the backend using npm directly
- Creates a single deployable JAR
- Default profile when no profile is specified

```bash
mvn clean package -Pprod
# or
./build.sh prod
```

## Build Process

The build uses `exec-maven-plugin` to run npm commands directly:

1. **initialize phase**: `npm ci` - Install dependencies from package-lock.json
2. **test phase**: `npm run test` - Run frontend tests (skippable with `-DskipNpmTests=true`)
3. **prepare-package phase**: `npm run build` - Build the frontend for production
4. **process-resources phase**: Copy built assets to Spring Boot static resources

## Build Commands

Use the provided build script for convenience:

```bash
# Development build (skip frontend)
./build.sh dev

# Production build (include frontend)
./build.sh prod

# Build only frontend
./build.sh frontend

# Clean all build artifacts
./build.sh clean

# Show help
./build.sh help
```

## CI/CD Integration

For GitHub Actions or other CI/CD systems:

```yaml
steps:
  - uses: actions/checkout@v4
  
  - name: Setup Node.js
    uses: actions/setup-node@v4
    with:
      node-version: '20'
      cache: 'npm'
      cache-dependency-path: src/frontend/package-lock.json
  
  - name: Setup Java
    uses: actions/setup-java@v4
    with:
      java-version: '21'
      distribution: 'temurin'
  
  - name: Build application
    run: mvn clean package -Pprod
```

## Development Workflow

### Backend Development
1. Run Spring Boot with dev profile: `mvn spring-boot:run -Pdev`
2. Run frontend separately: `cd src/frontend && npm run dev`
3. Frontend will proxy API calls to backend (configure in Vite if needed)

### Frontend Development
1. Build backend once: `./build.sh dev`
2. Run frontend in dev mode: `cd src/frontend && npm run dev`
3. Hot reload and fast refresh available

### Production Deployment
1. Build complete application: `./build.sh prod`
2. Deploy the JAR from `target/` directory
3. Access application at `http://localhost:8080`

## File Structure

```
src/
├── frontend/              # Vue.js application
│   ├── src/              # Vue.js source code
│   ├── dist/             # Built frontend (generated)
│   ├── package.json      # Frontend dependencies
│   └── vite.config.mjs   # Vite configuration
├── main/
│   ├── java/             # Spring Boot backend
│   └── resources/
│       └── static/       # Frontend assets (copied during build)
└── test/                 # Backend tests
```

## SPA Routing

The `SpaController` ensures that:
- Root path `/` serves the Vue.js application
- Frontend routes (e.g., `/dashboard`, `/members`) are handled by Vue Router
- API routes (e.g., `/api/members`) are handled by Spring Boot controllers
- Static assets are served from `/static/` path

## Configuration Notes

- Vite builds assets with hashed filenames for cache busting
- Assets are placed in `/assets/` subdirectory within static resources
- Spring Boot serves static content automatically from `classpath:/static/`
- CORS configuration may be needed for development mode

## Troubleshooting

### Frontend not loading
- Ensure build completed successfully: check `src/frontend/dist/` exists
- Verify static resources were copied: check `target/classes/static/`
- Check Spring Boot static resource configuration

### API calls failing
- Verify API endpoints start with `/api/`
- Check CORS configuration for development mode
- Ensure backend is running on expected port

### Build failures
- Check Node.js version compatibility
- Clear npm cache: `npm cache clean --force`
- Delete `node_modules` and reinstall: `rm -rf node_modules && npm install`
