# JBikas OPD — Frontend

React 18 + Vite single-page app for the OPD and Lab portal. It talks to `auth-service` (8081), `opd-service` (8082) and `lab-service` (8083).

```bash
npm install
npm run dev      # http://localhost:5173
npm run build    # outputs dist/, served by nginx in the Docker image
```

## Structure

```
src/
├── api/
│   ├── axiosClient.js        # base URLs (VITE_* overrides), JWT header, 401 → sign out
│   ├── errors.js             # apiErrorMessage(): server messages → UI text
│   ├── opdApi.js             # patients + visits (opd-service)
│   └── labApi.js             # catalog, orders, results, attachments, PDF (lab-service)
├── domain/clinical.js        # status/priority/flag labels, vitals list, client-side flag preview, time formatting
├── hooks/                    # useAsync (load + reload), useAction (mutations), useInterval (polling), useRevealOnNarrow
├── components/
│   ├── layout/AppShell.jsx   # sidebar, role navigation, page header
│   ├── ui/                   # Button, Card, Field, Alert, Badge, Tabs, Dialog, EmptyState, Skeleton, Icon
│   ├── clinical/             # StatusBadge, FlagMark, FlaggedValue, PatientBanner, VitalsSummary, LabResultsTable
│   └── ProtectedRoute.jsx    # auth + role guard
├── context/AuthContext.jsx   # session from the JWT (username, name, roles, expiry)
└── modules/
    ├── auth/Login.jsx
    ├── opd/
    │   ├── NurseDashboard.jsx      # tabs: register visit · triage · today's visits
    │   ├── nurse/                  # PatientForm, RegisterVisit, TriagePanel (vitals), TodayVisits
    │   ├── DoctorDashboard.jsx     # priority queue + consultation workspace
    │   └── doctor/                 # ConsultationWorkspace, LabOrderForm, LabOrderCard
    └── lab/
        ├── LabDashboard.jsx        # worklist tabs: to collect · in progress · released
        ├── OrderWorkspace.jsx      # collect, release, amend, cancel
        ├── ResultEntryForm.jsx     # result entry with live flags + critical release dialog
        └── AttachmentsPanel.jsx    # upload / open / remove files
```

Workflow rules and the API contract are described in `../docs/HOSPITAL_WORKFLOWS.md`.

## Conventions

- **No inline styles.** Components use the classes in `styles/global.css`, and those only reference variables from `tokens.css`. Change the look in `tokens.css` first.
- **Shared primitives first.** Reach for `components/ui` before writing new markup. `Field` takes a render function so the label is always linked to its control: `<Field label="Age">{(id) => <input id={id} className="input" />}</Field>`.
- **API calls go through `api/*Api.js`.** Add a function there rather than calling `axiosClient` from a page.
- **Flags come from the server.** `previewResultFlag` / `previewVitalFlag` only preview while typing; keep them in step with `ResultFlagEvaluator` and `VitalsAssessor`.
- **Statuses and flags** are labelled in `domain/clinical.js` and must match the Java enums (`VisitStatus`, `LabOrderStatus`, `ResultFlag`, `VitalFlag`).
- **New role or module:** add the route in `App.jsx`, a nav entry in `NAV_BY_ROLE` (`AppShell.jsx`), and a landing route in `ROLE_HOME` (`Login.jsx`).
- **Hooks before early returns.** Workspaces return early while loading; every hook must be called above those returns.

## Behaviour notes

- Queues and worklists refresh every 30 seconds while the tab is visible.
- A 401 from any service, or token expiry, signs the user out.
- Layout stacks below 1100px; under 820px the sidebar becomes a top bar, and opening a patient or order scrolls the workspace into view.
- Respects `prefers-reduced-motion`.
