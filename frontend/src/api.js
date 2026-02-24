const BASE_URL = (import.meta.env.VITE_API_BASE_URL || "/api").replace(/\/$/, "");
const ACCESS_TOKEN_KEY = "animalfarm_access_token";
const REFRESH_TOKEN_KEY = "animalfarm_refresh_token";
const USER_KEY = "animalfarm_user";
let refreshInFlight = null;

function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY) || "";
}

function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY) || "";
}

function setSession(data) {
  localStorage.setItem(ACCESS_TOKEN_KEY, data.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken);
  localStorage.setItem(USER_KEY, JSON.stringify({
    username: data.username,
    role: data.role,
    ownerId: data.ownerId,
    mustChangePassword: Boolean(data.mustChangePassword)
  }));
}

function clearSession() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

function authHeaders(extra = {}) {
  const token = getAccessToken();
  return {
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...extra
  };
}

async function doFetch(path, options = {}) {
  const headers = { ...(options.headers || {}) };
  const body = options.body;
  if (!(body instanceof FormData) && !headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: authHeaders(headers)
  });
  return res;
}

async function ensureRefreshed() {
  if (refreshInFlight) {
    return refreshInFlight;
  }
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error("Session expired. Please login again.");
  }
  refreshInFlight = fetch(`${BASE_URL}/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken })
  }).then(async (res) => {
    if (!res.ok) {
      clearSession();
      throw new Error("Session expired. Please login again.");
    }
    const data = await res.json();
    setSession(data);
    return data;
  }).finally(() => {
    refreshInFlight = null;
  });
  return refreshInFlight;
}

async function request(path, options = {}) {
  let res = await doFetch(path, options);
  if (res.status === 401 && !path.startsWith("/auth/")) {
    await ensureRefreshed();
    res = await doFetch(path, options);
  }
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Request failed: ${res.status}`);
  }
  if (res.status === 204) return null;
  const ct = res.headers.get("content-type") || "";
  return ct.includes("application/json") ? res.json() : res.blob();
}

export const authApi = {
  async login(username, password) {
    const data = await request("/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password })
    });
    setSession(data);
    return data;
  },
  async logout() {
    const refreshToken = getRefreshToken();
    try {
      await request("/auth/logout", {
        method: "POST",
        body: JSON.stringify({ refreshToken })
      });
    } finally {
      clearSession();
    }
  },
  async changePassword(currentPassword, newPassword) {
    const data = await request("/auth/change-password", {
      method: "POST",
      body: JSON.stringify({ currentPassword, newPassword })
    });
    setSession(data);
    return data;
  },
  currentUser() {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }
};

export const api = {
  getAnimals: () => request("/animals"),
  registerOwner: (payload) => request("/owners", { method: "POST", body: JSON.stringify(payload) }),
  registerAnimal: (payload) => request("/animals", { method: "POST", body: JSON.stringify(payload) }),
  transferAnimals: (payload) => request("/animals/transfer", { method: "POST", body: JSON.stringify(payload) }),
  sellAnimal: (animalId) => request(`/animals/${animalId}/sell`, { method: "POST" }),
  createTransferRequest: (payload) => request("/transfer-requests", { method: "POST", body: JSON.stringify(payload) }),
  listTransferRequests: () => request("/transfer-requests"),
  approveTransferRequest: (id) => request(`/transfer-requests/${id}/approve`, { method: "POST" }),
  rejectTransferRequest: (id) => request(`/transfer-requests/${id}/reject`, { method: "POST" }),
  getOwner: (ownerId) => request(`/owners/${ownerId}`),
  searchOwners: ({ ownerId, firstName }) => {
    const params = new URLSearchParams();
    if (ownerId) params.set("ownerId", ownerId);
    if (firstName) params.set("firstName", firstName);
    const q = params.toString();
    return request(`/owners/search${q ? `?${q}` : ""}`);
  },
  updateOwner: (ownerId, payload) => request(`/owners/${ownerId}`, { method: "PUT", body: JSON.stringify(payload) }),
  getInvoiceParameters: () => request("/invoice-parameters"),
  updateInvoiceParameters: (payload) => request("/invoice-parameters", { method: "PUT", body: JSON.stringify(payload) }),
  getMonthlyOwnerInvoice: (ownerId) => request(`/invoices/monthly/owner/${ownerId}`),
  getMonthlyOwnersInvoices: () => request("/invoices/monthly/owners"),
  generateAndEmailMonthlyInvoices: (payload) => request("/invoices/monthly/generate-and-email", { method: "POST", body: JSON.stringify(payload || {}) }),
  markInvoicePaid: (invoiceId) => request(`/invoices/${invoiceId}/mark-paid`, { method: "POST" }),
  downloadReport: async (type, value) => {
    let path = `/reports/owner/${value}`;
    if (type === "ownerVsAnimal") path = `/reports/owner-vs-animal?ownerId=${value}`;
    if (type === "parentVsAnimal") path = `/reports/parent-vs-animal?parentId=${value}`;
    if (type === "ownersList") path = "/reports/owners";
    if (type === "ownersAnimalTypeCounts") path = "/reports/owners-animal-type-counts";
    return request(path);
  }
};
