import { defineConfig } from "@playwright/test";

export default defineConfig({
  testDir: "./tests/e2e",
  use: {
    baseURL: process.env.E2E_BASE_URL || "http://localhost:5173",
    storageState: process.env.E2E_STORAGE_STATE || undefined,
    // Authenticated traces persist request headers/cookies; do not record institutional credentials.
    trace: "off",
    screenshot: "only-on-failure",
  },
  reporter: [["list"], ["html", { open: "never" }]],
});
