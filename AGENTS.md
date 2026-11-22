# MusicAssistantKOR — Agent Instructions

## Repository and scope
- Repository: `SoonCheol/MusicAssistantKOR`
- Sole development branch: `kor`. Always specify this ref for GitHub reads and writes.
- Existing Windows workspace: `E:\Codex\MusicAssistantKOR`. This path describes the user's machine; do not assume it is mounted in a cloud session.
- Preserve the official Music Assistant mobile app foundation, Korean localization, existing features, and compatibility with upstream updates.
- Do not create new repositories, branches, clones, or workspaces without explicit permission. Do not change branches or merge PRs.

## Mandatory startup
1. Read this `AGENTS.md` from the `kor` branch before development work.
2. Check the current `kor` HEAD and inspect relevant repository documents and actual source files.
3. Follow more-specific `AGENTS.md` files if any apply to the files being changed.
4. Reuse the existing GitHub connector and the existing build workflow; do not invent an alternative setup.

## GitHub access recovery
- Discover available GitHub connector actions before claiming that repository access is unavailable.
- Prefer proven operations: `fetch_file` with `ref: kor`, `fetch_commit`, and `compare_commits`.
- GitHub code search may search the default branch rather than `kor`; no search hits do **not** prove that files are absent on `kor`.
- If a file path is unknown, inspect commit changed-file lists and known paths, then fetch the actual file from `kor`.
- Distinguish tool failure, wrong path, wrong ref, permissions, and genuinely missing files.
- A single 404 or failed tool call is not evidence that the repository or its source is lost.
- Do not repeat authentication, clone the repository, or change environments as a workaround without authorization.
- If all supported approaches fail, report the exact attempted operations and errors; never claim inspection succeeded.

## Source-backed analysis
- Read implementation files and follow call sites, state flow, and affected interfaces before proposing code changes.
- Do not infer implementation from README, filenames, search excerpts, or prior chat alone.
- Separate verified facts from hypotheses and unverified areas. Never invent file contents, tests, commits, or build outcomes.
- Keep changes minimal, scoped, and compatible with existing features.

## Execution authorization
- Analysis request: read and report only.
- Modify only when explicitly instructed; commit, push, or build only when explicitly instructed.
- Use the existing GitHub Actions workflow (`.github/workflows/build-kor-apk.yml`) when a build is authorized; check the actual workflow first.
- Avoid unnecessary refactoring, broad tests, repeated builds, and automatic builds triggered by unrelated changes where controllable.
- Never silently modify the Music Assistant server, NAS, Docker, or original music files.

## Reporting
Report briefly in Korean: access result, actual branch/HEAD, files inspected, verified findings, proposed action, and any unverified items. For authorized modifications, additionally report changed files, commit SHA, and actual build status when applicable. Do not claim completion without tool evidence.

## Current feature status
- A single-track tag editor launched from the current track's three-dot menu, with a sheet based on the existing lyrics UI, has been discussed.
- WebDAV-based tag editing is **on hold**. Do not implement it until the user explicitly resumes the work.
